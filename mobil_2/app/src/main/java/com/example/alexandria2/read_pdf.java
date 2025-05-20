package com.example.alexandria2;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class read_pdf extends AppCompatActivity {

    private static final String BASE_URL = "http://77.222.47.209:3001/api/lecture_add";
    public static String fileName_g;
    private File Pdf_g;
    public static final ArrayList<teachers> TEACHERS = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_pdf);
        loadTeachersFromApi();
        PDFView pdfView = findViewById(R.id.pdfView);

        int idLecture = custom_list.id_lecture;
        if (idLecture == -1) {
            Toast.makeText(this, "Invalid Lecture ID", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                String apiUrl = BASE_URL + "?id_lecture=" + idLecture;

                HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    StringBuilder result = new StringBuilder();
                    int byteRead;
                    while ((byteRead = inputStream.read()) != -1) {
                        result.append((char) byteRead);
                    }

                    JSONObject jsonResponse = new JSONObject(result.toString());
                    int resultCode = jsonResponse.getInt("result");
                    if (resultCode == 0) {
                        JSONObject data = jsonResponse.getJSONObject("data");
                        String fileName = data.getString("name");
                        String base64Pdf = data.getString("file_pdf");

                        decodeAndShowPdf(pdfView, fileName, base64Pdf);
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "Error fetching lecture data", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "HTTP Error: " + responseCode, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void decodeAndShowPdf(PDFView pdfView, String fileName, String base64Pdf) {
        try {
            byte[] pdfBytes = Base64.decode(base64Pdf, Base64.DEFAULT);

            File tempPdf = File.createTempFile(fileName, ".pdf", getCacheDir());
            try (FileOutputStream fos = new FileOutputStream(tempPdf)) {
                fos.write(pdfBytes);
            }
            fileName_g = fileName;
            Pdf_g = tempPdf;

            runOnUiThread(() -> {
                pdfView.fromFile(tempPdf)
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .load();

                EditText name = findViewById(R.id.name);
                name.setText(fileName);
            });
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "Error loading PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }


    public void savePdfToDownloads(File tempPdf, String fileName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName + ".pdf");
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(MediaStore.Downloads.IS_PENDING, 1);

            ContentResolver resolver = getContentResolver();
            Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            Uri fileUri = resolver.insert(collection, values);

            try (OutputStream out = resolver.openOutputStream(fileUri);
                 InputStream in = new FileInputStream(tempPdf)) {

                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }

                values.clear();
                values.put(MediaStore.Downloads.IS_PENDING, 0);
                resolver.update(fileUri, values, null, null);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Файл сохранен в Загрузки", Toast.LENGTH_SHORT).show();
                });

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            // Android 9 и ниже
            File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File dest = new File(downloads, fileName + ".pdf");

            try (InputStream in = new FileInputStream(tempPdf);
                 OutputStream out = new FileOutputStream(dest)) {

                byte[] buffer = new byte[4096];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "Файл сохранен в Загрузки", Toast.LENGTH_SHORT).show();
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void save(View v){
        savePdfToDownloads(Pdf_g, fileName_g);
    }

    public void back(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void send(View v){
        dialogSendMail.showCustomDialog(this);
    }

    private void loadTeachersFromApi() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String apiUrl = "http://77.222.47.209:3001/api/find_teacher";
            ArrayList<teachers> fetchedTeachers = new ArrayList<>();
            String resultMessage;

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONArray jsonArray = new JSONArray(response.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject TeachersJson = jsonArray.getJSONObject(i);
                        fetchedTeachers.add(new teachers(
                                TeachersJson.getInt("id_user"),
                                TeachersJson.getString("surname"),
                                TeachersJson.getString("name"),
                                TeachersJson.getString("mail"),
                                TeachersJson.getInt("type_user_id")
                        ));
                    }
                    resultMessage = "Преподаватели успешно загружены!";
                } else {
                    resultMessage = "Ошибка: Код ответа " + responseCode;
                }

                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                resultMessage = "Ошибка при выполнении запроса: " + e.getMessage();
            }

            String finalResultMessage = resultMessage;
            handler.post(() -> {
                if (!fetchedTeachers.isEmpty()) {
                    TEACHERS.clear();
                    TEACHERS.addAll(fetchedTeachers);
                }
                if(!finalResultMessage.equals("Преподаватели успешно загружены!")){
                    Toast.makeText(this, finalResultMessage, Toast.LENGTH_SHORT).show();
                }

            });
        });
    }

}

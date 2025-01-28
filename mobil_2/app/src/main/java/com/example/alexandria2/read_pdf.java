package com.example.alexandria2;

import android.os.Bundle;
import android.util.Base64;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class read_pdf extends AppCompatActivity {

    private static final String BASE_URL = "http://77.222.47.209:3000/api/lecture_add";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_pdf);

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

            runOnUiThread(() -> {
                // Загружаем PDF в PDFView
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
}

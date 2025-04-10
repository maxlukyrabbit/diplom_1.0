package com.example.alexandria2;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private ListView list;
    private EditText name_lecture_findd, find_cours;
    private Spinner object_find;
    private CheckBox popular;
    private static final ArrayList<Lecture> lectures = new ArrayList<>();
    public static int flag_add = 0;
    private ImageView image_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        image_button = findViewById(R.id.del_old);
        if (add_user.root == 1) {
            image_button.setVisibility(View.VISIBLE);
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        find_cours = findViewById(R.id.find_course);
        name_lecture_findd = findViewById(R.id.name_lecture_find);
        popular = findViewById(R.id.popular);
        object_find = findViewById(R.id.typeObject_find);
        flag_add = 1;
        upload_file.putObject(getApplicationContext(), object_find);
        loadLecturesFromApi();
        reloadListView();
    }

    public void add_file(View v) {
        Intent intent = new Intent(this, upload_file.class);
        startActivity(intent);
    }


    private void reloadListView() {
        custom_list customListViewAdapter = new custom_list(this, lectures);
        list = findViewById(R.id.list_name);
        list.setAdapter(customListViewAdapter);
    }

    private void loadLecturesFromApi() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String apiUrl = "http://77.222.47.209:3001/api/find_all";
            ArrayList<Lecture> fetchedLectures = new ArrayList<>();
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
                        JSONObject lectureJson = jsonArray.getJSONObject(i);
                        fetchedLectures.add(new Lecture(
                                lectureJson.getInt("id_lecture"),
                                lectureJson.getString("name"),
                                lectureJson.getString("object_name"),
                                lectureJson.getInt("course")
                        ));
                    }
                    resultMessage = "Лекции успешно загружены!";
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
                if (!fetchedLectures.isEmpty()) {
                    lectures.clear();
                    lectures.addAll(fetchedLectures);
                }
                reloadListView();
                Toast.makeText(this, finalResultMessage, Toast.LENGTH_SHORT).show();
            });
        });
    }

    public void find_lecture(View v) {
        String name = name_lecture_findd.getText().toString();
        String object_find_text = object_find.getSelectedItem().toString();
        String course = find_cours.getText().toString();
        loadLecturesFromApi_find(
                name.isEmpty() ? null : name,
                object_find_text.isEmpty() || object_find_text.equals("Все предметы") ? null : object_find_text,
                course.isEmpty() ? null : course,
                popular.isChecked() ? "1" : null
        );
        reloadListView();
    }

    private void loadLecturesFromApi_find(@Nullable String nameLecture,
                                          @Nullable String objectName,
                                          @Nullable String course,
                                          @Nullable String popular) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            StringBuilder apiUrlBuilder = new StringBuilder("http://77.222.47.209:3001/api/get_custom");
            ArrayList<String> params = new ArrayList<>();

            try {
                if (nameLecture != null && !nameLecture.isEmpty()) {
                    params.add("name_lecture=" + URLEncoder.encode(nameLecture, StandardCharsets.UTF_8.name()));
                }
                if (objectName != null && !objectName.isEmpty()) {
                    params.add("object_name=" + URLEncoder.encode(objectName, StandardCharsets.UTF_8.name()));
                }
                if (course != null && !course.isEmpty()) {
                    params.add("course=" + URLEncoder.encode(course, StandardCharsets.UTF_8.name()));
                }
                if (popular != null && !popular.isEmpty()) {
                    params.add("popular=" + URLEncoder.encode(popular, StandardCharsets.UTF_8.name()));
                }

                if (!params.isEmpty()) {
                    apiUrlBuilder.append("?").append(TextUtils.join("&", params));
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String apiUrl = apiUrlBuilder.toString();
            ArrayList<Lecture> fetchedLectures = new ArrayList<>();
            String resultMessage;

            HttpURLConnection connection = null;
            try {
                URL url = new URL(apiUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        JSONArray jsonArray = new JSONArray(response.toString());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject lectureJson = jsonArray.getJSONObject(i);
                            fetchedLectures.add(new Lecture(
                                    lectureJson.getInt("id_lecture"),
                                    lectureJson.getString("name"),
                                    lectureJson.getString("object_name"),
                                    lectureJson.getInt("course")
                            ));
                        }
                        resultMessage = "Лекции успешно загружены!";
                    }
                } else {
                    resultMessage = "Ошибка: Код ответа " + responseCode;
                }
            } catch (Exception e) {
                e.printStackTrace();
                resultMessage = "Ошибка при выполнении запроса: " + e.getMessage();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            String finalResultMessage = resultMessage;
            handler.post(() -> {
                if (!fetchedLectures.isEmpty()) {
                    lectures.clear();
                    lectures.addAll(fetchedLectures);
                    reloadListView();
                } else {
                    Toast.makeText(getApplicationContext(), "Того, что вы ищете, нет", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(getApplicationContext(), finalResultMessage, Toast.LENGTH_SHORT).show();
            });
        });
    }


    public void del_old(View v) {
        String URL = "http://77.222.47.209:3001/api/del_old";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(URL)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("HTTP_ERROR", "Ошибка запроса: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Ошибка сети: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.i("HTTP_SUCCESS", "Ответ сервера: " + responseBody);
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Архивация прошла успешно", Toast.LENGTH_SHORT).show());
                } else {
                    Log.e("HTTP_ERROR", "Ошибка: " + response.code());
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Ошибка сервера: " + response.code(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    public void back(View v){
        Intent intent = new Intent(this, add_user.class);
        startActivity(intent);
    }
}

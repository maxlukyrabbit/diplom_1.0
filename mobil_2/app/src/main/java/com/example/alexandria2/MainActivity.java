package com.example.alexandria2;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private ListView list;
    private static final ArrayList<Lecture> lectures = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadLecturesFromApi();
        addSampleLectures();
        reloadListView();
    }

    private void reloadListView() {
        custom_list customListViewAdapter = new custom_list(this, lectures);
        list = findViewById(R.id.list_name);
        list.setAdapter(customListViewAdapter);
    }

    private void addSampleLectures() {
        lectures.add(new Lecture(1,"Laba_1", "1C", 3));
        lectures.add(new Lecture(2, "Laba_2", "1C", 3));
        lectures.add(new Lecture(3, "Laba_3", "1C", 3));
    }

    private void loadLecturesFromApi() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String apiUrl = "http://77.222.47.209:3000/api/find_all";
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

                    // Парсим JSON
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
                Toast.makeText(getApplicationContext(), finalResultMessage, Toast.LENGTH_SHORT).show();
            });
        });
    }


}

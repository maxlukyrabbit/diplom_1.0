package com.example.alexandria2;

import android.content.Context;
import android.util.Base64;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FileUploader {

    // Создаем ExecutorService с фиксированным пулом потоков
    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public static void uploadFileToServer(Context context, File pdfFile, String fileName, int objectId, int course, int userId) {
        // URL сервера
        String url = "http://77.222.47.209:3000/api/lecture_add";

        executorService.execute(() -> {
            // Чтение файла и преобразование в Base64
            String pdfBase64;
            try {
                pdfBase64 = encodeFileToBase64(pdfFile);
            } catch (IOException e) {
                postToMainThread(() -> Toast.makeText(context, "Ошибка при чтении файла: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                return;
            }

            // Формирование JSON данных
            JSONObject jsonData = new JSONObject();
            try {
                jsonData.put("name", fileName);
                jsonData.put("object_id", objectId);
                jsonData.put("file_pdf", pdfBase64);
                jsonData.put("course", course);
                jsonData.put("user_id", userId);
                jsonData.put("count_view", 0);
            } catch (Exception e) {
                postToMainThread(() -> Toast.makeText(context, "Ошибка при создании JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                return;
            }

            // Подготовка запроса OkHttp
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(JSON, jsonData.toString());
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .put(requestBody) // Используем метод PUT
                    .build();

            try {
                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    // Обработка ответа сервера в основном потоке
                    postToMainThread(() -> Toast.makeText(context, "Файл успешно загружен: " + responseBody, Toast.LENGTH_LONG).show());
                } else {
                    // Обработка ошибки
                    postToMainThread(() -> Toast.makeText(context, "Ошибка загрузки файла: " + response.code(), Toast.LENGTH_LONG).show());
                }
            } catch (IOException e) {
                postToMainThread(() -> Toast.makeText(context, "Ошибка запроса: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private static String encodeFileToBase64(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[(int) file.length()];
            fis.read(buffer);
            return Base64.encodeToString(buffer, Base64.NO_WRAP);
        }
    }

    private static void postToMainThread(Runnable action) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(action);
    }
}

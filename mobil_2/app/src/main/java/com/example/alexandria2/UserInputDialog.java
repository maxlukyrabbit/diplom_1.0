package com.example.alexandria2;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class UserInputDialog {
    private static HashMap<String, Integer> specializationMap = new HashMap<>();

    public static void showDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);

        View dialogView = inflater.inflate(R.layout.dialog_user_input, null);
        builder.setView(dialogView);

        // Инициализация полей ввода
        EditText surnameInput = dialogView.findViewById(R.id.surnameInput);
        EditText nameInput = dialogView.findViewById(R.id.nameInput);
        EditText patronymicInput = dialogView.findViewById(R.id.patronymicInput);
        EditText courseInput = dialogView.findViewById(R.id.courseInput);
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);

        Spinner typeUserSpinner = dialogView.findViewById(R.id.typeUserSpinner);
        Spinner specializationSpinner = dialogView.findViewById(R.id.specializationSpinner);

        // Настройка данных для спиннера типа пользователя
        String[] typeUserOptions = {"Студент", "Преподаватель"};
        ArrayAdapter<String> typeUserAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, typeUserOptions);
        typeUserAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeUserSpinner.setAdapter(typeUserAdapter);

        // Настройка кнопок
        builder.setPositiveButton("Зарегистрироваться", null);
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Загрузка специальностей и обновление спиннера
        putSpecializations(context, specializationSpinner);

        // Обработка нажатия на кнопку "Зарегистрироваться"
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(v -> {
            String surname = surnameInput.getText().toString().trim();
            String name = nameInput.getText().toString().trim();
            String patronymic = patronymicInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String typeUser = typeUserSpinner.getSelectedItem().toString();
            String specialization = specializationSpinner.getSelectedItem().toString();

            int course;
            try {
                course = Integer.parseInt(courseInput.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Введите корректный курс", Toast.LENGTH_SHORT).show();
                return;
            }

            if (surname.isEmpty() || name.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Введите фамилию, имя и пароль", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = "http://77.222.47.209:3000/api/user_add";

            JSONObject jsonData = new JSONObject();
            try {
                jsonData.put("surname", surname);
                jsonData.put("name", name);
                jsonData.put("patronymic", patronymic);
                jsonData.put("type_user_id", typeUser.equals("Преподаватель") ? 2 : 1);
                jsonData.put("specialization_id", specializationMap.get(specialization));
                jsonData.put("course", course);
                jsonData.put("password", password);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Ошибка при создании JSON", Toast.LENGTH_SHORT).show();
                return;
            }

            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, jsonData.toString());
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .put(body)
                    .build();

            new Thread(() -> {
                try {
                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(context, "Успех: " + responseBody, Toast.LENGTH_LONG).show();
                        });
                    } else {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(context, "Ошибка: " + response.code(), Toast.LENGTH_LONG).show();
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(context, "Ошибка запроса: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();

            dialog.dismiss();
        });
    }

    private static void putSpecializations(Context context, Spinner specializationSpinner) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String apiUrl = "http://77.222.47.209:3000/api/get_specialization";
            ArrayList<String> specializationNames = new ArrayList<>();

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
                        JSONObject specializationJson = jsonArray.getJSONObject(i);
                        int id = specializationJson.getInt("id_specialization");
                        String name = specializationJson.getString("specialization_name");

                        specializationMap.put(name, id);
                        specializationNames.add(name);
                    }
                } else {
                    throw new IOException("Ошибка: Код ответа " + responseCode);
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> {
                    Toast.makeText(context, "Ошибка при загрузке специальностей: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
                return;
            }

            handler.post(() -> {
                ArrayAdapter<String> specializationAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, specializationNames);
                specializationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                specializationSpinner.setAdapter(specializationAdapter);
            });
        });
    }
}

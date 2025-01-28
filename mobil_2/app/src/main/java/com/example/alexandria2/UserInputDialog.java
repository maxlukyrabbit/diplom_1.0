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

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserInputDialog {

    public static void showDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);

        // Подключение пользовательского интерфейса
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

        // Настройка данных для спиннеров
        String[] typeUserOptions = {"Студент", "Преподаватель"};
        String[] specializationOptions = {
                "Информационные системы и программирование",
                "Автоматизация механизированных систем",
                "Компьютерные сети"
        };

        ArrayAdapter<String> typeUserAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, typeUserOptions);
        typeUserAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeUserSpinner.setAdapter(typeUserAdapter);

        ArrayAdapter<String> specializationAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, specializationOptions);
        specializationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        specializationSpinner.setAdapter(specializationAdapter);

        // Добавление кнопок
        builder.setPositiveButton("Зарегистрироваться", null);
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Обработка нажатия на кнопку "Зарегистрироваться"
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(v -> {
            // Считывание данных пользователя
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

            // Проверка обязательных полей
            if (surname.isEmpty() || name.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Введите фамилию, имя и пароль", Toast.LENGTH_SHORT).show();
                return;
            }

            // URL сервера
            String url = "http://77.222.47.209:3000/api/user_add";

            // Создание JSON-объекта для отправки
            JSONObject jsonData = new JSONObject();
            try {
                jsonData.put("surname", surname);
                jsonData.put("name", name);
                jsonData.put("patronymic", patronymic);
                jsonData.put("type_user_id", typeUser.equals("Преподаватель") ? 2 : 1);
                jsonData.put("specialization_id", specialization.equals("Информационные системы и программирование") ? 1 :
                        specialization.equals("Автоматизация механизированных систем") ? 2 : 3);
                jsonData.put("course", course);
                jsonData.put("password", password);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Ошибка при создании JSON", Toast.LENGTH_SHORT).show();
                return;
            }

            // Создание MediaType для JSON
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            // Создание тела запроса
            RequestBody body = RequestBody.create(JSON, jsonData.toString());

            // Создание клиента OkHttp
            OkHttpClient client = new OkHttpClient();

            // Создание запроса
            Request request = new Request.Builder()
                    .url(url)
                    .put(body) // Метод PUT
                    .build();

            // Выполнение запроса в отдельном потоке
            new Thread(() -> {
                try {
                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();

                        // Обработка ответа сервера в основном потоке
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(context, "Успех: " + responseBody, Toast.LENGTH_LONG).show();
                        });
                    } else {
                        // Обработка ошибки
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
}

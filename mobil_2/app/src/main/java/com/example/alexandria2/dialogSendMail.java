package com.example.alexandria2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class dialogSendMail {

    public static void showCustomDialog(Activity activity) {
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * activity.getResources().getDisplayMetrics().density); // dp -> px
        layout.setPadding(padding, padding, padding, padding);

        Spinner spinner = new Spinner(activity);
        ArrayList<String> surnames = new ArrayList<>();
        ArrayList<String> emails = new ArrayList<>();

        if (read_pdf.TEACHERS != null) {
            for (teachers teacher : read_pdf.TEACHERS) {
                surnames.add(teacher.surname);
                emails.add(teacher.mail);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, surnames);
        spinner.setAdapter(adapter);
        layout.addView(spinner);

        EditText editText = new EditText(activity);
        editText.setHint("Введите вопрос");
        layout.addView(editText);

        new AlertDialog.Builder(activity)
                .setTitle("Выберите преподавателя")
                .setView(layout)
                .setPositiveButton("OK", (dialog, which) -> {
                    String selectedSurname = spinner.getSelectedItem().toString();
                    String inputText = editText.getText().toString();

                    if (inputText.isEmpty()) {
                        Toast.makeText(activity, "Укажите вопрос", Toast.LENGTH_LONG).show();
                        return;
                    }

                    int index = surnames.indexOf(selectedSurname);
                    String selectedEmail = (index >= 0 && index < emails.size()) ? emails.get(index) : null;

                    if (selectedEmail == null) {
                        Toast.makeText(activity, "Email не найден", Toast.LENGTH_LONG).show();
                        return;
                    }

                    new Thread(() -> {
                        try {
                            GMailSender sender = new GMailSender("slucaev059@gmail.com", "hpoy sfjb wkie ojax");
                            sender.sendMail(
                                    "Вопрос по лекции" + read_pdf.fileName_g,
                                    inputText + "\nПожалуйcта, ответьте на почту: " + add_user.mail,
                                    "slucaev059@gmail.com",
                                    selectedEmail
                            );

                            activity.runOnUiThread(() ->
                                    Toast.makeText(activity, "Письмо отправлено", Toast.LENGTH_SHORT).show());

                        } catch (Exception e) {
                            e.printStackTrace();
                            activity.runOnUiThread(() ->
                                    Toast.makeText(activity, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show());
                        }
                    }).start();

                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}

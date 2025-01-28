package com.example.alexandria2;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class upload_file extends AppCompatActivity {
    private Uri uri;
    private EditText name, course;
    private Spinner object;
    public Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_file);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        name = findViewById(R.id.name_lecture);
        course = findViewById(R.id.courseInput);
        object = findViewById(R.id.typeObject);

        String[] typeObject = {"1C", "Программрование"};
        ArrayAdapter<String> typeUserAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, typeObject);
        typeUserAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        object.setAdapter(typeUserAdapter);
    }



    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    uri = result.getData().getData();
                    if (uri != null) {
                        String fileName = getFileNameFromUri(uri);

                        if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
                            Toast.makeText(this, "Выбран файл: " + uri.getPath(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Выберите PDF файл", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Ошибка: файл не выбран", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private String getFileNameFromUri(Uri uri) {
        String result = null;

        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }

        return result;
    }


    public void open_file(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        filePickerLauncher.launch(intent);
    }


    public void add(View v) {
        context = getApplicationContext();

        File pdfFile = null;
        try {
            pdfFile = getFileFromUri(uri);
        } catch (IOException e) {
            Toast.makeText(context, "Ошибка получения файла: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        if (pdfFile == null) {
            Toast.makeText(context, "Не удалось получить файл", Toast.LENGTH_LONG).show();
            return;
        }

        FileUploader.uploadFileToServer(
                context,
                pdfFile,
                name.getText().toString(),
                object.equals("1C") ? 2 : 1,
                4,
                1
        );
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private File getFileFromUri(Uri uri) throws IOException {
        File tempFile = File.createTempFile("upload", ".pdf", context.getCacheDir());
        tempFile.deleteOnExit();

        try (FileInputStream inputStream = (FileInputStream) context.getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }

        return tempFile;
    }




}
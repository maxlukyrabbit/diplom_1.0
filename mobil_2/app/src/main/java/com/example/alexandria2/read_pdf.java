package com.example.alexandria2;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.io.InputStream;
import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;

public class read_pdf extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_pdf);

        PDFView pdfView = findViewById(R.id.pdfView);


        InputStream inputStream = getResources().openRawResource(R.raw.laba_2);
        pdfView.fromStream(inputStream)
                .enableSwipe(true)
                .load();
    }
}

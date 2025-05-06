package com.example.alexandria2;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class custom_list extends BaseAdapter {
    public static int id_lecture = 0;
    private final Context context;
    private final ArrayList<Lecture> items;
    private final OkHttpClient client = new OkHttpClient();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public custom_list(Context context, ArrayList<Lecture> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if ((this.items.get(position).status == 0 && add_user.root != 3) || this.items.get(position).status == 2) {
            View emptyView = new View(context);
            emptyView.setLayoutParams(new AbsListView.LayoutParams(0, 0));
            return emptyView;
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.activity_custom_list, parent, false);

        TextView name = view.findViewById(R.id.lecture_title);
        TextView subject = view.findViewById(R.id.subject);
        TextView course = view.findViewById(R.id.course);
        Button read = view.findViewById(R.id.read);
        ImageView image_new = view.findViewById(R.id.image_new);
        Button delete = view.findViewById(R.id.delete);
        Button agree = view.findViewById(R.id.agree);

        name.setText(name.getText().toString() + this.items.get(position).name);
        subject.setText(subject.getText().toString() + this.items.get(position).object);
        course.setText(course.getText().toString() + String.valueOf(this.items.get(position).course));

        if (this.items.get(position).status == 0 && add_user.root == 3) {
            agree.setVisibility(View.VISIBLE);
            image_new.setVisibility(View.VISIBLE);
        }

        if (add_user.root == 3) {
            delete.setVisibility(View.VISIBLE);
        }
        read.setOnClickListener(v -> {
            id_lecture = this.items.get(position).id_lecture;
            Intent intent = new Intent(context, read_pdf.class);
            context.startActivity(intent);
        });
        delete.setOnClickListener(v ->{
            change_stage(items.get(position).id_lecture, 2);
        });

        agree.setOnClickListener(v ->{
            change_stage(items.get(position).id_lecture, 1);
        });



        return view;
    }

    private void change_stage(int id_lecture, int status){
        executorService.execute(() -> {
            String url = "http://77.222.47.209:3001/api/change_status?status=" + status +"&id_lecture=" + id_lecture;
            Request request = new Request.Builder().url(url).build();
            try (Response response = client.newCall(request).execute()) {
                boolean success = response.isSuccessful();
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (success) {
                        Toast.makeText(context, status == 1 ? "Подтверждение" : "Удаление" + " проведенно успешно. Ожидайте", Toast.LENGTH_LONG).show();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        Intent intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "Ошибка сети: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
}

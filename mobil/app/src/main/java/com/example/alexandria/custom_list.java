package com.example.alexandria;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class custom_list extends BaseAdapter {
    private final Context context;
    private final ArrayList<Lecture> items;

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
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.activity_custom_list, parent, false);
        TextView name = view.findViewById(R.id.lecture_title);
        TextView subject = view.findViewById(R.id.subject);
        TextView course = view.findViewById(R.id.course);


        name.setText(name.getText().toString() + this.items.get(position).name);
        subject.setText(subject.getText().toString() + this.items.get(position).object);
        course.setText(course.getText().toString() + String.valueOf(this.items.get(position).course));

        return view;
    }
}

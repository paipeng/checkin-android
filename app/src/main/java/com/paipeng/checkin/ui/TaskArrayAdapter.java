package com.paipeng.checkin.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.paipeng.checkin.R;
import com.paipeng.checkin.restclient.module.Task;

import java.util.List;

public class TaskArrayAdapter extends ArrayAdapter<Task> {

    public TaskArrayAdapter(@NonNull Context context, int resource, @NonNull List<Task> tasks) {
        super(context, resource, tasks);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Task task = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.task_array_adapter, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.firstLine);
        TextView tvHome = (TextView) convertView.findViewById(R.id.secondLine);
        // Populate the data into the template view using the data object
        tvName.setText(task.getName());
        tvHome.setText(task.getDescription());
        // Return the completed view to render on screen
        return convertView;
    }
}

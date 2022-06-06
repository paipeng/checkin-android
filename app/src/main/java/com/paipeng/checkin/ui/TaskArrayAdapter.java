package com.paipeng.checkin.ui;

import android.content.Context;
import android.util.Log;
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
    private final static String TAG = TaskArrayAdapter.class.getSimpleName();

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

    public void updateTask(int state, Task task) {
        Log.d(TAG, "updateTask: " + state);
        if (state == 0) {
            // save
            add(task);
        } else if (state == 1) {

        } else if (state == 2) {
            remove(task);
        }

    }
}

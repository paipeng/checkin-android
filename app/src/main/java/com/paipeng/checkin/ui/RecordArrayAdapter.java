package com.paipeng.checkin.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.paipeng.checkin.R;
import com.paipeng.checkin.restclient.module.Record;

import java.util.List;

public class RecordArrayAdapter  extends ArrayAdapter<Record> {

    public RecordArrayAdapter(@NonNull Context context, int resource, @NonNull List<Record> tasks) {
        super(context, resource, tasks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Record record = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.record_array_adapter, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.firstLine);
        TextView tvHome = (TextView) convertView.findViewById(R.id.secondLine);
        // Populate the data into the template view using the data object
        tvName.setText(record.getUser().getUsername());
        tvHome.setText(record.getCreateTime().toString() + " - " + record.getId());
        // Return the completed view to render on screen
        return convertView;
    }
}

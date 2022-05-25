package com.paipeng.checkin.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.paipeng.checkin.R;
import com.paipeng.checkin.model.IdCard;
import com.paipeng.checkin.restclient.module.Task;

import java.util.ArrayList;
import java.util.List;

public class IdCardAdapter extends ArrayAdapter<String> {

    private IdCard idCard;
    public IdCardAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public void setIdCard(IdCard idCard) {
        this.idCard = idCard;
        ArrayList<String> data = new ArrayList<>();
        if (idCard.getType() == 0) {
            data.add(idCard.getDepartment());
            data.add(idCard.getCompany());
            data.add(idCard.getIssuedDate());
            data.add(idCard.getExpireDate());
        } else {
            data.add(idCard.getCompany());
            data.add(idCard.getExpireDate());
            data.add(idCard.getSerialNumber());
            data.add(idCard.getChipUID());
        }
        addAll(data);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        String data = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.idcard_adapter, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.firstLine);
        TextView tvHome = (TextView) convertView.findViewById(R.id.secondLine);
        // Populate the data into the template view using the data object
        tvName.setText(data);

        //tvHome.setText(data);
        if (idCard.getType() == 0) {
            if (position == 0) {
                tvHome.setText("部门");
            } else if (position == 1) {
                tvHome.setText("公司");
            } else if (position == 2) {
                tvHome.setText("签发日期");
            } else if (position == 3) {
                tvHome.setText("有效日期");
            }
        } else {
            if (position == 0) {
                tvHome.setText("单位");
            } else if (position == 1) {
                tvHome.setText("有效日期");
            } else if (position == 2) {
                tvHome.setText("证卡编号");
            } else if (position == 3) {
                tvHome.setText("芯片序号");
            }
        }
        // Return the completed view to render on screen
        return convertView;
    }
}

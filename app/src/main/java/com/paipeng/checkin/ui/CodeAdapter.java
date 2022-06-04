package com.paipeng.checkin.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.paipeng.checkin.R;
import com.paipeng.checkin.restclient.module.Code;

import java.util.List;

public class CodeAdapter  extends ArrayAdapter<Code> {
    private final static String TAG = CodeAdapter.class.getSimpleName();
    public CodeAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }


    public void setCodes(List<Code> codes) {
        Log.d(TAG, "setCodes: " + codes.size());
        //this.faceOCRIdCard = faceOCRIdCard;
        addAll(codes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Code code = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.code_adapter, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.firstLine);
        //TextView tvHome = (TextView) convertView.findViewById(R.id.secondLine);
        Switch switchButton = (Switch) convertView.findViewById(R.id.stateSwitch);
        ImageView iconImageview = (ImageView) convertView.findViewById(R.id.icon);

        // Populate the data into the template view using the data object
        //tvName.setText(code.getName());

        tvName.setText(code.getSerialNumber());
        switchButton.setChecked(code.getState()==1);
        switchButton.setText(code.getName());

        Bitmap iconBitmap;
        if (code.getType() == 0) {
            // barcode
            iconBitmap = BitmapFactory.decodeResource(parent.getResources(), R.drawable.barcode);

        } else if (code.getType() == 1) {
            // chip /nfc
            iconBitmap = BitmapFactory.decodeResource(parent.getResources(), R.drawable.nfc);
        } else {
            iconBitmap = BitmapFactory.decodeResource(parent.getResources(), R.drawable.barcode);
        }
        iconImageview.setImageBitmap(iconBitmap);

        // Return the completed view to render on screen
        return convertView;
    }
}

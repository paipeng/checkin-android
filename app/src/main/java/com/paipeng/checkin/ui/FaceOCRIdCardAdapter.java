package com.paipeng.checkin.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.paipeng.checkin.R;
import com.paipeng.checkin.model.FaceOCRIdCard;
import com.paipeng.checkin.model.IdCard;
import com.paipeng.checkin.model.IdCardImage;

import java.util.ArrayList;

public class FaceOCRIdCardAdapter  extends ArrayAdapter<IdCardImage> {
    private final static String TAG = FaceOCRIdCardAdapter.class.getSimpleName();

    private FaceOCRIdCard faceOCRIdCard;

    public FaceOCRIdCardAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public void setFaceOCRIdCard(FaceOCRIdCard faceOCRIdCard) {
        Log.d(TAG, "setFaceOCRIdCard: " + faceOCRIdCard.getType());
        this.faceOCRIdCard = faceOCRIdCard;
        ArrayList<IdCardImage> idCardImages = new ArrayList<>();
        if (faceOCRIdCard.getType() == 0) {
            //data.add(faceOCRIdCard.getDepartment());
            //data.add(faceOCRIdCard.getCompany());
            //data.add(faceOCRIdCard.getIssuedDate());
            //data.add(faceOCRIdCard.getExpireDate());
        } else {
            IdCardImage idCardImage = new IdCardImage();
            idCardImage.setValue(faceOCRIdCard.getCompany());
            idCardImage.setName("单位");
            idCardImage.setBitmap(faceOCRIdCard.getCompanyBitmap());
            idCardImages.add(idCardImage);

            idCardImage = new IdCardImage();
            idCardImage.setValue(faceOCRIdCard.getExpireDate());
            idCardImage.setName("有效日期");
            idCardImage.setBitmap(faceOCRIdCard.getExpireDateBitmap());
            idCardImages.add(idCardImage);

            idCardImage = new IdCardImage();
            idCardImage.setValue(faceOCRIdCard.getSerialNumber());
            idCardImage.setName("证卡编号");
            idCardImage.setBitmap(faceOCRIdCard.getSerialNumberBitmap());
            idCardImages.add(idCardImage);

            idCardImage = new IdCardImage();
            idCardImage.setValue(faceOCRIdCard.getChipUID());
            idCardImage.setName("芯片序号");
            idCardImage.setBitmap(faceOCRIdCard.getChipUIDBitmap());
            idCardImages.add(idCardImage);

        }
        addAll(idCardImages);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        IdCardImage data = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.idcard_adapter, parent, false);
        }
        // Lookup view for data population
        TextView tvName = (TextView) convertView.findViewById(R.id.firstLine);
        TextView tvHome = (TextView) convertView.findViewById(R.id.secondLine);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
        // Populate the data into the template view using the data object
        tvName.setText(data.getValue());

        //tvHome.setText(data);
        if (faceOCRIdCard.getType() == 0) {
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
            tvHome.setText(data.getName());
        }
        imageView.setImageBitmap(data.getBitmap());


        // Return the completed view to render on screen
        return convertView;
    }
}

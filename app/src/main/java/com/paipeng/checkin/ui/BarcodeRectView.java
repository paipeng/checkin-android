package com.paipeng.checkin.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.arcsoft.arcfacedemo.util.DrawHelper;
import com.arcsoft.arcfacedemo.widget.BaseRectView;

public class BarcodeRectView extends BaseRectView {
    public BarcodeRectView(Context context) {
        super(context);
    }

    public BarcodeRectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (drawInfoList != null && drawInfoList.size() > 0) {
            for (int i = 0; i < drawInfoList.size(); i++) {
                DrawHelper.drawBarcodeFrame(canvas, drawInfoList.get(i), DEFAULT_FACE_RECT_THICKNESS, paint);
            }
        }
    }
}

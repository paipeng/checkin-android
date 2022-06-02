package com.paipeng.checkin.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;

import com.arcsoft.arcfacedemo.util.DrawHelper;
import com.arcsoft.arcfacedemo.widget.BaseRectView;

public class IdCardRectView extends BaseRectView {
    private final String TAG = IdCardRectView.class.getSimpleName();
    public IdCardRectView(Context context) {
        super(context);
    }

    public IdCardRectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw: " + drawInfoList);
        if (drawInfoList != null && drawInfoList.size() > 0) {
            for (int i = 0; i < drawInfoList.size(); i++) {
                DrawHelper.drawOCRFrame(canvas, drawInfoList.get(i), DEFAULT_FACE_RECT_THICKNESS, paint);
            }
        }
    }
}

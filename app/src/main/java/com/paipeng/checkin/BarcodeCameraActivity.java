package com.paipeng.checkin;

import android.graphics.Rect;

import com.arcsoft.arcfacedemo.model.DrawInfo;
import com.arcsoft.arcfacedemo.util.face.RecognizeColor;

public class BarcodeCameraActivity extends CameraActivity{

    @Override
    protected void initView() {
        super.initView();
        rectView = findViewById(R.id.single_camera_face_rect_view);
    }

    @Override
    protected Rect getFrameRect() {
        Rect frameRect = new Rect();
        int block_size = previewSize.height / 2;
        frameRect.top = (previewSize.width - block_size)/2;
        frameRect.left = (previewSize.height - block_size)/2;
        frameRect.right = frameRect.left + block_size;
        frameRect.bottom = frameRect.top + block_size;
        return frameRect;
    }

    @Override
    protected DrawInfo getDrawInfo() {
        return new DrawInfo(getFrameRect(),
                0, 0, 0, RecognizeColor.COLOR_SUCCESS, "Barcode Scan");
    }

}

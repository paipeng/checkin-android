package com.paipeng.checkin;

import android.graphics.Rect;

import com.arcsoft.arcfacedemo.model.DrawInfo;
import com.arcsoft.arcfacedemo.util.face.RecognizeColor;

public class OCRCameraActivity extends CameraActivity{

    @Override
    protected void initView() {
        super.initView();
        rectView = findViewById(R.id.single_camera_face_rect_view);
    }

    @Override
    protected Rect getFrameRect() {
        Rect frameRect = new Rect();
        int block_width = previewSize.height / 5 * 4;
        int block_height = block_width * 5 / 3;
        frameRect.top = (previewSize.width - block_height)/2;
        frameRect.left = (previewSize.height - block_width)/2;
        frameRect.right = frameRect.left + block_width;
        frameRect.bottom = frameRect.top + block_height;
        return frameRect;
    }

    @Override
    protected DrawInfo getDrawInfo() {
        return new DrawInfo(getFrameRect(),
                0, 0, 0, RecognizeColor.COLOR_SUCCESS, "IdCard OCR");
    }
}

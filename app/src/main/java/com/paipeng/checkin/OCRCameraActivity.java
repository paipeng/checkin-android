package com.paipeng.checkin;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.arcsoft.arcfacedemo.model.DrawInfo;
import com.arcsoft.arcfacedemo.util.face.RecognizeColor;
import com.paipeng.checkin.utils.ImageUtil;

public class OCRCameraActivity extends CameraActivity {

    private static final String TAG = OCRCameraActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ocr_camera);
        //保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().setAttributes(attributes);
        }

        // Activity启动后就锁定为启动时的方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        //本地人脸库初始化
        FaceServer.getInstance().init(this);

        initView();
    }

    @Override
    protected void initView() {
        super.initView();
        rectView = findViewById(R.id.single_camera_face_rect_view);
    }

    @Override
    protected Rect getFrameRect() {
        Rect frameRect = new Rect();
        int block_width = previewSize.height / 6 * 5;
        int block_height = (int) (block_width * 5.0 / 3.2);
        frameRect.top = (previewSize.width - block_height) / 2;
        frameRect.left = (previewSize.height - block_width) / 2;
        frameRect.right = frameRect.left + block_width;
        frameRect.bottom = frameRect.top + block_height;
        return frameRect;
    }

    @Override
    protected DrawInfo getDrawInfo() {
        return new DrawInfo(getFrameRect(),
                0, 0, 0, RecognizeColor.COLOR_SUCCESS, "IdCard OCR");
    }


    @Override
    protected void handlePreview(byte[] nv21, int width, int height) {
        if (rectView != null) {
            //barcodeRectView.clearFaceInfo();
        }
        // 1920 x 1080
        Log.d(TAG, "preview size: " + width + "-" + height);
        Bitmap idCardBitmap = ImageUtil.getFocusFrameBitmap(nv21, width, height, getFrameRect(), true);
        //ImageUtil.saveImage(idCardBitmap);
    }
}

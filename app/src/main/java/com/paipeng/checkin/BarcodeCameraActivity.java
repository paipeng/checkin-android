package com.paipeng.checkin;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.arcsoft.arcfacedemo.model.DrawInfo;
import com.arcsoft.arcfacedemo.util.face.RecognizeColor;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.paipeng.checkin.utils.BarcodeUtil;

public class BarcodeCameraActivity extends CameraActivity{
    private static final String TAG = BarcodeCameraActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_barcode_camera);
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


    @Override
    protected void handlePreview(byte[] nv21, int width, int height) {
        if (rectView != null) {
            //barcodeRectView.clearFaceInfo();
        }
        // 1920 x 1080
        Log.d(TAG, "preview size: " + previewSize.width + "-" + previewSize.height);

        //Get the data of the specified range of frames
        int block_size = previewSize.height / 2;

        try {
            String decodedText = BarcodeUtil.decode(nv21, previewSize.width, previewSize.height,
                    (previewSize.width - block_size) / 2,
                    (previewSize.height - block_size) / 2,
                    block_size,
                    block_size
            );
            if (decodedText != null) {
                Log.d(TAG, "decodedText: " + decodedText);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("BARCODE_TEXT", decodedText);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
                Log.d(TAG, "face compare success -> back");
            }
        } catch (ChecksumException e) {
            Log.e(TAG, "ChecksumException");
        } catch (NotFoundException e) {
            Log.e(TAG, "NotFoundException");
        } catch (FormatException e) {
            Log.e(TAG, "FormatException");
        }
    }
}

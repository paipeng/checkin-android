package com.paipeng.checkin.model;

import android.graphics.Bitmap;

import com.baidu.paddle.lite.demo.ocr.OcrResultModel;

import java.util.List;

public class FaceOCRIdCard extends IdCard{
    private Bitmap capturedBitmap;
    private List<OcrResultModel> ocrResultModels;
}

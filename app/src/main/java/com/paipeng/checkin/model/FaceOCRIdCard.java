package com.paipeng.checkin.model;

import android.graphics.Bitmap;

import com.baidu.paddle.lite.demo.ocr.OcrResultModel;

import java.util.List;

public class FaceOCRIdCard extends IdCard{
    private Bitmap capturedBitmap;
    private List<OcrResultModel> ocrResultModels;
    private Bitmap chipUIDBitmap;
    private Bitmap expireDateBitmap;
    private Bitmap serialNumberBitmap;
    private Bitmap companyBitmap;
    private Bitmap nameBitmap;
    private float faceScore;

    public Bitmap getCapturedBitmap() {
        return capturedBitmap;
    }

    public void setCapturedBitmap(Bitmap capturedBitmap) {
        this.capturedBitmap = capturedBitmap;
    }

    public List<OcrResultModel> getOcrResultModels() {
        return ocrResultModels;
    }

    public void setOcrResultModels(List<OcrResultModel> ocrResultModels) {
        this.ocrResultModels = ocrResultModels;
    }

    public Bitmap getChipUIDBitmap() {
        return chipUIDBitmap;
    }

    public void setChipUIDBitmap(Bitmap chipUIDBitmap) {
        this.chipUIDBitmap = chipUIDBitmap;
    }


    public Bitmap getExpireDateBitmap() {
        return expireDateBitmap;
    }

    public void setExpireDateBitmap(Bitmap expireDateBitmap) {
        this.expireDateBitmap = expireDateBitmap;
    }

    public Bitmap getSerialNumberBitmap() {
        return serialNumberBitmap;
    }

    public void setSerialNumberBitmap(Bitmap serialNumberBitmap) {
        this.serialNumberBitmap = serialNumberBitmap;
    }

    public Bitmap getCompanyBitmap() {
        return companyBitmap;
    }

    public void setCompanyBitmap(Bitmap companyBitmap) {
        this.companyBitmap = companyBitmap;
    }

    public Bitmap getNameBitmap() {
        return nameBitmap;
    }

    public void setNameBitmap(Bitmap nameBitmap) {
        this.nameBitmap = nameBitmap;
    }

    public boolean isValidate() {
        return this.nameBitmap != null && this.companyBitmap != null && this.serialNumberBitmap != null && this.expireDateBitmap != null && this.chipUIDBitmap != null;
    }

    public void setFaceScore(float faceScore) {
        this.faceScore = faceScore;
    }

    public float getFaceScore() {
        return faceScore;
    }
}

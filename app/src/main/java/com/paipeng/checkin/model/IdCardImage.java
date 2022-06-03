package com.paipeng.checkin.model;

import android.graphics.Bitmap;

public class IdCardImage {
    private String name;
    private String value;
    private Bitmap bitmap;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}

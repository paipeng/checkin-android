package com.paipeng.checkin.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ImageUtil {
    private static final String TAG = ImageUtil.class.getSimpleName();

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Bitmap resize(Bitmap bitmap, int newWidth, int newHeight) {
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);

            return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        } catch (Exception | Error exception) {
            Log.e(TAG, "resize " + exception.getMessage());
            return null;
        }
    }

    public static byte[] bitmapToRGBByteArray(Bitmap bitmap) {
        int bytes = bitmap.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes);
        bitmap.copyPixelsToBuffer(buffer);

        byte[] rgba = buffer.array();
        byte[] pixels = new byte[(rgba.length / 4) * 3];

        int count = rgba.length / 4;
        for (int i = 0; i < count; i++) {

            pixels[i * 3] = rgba[i * 4];        //R
            pixels[i * 3 + 1] = rgba[i * 4 + 1];    //G
            pixels[i * 3 + 2] = rgba[i * 4 + 2];       //B

        }
        return pixels;
    }

    public static Bitmap convertByteToBitmap(byte[] arrData) {
        Bitmap resultBitmap = null;
        if (arrData != null) {
            BitmapFactory.Options objPpt = new BitmapFactory.Options();
            resultBitmap = BitmapFactory.decodeByteArray(arrData, 0, arrData.length, objPpt);
        }
        return resultBitmap;
    }

    public static Bitmap createSingleImageFromMultipleImages(Bitmap firstImage, Bitmap secondImage) {
        Bitmap result = Bitmap.createBitmap(firstImage.getWidth(), firstImage.getHeight(), firstImage.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(firstImage, 0f, 0f, null);
        canvas.drawBitmap(secondImage, 0, 0, null);
        return result;
    }

    public static Bitmap cropYUVToBitmap(byte[] data, int width, int height) {
        YuvImage image = new YuvImage(data, ImageFormat.NV21, width, height, null);

        return null;
    }


    public static byte[] yuv2gray(byte[] yuv, int width, int height) {
        int total = width * height;
        byte[] gray = new byte[total];
        int Y, Cb = 0, Cr = 0, index = 0;
        int R, G, B;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Y = yuv[y * width + x];
                if (Y < 0) Y += 255;

                if ((x & 1) == 0) {
                    Cr = yuv[(y >> 1) * (width) + x + total];
                    Cb = yuv[(y >> 1) * (width) + x + total + 1];

                    if (Cb < 0) Cb += 127;
                    else Cb -= 128;
                    if (Cr < 0) Cr += 127;
                    else Cr -= 128;
                }

                R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
                G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1) + (Cr >> 3) + (Cr >> 4) + (Cr >> 5);
                B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);

                if (R < 0) R = 0;
                else if (R > 255) R = 255;
                if (G < 0) G = 0;
                else if (G > 255) G = 255;
                if (B < 0) B = 0;
                else if (B > 255) B = 255;

                gray[index++] = (byte) ((R + G + B) / 3.0);
            }
        }

        return gray;
    }

    private static byte[] getFocusFrameData(byte[] yuv, int width, int height, Rect frameRect, boolean rotate) {
        Log.d(TAG, "getFocusFrameData: " + width + "-" + height + " frameRect " + frameRect.left + " " + frameRect.top + " / " + frameRect.width() + "-" + frameRect.height());
        int total = width * height;
        byte[] gray = new byte[frameRect.width() * frameRect.height()];
        int Y, Cb = 0, Cr = 0, index = 0;
        int R, G, B;

        int offsetX, offsetY;

        offsetX = frameRect.top;
        offsetY = frameRect.left;

        for (int y = offsetY; y < offsetY + frameRect.width(); y++) {
            for (int x = offsetX; x < offsetX + frameRect.height(); x++) {
                Y = yuv[y * width + x];
                if (Y < 0) Y += 255;

                if ((x & 1) == 0) {
                    Cr = yuv[(y >> 1) * (width) + x + total];
                    Cb = yuv[(y >> 1) * (width) + x + total + 1];

                    if (Cb < 0) Cb += 127;
                    else Cb -= 128;
                    if (Cr < 0) Cr += 127;
                    else Cr -= 128;
                }

                R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
                G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1) + (Cr >> 3) + (Cr >> 4) + (Cr >> 5);
                B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);

                if (R < 0) R = 0;
                else if (R > 255) R = 255;
                if (G < 0) G = 0;
                else if (G > 255) G = 255;
                if (B < 0) B = 0;
                else if (B > 255) B = 255;

                if (rotate) {
                    gray[(x - offsetX) * frameRect.width() + (frameRect.width() - 1 - (y - offsetY))] = (byte) ((R + G + B) / 3.0);
                } else {
                    gray[index++] = (byte) ((R + G + B) / 3.0);
                }
            }
        }
        return gray;
    }


    public static byte[] getFocusFrameData2(byte[] yuv, int width, int height, boolean rotate) {
        int total = width * height;
        byte[] gray = new byte[width * height];
        int Y, Cb = 0, Cr = 0, index = 0;
        int R, G, B;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Y = yuv[y * width + x];
                if (Y < 0) Y += 255;

                if ((x & 1) == 0) {
                    Cr = yuv[(y >> 1) * (width) + x + total];
                    Cb = yuv[(y >> 1) * (width) + x + total + 1];

                    if (Cb < 0) Cb += 127;
                    else Cb -= 128;
                    if (Cr < 0) Cr += 127;
                    else Cr -= 128;
                }

                R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
                G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1) + (Cr >> 3) + (Cr >> 4) + (Cr >> 5);
                B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);

                if (R < 0) R = 0;
                else if (R > 255) R = 255;
                if (G < 0) G = 0;
                else if (G > 255) G = 255;
                if (B < 0) B = 0;
                else if (B > 255) B = 255;

                if (rotate) {
                    gray[x * height + (height - 1 - y)] = (byte) ((R + G + B) / 3.0);
                } else {
                    gray[index++] = (byte) ((R + G + B) / 3.0);
                }
            }
        }
        return gray;
    }

    public static Bitmap getFocusFrameBitmap(byte[] yuv, int width, int height, Rect frameRect, boolean rotate) {
        byte[] gray = getFocusFrameData(yuv, width, height, frameRect, rotate);
        return convertGrayDataToBitmap(gray, frameRect.width(), frameRect.height());
    }

    public static Bitmap convertGrayDataToBitmap(byte[] data, int imageWidth, int imageHeight) {
        try {
            //Create bitmap with width, height, and 4 bytes color (RGBA)
            Bitmap bmp = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
            final int pixCount = imageWidth * imageHeight;
            int[] intGreyBuffer = new int[pixCount];
            for (int i = 0; i < pixCount - 1; i++) {
                int greyValue = (int) data[i] & 0xff;
                intGreyBuffer[i] = 0xff000000 | (greyValue << 16) | (greyValue << 8) | greyValue;
            }

            bmp.setPixels(intGreyBuffer, 0, imageWidth, 0, 0, imageWidth, imageHeight);
            return bmp;
        } catch (Exception e) {
            return null;
        }

    }

    public static String saveImage(Bitmap bmp) {
        if (bmp == null) {
            return null;
        }
        File appDir = new File(Environment.getExternalStorageDirectory(), "IdCard");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Environment.getExternalStorageDirectory() + "/IdCard" + "/" + fileName;
    }
}

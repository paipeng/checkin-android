package com.paipeng.checkin.utils;

import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.EnumMap;
import java.util.Map;
import java.util.Vector;

public class BarcodeUtil {
    private static final String TAG = BarcodeUtil.class.getSimpleName();
    public static String decode(byte[] yuv, int width, int height, int cropLeft, int cropTop, int cropWidth, int cropHeight) throws ChecksumException, NotFoundException, FormatException {
        Log.d(TAG, "decode");
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(yuv, width, height, cropLeft, cropTop, cropWidth, cropHeight, false);
        if (source != null) {
            Log.d(TAG, "source valid");
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
            if (binaryBitmap != null) {
                Log.d(TAG, "binaryBitmap valid");
                Map<DecodeHintType, Object> hints = new EnumMap<>(
                        DecodeHintType.class);
                Vector<BarcodeFormat> decodeFormats;
                decodeFormats = new Vector<>();
                decodeFormats.add(BarcodeFormat.QR_CODE);
                decodeFormats.add(BarcodeFormat.PDF_417);
                decodeFormats.add(BarcodeFormat.DATA_MATRIX);
                hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
                hints.put(DecodeHintType.TRY_HARDER, true);

                QRCodeReader reader = new QRCodeReader();
                Result objRawResult;
                objRawResult = reader.decode(binaryBitmap, hints);

                String strResult = objRawResult.getText();
                Log.d(TAG, "decodeQR: " + strResult);
                System.err.println("decodeQR: " + strResult);
                return strResult;
            }
        }
        return null;
    }
}

package com.paipeng.checkin.utils;

import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class NdefUtil {
    private static final String TAG = NdefUtil.class.getSimpleName();
    private String mTagText;

    /**
     * 读取NFC标签文本数据
     */
    private void readNfcTag(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage msgs[] = null;
            int contentSize = 0;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                    contentSize += msgs[i].toByteArray().length;
                }
            }
            try {
                if (msgs != null) {
                    Log.d(TAG, msgs.length+" 长度");
                    NdefRecord record = msgs[0].getRecords()[0];
                    String textRecord = parseTextRecord(record);
                    mTagText += textRecord + "\n\ntext\n" + contentSize + " bytes";
                    Log.d(TAG, mTagText);
                }
            } catch (Exception e) {
            }
        }
    }


    /**
     * 写标签
     * @param ndef
     * @param tag
     * @param ndefMessage
     * @return
     * @throws IOException
     * @throws FormatException
     */
    private boolean writeMsg(Ndef ndef, Tag tag, NdefMessage ndefMessage) throws IOException, FormatException {
        try {
            if (ndef == null) {
                //showToast("格式化数据开始");
                //Ndef格式类
                NdefFormatable format = NdefFormatable.get(tag);
                format.connect();
                format.format(ndefMessage);
            } else {
                //shotToast("写入数据开始");
                //数据的写入过程一定要有连接操作
                ndef.connect();
                ndef.writeNdefMessage(ndefMessage);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            //shotToast("IO异常，读写失败");
        } catch (FormatException e) {
            e.printStackTrace();
            //shotToast("格式化异常,读写失败");
        } catch (NullPointerException e) {
            //shotToast("格NullPointerException异常,读写失败");
        }catch (IllegalStateException e){
            //shotToast("Close other technology first!");
        }
        return false;
    }



    public static String parseTextRecord(NdefRecord ndefRecord) {
        if (ndefRecord.getType()[0] == 0x54) {
            Log.d(TAG, "ndef type text");
            int offset = 3;
            // byte 0: the length of language code
            // byte 1: language code byte 1: 'z' 0x7A
            // byte 2: language code byte 2: 'h' 0x68
            String text = new String(ndefRecord.getPayload(), offset, ndefRecord.getPayload().length - offset, StandardCharsets.UTF_8);

            Log.d(TAG, "text: " + text);

            return text;
        } else {
            return null;
        }
    }
}

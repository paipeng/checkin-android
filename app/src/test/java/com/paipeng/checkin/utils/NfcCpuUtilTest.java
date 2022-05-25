package com.paipeng.checkin.utils;

import junit.framework.TestCase;

public class NfcCpuUtilTest extends TestCase {

    public void testEncrypt() throws Exception {
        // B9-F8-3E-A8-64-D7-0C-9C-90-00
        byte[] data = {(byte)0xB9, (byte) 0xF8, 0x3E, (byte)0xA8, 0x64, (byte)0xD7, 0x0C, (byte)0x9C};


        int keySize = 24;
        byte[] d = new byte[keySize];
        byte[] k = new byte[keySize];
        for (int i = 0; i < keySize; i ++) {
            d[i] = (byte)i;
            k[i] = (byte)(0xff - i);
        }
        //byte[] encryptedData = NfcCpuUtil.encrypt(data, NfcCpuUtil.CMD_KEY);
        byte[] encryptedData = NfcCpuUtil.encrypt(d, k);
        String text = NfcCpuUtil.printByte(encryptedData);
        System.out.println(text);
    }
}
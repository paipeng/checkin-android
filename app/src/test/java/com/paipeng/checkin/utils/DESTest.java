package com.paipeng.checkin.utils;

import junit.framework.TestCase;

public class DESTest extends TestCase {

    public void testEncrypt() {
        int keySize = 8;
        byte[] d = new byte[keySize];
        char[] k = new char[keySize];
        for (int i = 0; i < keySize; i ++) {
            d[i] = (byte)i;
            k[i] = (char)(0xff - i);
        }
        String text0 = NfcCpuUtil.printByte(d);
        System.out.println(text0);

        //byte[] encryptedData = NfcCpuUtil.encrypt(data, NfcCpuUtil.CMD_KEY);
        DES des = new DES(k);
        byte[] encryptedData = des.encrypt(d);
        String text = NfcCpuUtil.printByte(encryptedData);
        System.out.println(text);


        byte[] decryptedData = des.decrypt(encryptedData);
        String text2 = NfcCpuUtil.printByte(decryptedData);
        System.out.println(text2);
    }

    public void testDecrypt() {
    }
}
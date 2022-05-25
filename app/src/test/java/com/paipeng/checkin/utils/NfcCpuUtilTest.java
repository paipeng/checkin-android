package com.paipeng.checkin.utils;

import junit.framework.TestCase;

import org.junit.Assert;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

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

    public void testTripleDes() throws Exception {
        byte[] secretKey = "9mng65v8jf4lxn93nabf981m".getBytes();
        String secretMessage = "Baeldung secret message";
        System.out.println(secretMessage);
        byte[] secretMessagesBytes = secretMessage.getBytes(StandardCharsets.UTF_8);

        byte[] encryptedData = NfcCpuUtil.tripleDesEncrypt(secretMessagesBytes, secretKey);
        String text = NfcCpuUtil.printByte(encryptedData);
        System.out.println(text);
        String encodedMessage = Base64.getEncoder().encodeToString(encryptedData);
        System.out.println(encodedMessage);

        byte[] decryptedMessageBytes = NfcCpuUtil.tripleDesDecrypt(encryptedData, secretKey);
        String decryptedMessage = new String(decryptedMessageBytes, StandardCharsets.UTF_8);
        System.out.println(decryptedMessage);
    }


    public void testTripleDes2() throws Exception {
        byte[] data = {(byte)0xB9, (byte) 0xF8, 0x3E, (byte)0xA8, 0x64, (byte)0xD7, 0x0C, (byte)0x9C};
        String text = NfcCpuUtil.printByte(data);
        System.out.println(text);

        byte[] encryptedData = NfcCpuUtil.tripleDesEncrypt(data, NfcCpuUtil.CMD_KEY);
        String text1 = NfcCpuUtil.printByte(encryptedData);
        System.out.println(text1);

        byte[] decryptedData = NfcCpuUtil.tripleDesDecrypt(encryptedData, NfcCpuUtil.CMD_KEY);
        String text2 = NfcCpuUtil.printByte(decryptedData);
        System.out.println(text2);

        Assert.assertEquals(text, text2);
    }
}
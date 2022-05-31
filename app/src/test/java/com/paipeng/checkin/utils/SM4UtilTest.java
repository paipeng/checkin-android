package com.paipeng.checkin.utils;

import junit.framework.TestCase;

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.junit.Assert;

public class SM4UtilTest extends TestCase {

    public void testGenerateKey() throws Exception {
        byte[] key = SM4Util.generateKey();
        Assert.assertNotNull(key);
        System.out.println("key size: " + key.length);
        for (int i = 0; i < key.length; i++) {
            System.out.print(Integer.toHexString(key[i] & 0xFF) + ((i<(key.length-1))?"-":""));
        }
        System.out.println("\n");
    }

    public void testGenerateKeySize8() throws Exception {
        byte[] key = SM4Util.generateKey(8*8);
        Assert.assertNotNull(key);
        System.out.println("key size: " + key.length);
        for (int i = 0; i < key.length; i++) {
            System.out.print(Integer.toHexString(key[i] & 0xFF) + ((i<(key.length-1))?"-":""));
        }
        System.out.println("\n");
    }

    public void testGenerateKeySize16() throws Exception {
        byte[] key = SM4Util.generateKey(16*8); // bits
        Assert.assertNotNull(key);
        System.out.println("key size: " + key.length);
        for (int i = 0; i < key.length; i++) {
            System.out.print(Integer.toHexString(key[i] & 0xFF) + ((i<(key.length-1))?"-":""));

        }
        System.out.println("\n");
    }

    public void testEncryptEcb() throws Exception {
    }

    public void testEncrypt_Ecb_Padding() throws Exception {
        byte[] key = SM4Util.generateKey(16*8); // bits
        int data_len = 16;
        byte[] input = new byte[data_len];
        for (int i = 0; i < data_len; i++) {
            input[i] = (byte)i;
        }

        for (int i = 0; i < input.length; i++) {
            System.out.print(Integer.toHexString(input[i] & 0xFF) + ((i<(input.length-1))?"-":""));
        }
        System.out.println("\n");


        byte[] encoded = SM4Util.encrypt_Ecb_Padding(key, input);
        for (int i = 0; i < encoded.length; i++) {
            System.out.print(Integer.toHexString(encoded[i] & 0xFF) + ((i<(encoded.length-1))?"-":""));
        }
        System.out.println("\n");


        byte[] decoded = SM4Util.decrypt_Ecb_Padding(key, encoded);
        for (int i = 0; i < decoded.length; i++) {
            System.out.print(Integer.toHexString(decoded[i] & 0xFF) + ((i<(decoded.length-1))?"-":""));
            Assert.assertEquals(input[i], decoded[i]);
        }
        System.out.println("\n");
    }
    public void testEncrypt_Ecb_Padding2() throws Exception {

        byte[] key = {
            0x01, 0x23, 0x45, 0x67, (byte)0x89, (byte)0xab, (byte)0xcd, (byte)0xef, (byte)0xfe, (byte)0xdc, (byte)0xba, (byte)0x98, 0x76, 0x54, 0x32, 0x10
        } ;

        int data_len = 16;
        byte[] input = new byte[data_len];
        for (int i = 0; i < data_len; i++) {
            input[i] = (byte)i;
        }

        for (int i = 0; i < input.length; i++) {
            System.out.print(Integer.toHexString(input[i] & 0xFF) + ((i<(input.length-1))?"-":""));
        }
        System.out.println("\n");


        byte[] encoded = SM4Util.encrypt_Ecb_Padding(key, input);
        for (int i = 0; i < encoded.length; i++) {
            System.out.print(Integer.toHexString(encoded[i] & 0xFF) + ((i<(encoded.length-1))?"-":""));
        }
        System.out.println("\n");


        byte[] decoded = SM4Util.decrypt_Ecb_Padding(key, encoded);
        for (int i = 0; i < decoded.length; i++) {
            System.out.print(Integer.toHexString(decoded[i] & 0xFF) + ((i<(decoded.length-1))?"-":""));
            Assert.assertEquals(input[i], decoded[i]);
        }
        System.out.println("\n");
    }

    public void testEncrypt_Ecb_Padding3() throws Exception {
        byte[] key = {
                0x01, 0x23, 0x45, 0x67, (byte)0x89, (byte)0xab, (byte)0xcd, (byte)0xef, (byte)0xfe, (byte)0xdc, (byte)0xba, (byte)0x98, 0x76, 0x54, 0x32, 0x10
        } ;
        byte[] input = {0x01, 0x23, 0x45, 0x67, (byte)0x89, (byte)0xab, (byte)0xcd, (byte)0xef, (byte)0xfe, (byte)0xdc, (byte)0xba, (byte)0x98, 0x76, 0x54, 0x32, 0x00};

        System.out.println("input:\n");
        for (int i = 0; i < input.length; i++) {
            System.out.print(Integer.toHexString(input[i] & 0xFF) + ((i<(input.length-1))?"-":""));
        }
        System.out.println("\n");


        byte[] encoded = SM4Util.encrypt_Ecb_Padding(key, input);
        System.out.println("encoded:\n");
        for (int i = 0; i < encoded.length; i++) {
            System.out.print(Integer.toHexString(encoded[i] & 0xFF) + ((i<(encoded.length-1))?"-":""));
        }
        System.out.println("\n");


        byte[] decoded = SM4Util.decrypt_Ecb_Padding(key, encoded);
        System.out.println("decoded:\n");
        for (int i = 0; i < decoded.length; i++) {
            System.out.print(Integer.toHexString(decoded[i] & 0xFF) + ((i<(decoded.length-1))?"-":""));
            Assert.assertEquals(input[i], decoded[i]);
        }
        System.out.println("\n");
    }

    public void testTestEncryptEcb() throws Exception {
        String input = "ABCDEFG12345678";
        System.out.println("input: " + input);
        byte[] key = SM4Util.generateKey();
        String hexKey = ByteUtils.toHexString(key);
        System.out.println("hexKey: " + hexKey);
        String encodedText = SM4Util.encryptEcb(hexKey, input);
        System.out.println("encodedText: " + encodedText);
        String decodedText = SM4Util.decryptEcb(hexKey, encodedText);
        System.out.println("decodedText: " + decodedText);


    }
}
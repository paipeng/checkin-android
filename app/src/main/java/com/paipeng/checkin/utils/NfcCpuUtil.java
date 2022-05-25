package com.paipeng.checkin.utils;

import android.nfc.tech.IsoDep;
import android.util.Log;

import java.io.IOException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class NfcCpuUtil {

    private static final String TAG = NfcCpuUtil.class.getSimpleName();
    /**
     * 1. "COS command box" Enter "00A40000023F00", then click the "transmission command" into the main directory
     */
    private final byte[] CMD_START = {0x00, (byte) 0xA4, 0x00, 0x00, 0x02, 0x3F, 0x00}; // 6f, 15,84, e, 31,50,41,59,2e, 53,59,53,2e, 44,44,46,30,31, a5,3 , 88,1,1,90,0,
    /**
     * The composite external authentication (secret key: FFFFFFFFFFFFFFFF, secret key identification number: 00)
     */
    public static final byte[] CMD_KEY = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    /**
     * 4 2.1 obtain random code {0x00, (byte) 0x84 , 0x00, 0x00, 0x04}
     */
    private final byte[] CMD_GET_RANDOM = {0x00, (byte) 0x84, 0x00, 0x00, 0x08};


    private final byte[] CMD_EXTERN_AUTH = {0x00, (byte) 0x82, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    // 3 deletes all files in the home directory:. 800E000000 (Note: this command deletes all files in the main directory)
    private final byte[] CMD_DEL = {(byte) 0x80, 0x0E, 0x00, 0x00, 0x00};

    // 4. Establish external authentication keys 4.1 Select the root directory (00A4000000)
    // 4.2 build key file (3F 80 E0 00 00 07 00 01 F0 FF FF B0
    // 4.3 create an external authentication key (80 D4 01 00 0D AA 55 FFFFFFFFFFFFFFFF the FOFO 39)
    private final byte[] CMD_CREATE_DIR = {0x00, (byte) 0xA4, 0x00, 0x00, 0x02, 0x3f, 0x00};
    private final byte[] CMD_CREATE_KEY = {(byte) 0x80, (byte) 0xE0, 0x00, 0x00, 0x07, 0x3F, 0x00, (byte) 0xB0, 0x01, (byte) 0xF0, (byte) 0xFF, (byte) 0xFF};
    private final byte[] CMD_CREATE_OUT_KEY = {(byte) 0x80, (byte) 0xD4, (byte) 0x01, (byte) 0x00, (byte) 0x0D, (byte) 0x39, (byte) 0xF0, (byte) 0xF0, (byte) 0xAA
            , (byte) 0x55, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
    // build. 5 from the access key file definition file
    private final byte[] CMD_ACCESS = {(byte) 0x80, (byte) 0xE0, (byte) 0x00, (byte) 0x01, (byte) 0x07, (byte) 0x3F, (byte) 0x01, (byte) 0x8F, (byte) 0x95, (byte) 0xF0, (byte) 0xFF, (byte) 0xFF};
    // fill key 123456
    private final byte[] CMD_ACCESS_INTO = {(byte) 0x80, (byte) 0xD4, (byte) 0x01, (byte) 0x01, (byte) 0x08, (byte) 0x3A, (byte) 0xF0, (byte) 0xEF, (byte) 0x44, (byte) 0x55, (byte) 0x12, (byte) 0x34, (byte) 0x56};
    // 6. create a custom file, identified as 005 (80E000050728000FF4F4FF02)
    private final byte[] CMD_ACCESS_FILE = {(byte) 0x80, (byte) 0xE0, (byte) 0x00, (byte) 0x05, (byte) 0x07, (byte) 0x28, (byte) 0x00, (byte) 0x0F, (byte) 0xF4, (byte) 0xF4, (byte) 0xFF, (byte) 0x02};
    // 7 // write the data into the document identification file 0005
    //7.1 selected the file (00A40000020005)
    // 7.2写数据“112233445566”到该文件（00D6000006112233445566）
    private final byte[] CMD_ACCESS_FILE_CHOOICE = {(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x05};
    private final byte[] CMD_ACCESS_FILE_WRITE = {(byte) 0x00, (byte) 0xD6, (byte) 0x00, (byte) 0x00, (byte) 0x06, (byte) 0x88, (byte) 0x88, (byte) 0x88, (byte) 0x44, (byte) 0x55, (byte) 0x66};

    // ISO-DEP // Declare Tag protocol operation example of
    private final IsoDep tag;

    public NfcCpuUtil(IsoDep Tag) throws IOException {
        // Initialization protocol ISO-DEP Tag action class instance
        this.tag = Tag;
        tag.setTimeout(5000);
        tag.connect();
    }

    public byte[] read(int dataLen) throws IOException {
        // 1 enter the main directory.
        byte[] resp = tag.transceive(CMD_START);
        if (!checkRs(resp)) {
            return null;
        }
        Log.d(TAG, "1 successful access main menu.");
        resp = tag.transceive(CMD_GET_RANDOM); // 2 to obtain random code

        if (!checkRs(resp)) {
            return null;
        }
        Log.d(TAG, "2 Get random code success");

        byte [] random = { resp[0], resp[1], resp[2], resp[3], resp[4], resp[5], resp[6], resp[7]}; // 3 random code 4 bytes + 4 bytes 0
        byte [] desKey;
        try {
            desKey = NfcCpuUtil.desEncrypt(random, CMD_KEY); // production. 4 random code encrypted
            Log.d(TAG, "3 random code encryption production after");
            printByte (desKey);
        } catch (Exception e) {
            e.printStackTrace ();
            desKey = null;
        }

        byte[] request = new byte[CMD_EXTERN_AUTH.length];
        for (int i = 0; i < CMD_EXTERN_AUTH.length; i++) {
            request[i] = CMD_EXTERN_AUTH[i];
        }
        for (int i = 0; i < 8; i++) {
            request[i + 5] = desKey[i];
        }
        Log.d(TAG, "auth with random + key");
        printByte (request);

        resp = tag.transceive(request);
        if (!checkRs(resp)) {
            return null;
        }
        // 0x6A 0x88

        return null;
    }

    public byte[] wirte() throws IOException {
        byte[] resp = tag.transceive(CMD_START); // enter the main directory. 1
        if (checkRs(resp)) {
            Log.d(TAG, "successful access main menu. 1");
            resp = tag.transceive(CMD_GET_RANDOM); // 2 to obtain random code
            if (checkRs(resp)) {
                Log.d(TAG, "Get random code 2");
                byte[] random = {resp[0], resp[1], resp[2], resp[3], 0x00, 0x00, 0x00, 0x00}; // 3 random code 4 bytes + 4 bytes 0
                byte[] desKey;
                try {
                    desKey = encrypt(random, CMD_KEY); // production. 4 random code encrypted
                    Log.d(TAG, "random code encryption production after 3");
                    printByte(desKey);
                } catch (Exception e) {
                    e.printStackTrace();
                    desKey = null;
                }
                // 00 82 00 00 08 7F CF2 73 is A0 5B 9C F1 90
                if (desKey != null && desKey.length > 8) {
                    byte[] respondKey = {0x00, (byte) 0x82, 0x00, 0x00, 0x08, desKey[0], desKey[1], desKey[2], desKey[3], desKey[4], desKey[5], desKey[6], desKey[7]};
                    Log.d(TAG, "4 after producing the encrypted random code command");
                    printByte(respondKey);
                    resp = tag.transceive(respondKey); //. 5 to transmit the encrypted random code, attention here the fourth byte represent the password identifier 00,
                }
                if (checkRs(resp)) {
                    Log.d(TAG, "external authentication success. 5");
                    resp = tag.transceive(CMD_DEL);
                    if (checkRs(resp)) {
                        Log.d(TAG, "6 directory is removed successfully");
                        resp = tag.transceive(CMD_CREATE_DIR);
                        if (checkRs(resp)) {
                            Log.d(TAG, "choose the directory. 7");
                            resp = tag.transceive(CMD_CREATE_KEY);
                            if (checkRs(resp)) {
                                Log.d(TAG, "create the directory. 8");
                                resp = tag.transceive(CMD_CREATE_OUT_KEY);
                                if (checkRs(resp)) {
                                    Log.d(TAG, "9 to create an external authentication key success");
                                    resp = tag.transceive(CMD_ACCESS);
                                    if (checkRs(resp)) {
                                        Log.d(TAG, "10 to establish access from the key file definition file successfully");
                                        resp = tag.transceive(CMD_ACCESS_INTO); // 11 filled key 123 456
                                        if (checkRs(resp)) {
                                            Log.d(TAG, "filling key 123456 success. 11");
                                            resp = tag.transceive(CMD_ACCESS_FILE); // create a custom file 12 is, identified as 005
                                            if (checkRs(resp)) {
                                                Log.d(TAG, "12 to create a custom file, identified as 005 successful");
                                                resp = tag.transceive(CMD_ACCESS_FILE_CHOOICE); // select the file 13 0005
                                                if (checkRs(resp)) {
                                                    Log.d(TAG, "0005 to select the file 13 is successfully");
                                                    resp = tag.transceive(CMD_ACCESS_FILE_WRITE); // 14 write data "112233445566" to the file
                                                    if (checkRs(resp)) { // 15 should close the connection
                                                        Log.d(TAG, "14 write data 112233445566 to the file successfully");
                                                        return "01".getBytes();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean checkRs(byte[] resp) {
        String r = printByte(resp);
        Log.i(TAG, "response " + r);
        int status = ((0xff & resp[resp.length - 2]) << 8) | (0xff & resp[resp.length - 1]);
        return status == 0x9000;
    }

    public static String printByte(byte[] data) {
        StringBuffer bf = new StringBuffer();

        int i = 0;
        for (byte b : data) {
            String h = Integer.toHexString(b & 0xFF);
            if (h.length() <= 1) {
                bf.append("0");
            }

            bf.append(h.toUpperCase());

            if (i == data.length - 1) {
                break;
            }
            bf.append("-");
            i++;
        }
        Log.i(TAG, bf.toString());
        return bf.toString();
    }

    private void print(String msg) {
        Log.i(TAG, msg);
    }

    public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        KeySpec keySpec = new DESedeKeySpec(key);;
        SecretKeyFactory secretKeyFactory;
        secretKeyFactory = SecretKeyFactory.getInstance("DESede");
        SecretKey secretKey = secretKeyFactory.generateSecret(keySpec);


        Cipher c3des = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        SecretKeySpec myKey = new SecretKeySpec(key, "DESede");
        IvParameterSpec ivspec = new IvParameterSpec("01234567".getBytes());

        c3des.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] cipherText = c3des.doFinal(data);
        return cipherText;
    }

    public static byte[] desEncrypt(byte[] data, byte[] key) throws Exception {
        char[] k = new char[key.length];
        for (int i = 0; i < key.length; i ++) {

            k[i] = (char)key[i];
        }
        DES des = new DES(k);

        byte[] encryptedData = des.encrypt(data);
        return encryptedData;
    }

    public static byte[] tripleDesEncrypt(byte[] data, byte[] key) throws Exception {
        /*
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "TripleDES");
        byte[] iv = "a76nb5h9".getBytes();
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher encryptCipher = Cipher.getInstance("TripleDES/CBC/PKCS5Padding");
        encryptCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);

        byte[] encryptedMessageBytes = encryptCipher.doFinal(data);
        return encryptedMessageBytes;

         */
        char[] k = new char[key.length];
        for (int i = 0; i < key.length; i ++) {

            k[i] = (char)key[i];
        }
        DES des = new DES(k);

        byte[] encryptedData = des.encrypt(data);
        encryptedData = des.encrypt(encryptedData);
        encryptedData = des.encrypt(encryptedData);
        return encryptedData;
    }


    public static byte[] tripleDesDecrypt(byte[] data, byte[] key) throws Exception {
        /*
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "TripleDES");
        byte[] iv = "a76nb5h9".getBytes();
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher decryptCipher = Cipher.getInstance("TripleDES/CBC/PKCS5Padding");
        decryptCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);

        byte[] decryptedMessageBytes = decryptCipher.doFinal(data);
        return decryptedMessageBytes;

         */
        char[] k = new char[key.length];
        for (int i = 0; i < key.length; i ++) {

            k[i] = (char)key[i];
        }
        DES des = new DES(k);

        byte[] decryptedData = des.decrypt(data);
        decryptedData = des.decrypt(decryptedData);
        decryptedData = des.decrypt(decryptedData);
        return decryptedData;
    }
}

package com.paipeng.checkin.utils.guomi;

import com.paipeng.checkin.utils.StringUtil;

import junit.framework.TestCase;

import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.custom.gm.SM2P256V1Curve;
import org.junit.Assert;

import java.io.File;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.Security;

public class SM2UtilTest extends TestCase {
    private BigInteger SM2_ECC_GX = new BigInteger(
            "32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7", 16);
    private BigInteger SM2_ECC_GY = new BigInteger(
            "BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0", 16);

    public void testGenerateKeyPair() throws Exception {
        SM2P256V1Curve curve = new SM2P256V1Curve();

        ECDomainParameters DOMAIN_PARAMS = new ECDomainParameters(curve, curve.createPoint(SM2_ECC_GX, SM2_ECC_GY), curve.getOrder(), curve.getCofactor());

        Security.addProvider(new BouncyCastleProvider());
        KeyPair keyPair = SM2Util.generateKeyPair(DOMAIN_PARAMS, new SecureRandom());
        Assert.assertNotNull(keyPair);

        BCECPublicKey publicKey = (BCECPublicKey) keyPair.getPublic();
        Assert.assertNotNull(publicKey);
        BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
        Assert.assertNotNull(privateKey);

        /*
        //保存私钥至文件prikey.txt
        java.io.ObjectOutputStream out=new java.io.ObjectOutputStream(new java.io.FileOutputStream("sm2privatekey.txt"));
        out.writeObject(privateKey);
        out.close();

        //保存公钥至文件pubkey.txt
        out=new java.io.ObjectOutputStream(new java.io.FileOutputStream("sm2publickey.txt"));
        out.writeObject(publicKey);
        out.close();
         */
        SM2Util.savePrivateKey2File(privateKey, new File("sm2privatekey.txt"));
        SM2Util.savePublicKey2File(publicKey, new File("sm2publickey.txt"));
    }

    public void testSM2_EncryptDecrypt() throws Exception {
        SM2P256V1Curve curve = new SM2P256V1Curve();
        ECDomainParameters DOMAIN_PARAMS = new ECDomainParameters(curve, curve.createPoint(SM2_ECC_GX, SM2_ECC_GY), curve.getOrder(), curve.getCofactor());

        Security.addProvider(new BouncyCastleProvider());
        KeyPair keyPair = SM2Util.generateKeyPair(DOMAIN_PARAMS, new SecureRandom());
        Assert.assertNotNull(keyPair);

        BCECPublicKey publicKey = (BCECPublicKey) keyPair.getPublic();
        Assert.assertNotNull(publicKey);
        BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
        Assert.assertNotNull(privateKey);

        byte[] input_data = new byte[128];

        for (int i = 0; i < 128; i++) {
            input_data[i] = (byte) i;
        }

        System.out.println("input:" + StringUtil.bytesToHexString(input_data));


        byte[] cipher = SM2Util.encode(publicKey, input_data);
        System.out.println("cipher:" + StringUtil.bytesToHexString(cipher));
        byte[] output_data = SM2Util.decode(privateKey, cipher);
        System.out.println("output_data:" + StringUtil.bytesToHexString(output_data));

        for (int i = 0; i < 128; i++) {
            Assert.assertEquals(input_data[i], output_data[i]);
        }
    }
}
package com.paipeng.checkin.utils.guomi;

import com.paipeng.checkin.utils.StringUtil;

import junit.framework.TestCase;

import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.custom.gm.SM2P256V1Curve;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
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
        KeyPair keyPair = SM2Util.getInstance().generateKeyPair(DOMAIN_PARAMS, new SecureRandom());
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
        SM2Util.getInstance().savePrivateKey2File(privateKey, new File("sm2privatekey.txt"));
        SM2Util.getInstance().savePublicKey2File(publicKey, new File("sm2publickey.txt"));


        System.out.println("private key data:" + StringUtil.bytesToHexString(privateKey.getD().toByteArray()) + " size: " + privateKey.getD().toByteArray().length);
        System.out.println("public key data:" + StringUtil.bytesToHexString(publicKey.getQ().getEncoded(false)) + " size: " + publicKey.getQ().getEncoded(false).length);
    }

    public void testSM2_EncryptDecrypt() throws Exception {
        SM2P256V1Curve curve = new SM2P256V1Curve();
        ECDomainParameters DOMAIN_PARAMS = new ECDomainParameters(curve, curve.createPoint(SM2_ECC_GX, SM2_ECC_GY), curve.getOrder(), curve.getCofactor());

        KeyPair keyPair = SM2Util.getInstance().generateKeyPair(DOMAIN_PARAMS, new SecureRandom());
        Assert.assertNotNull(keyPair);

        BCECPublicKey publicKey = (BCECPublicKey) keyPair.getPublic();
        Assert.assertNotNull(publicKey);
        BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
        Assert.assertNotNull(privateKey);


        System.out.println("private key data:" + StringUtil.bytesToHexString(privateKey.getD().toByteArray()) + " size: " + privateKey.getD().toByteArray().length);
        System.out.println("public key data:" + StringUtil.bytesToHexString(publicKey.getQ().getEncoded(false)) + " size: " + publicKey.getQ().getEncoded(false).length);

        byte[] input_data = new byte[128];
        for (int i = 0; i < 128; i++) {
            input_data[i] = (byte) i;
        }

        System.out.println("input:" + StringUtil.bytesToHexString(input_data));


        byte[] cipher = SM2Util.getInstance().encode(publicKey, input_data);
        System.out.println("cipher:" + StringUtil.bytesToHexString(cipher));
        byte[] output_data = SM2Util.getInstance().decode(privateKey, cipher);
        System.out.println("output_data:" + StringUtil.bytesToHexString(output_data));

        for (int i = 0; i < 128; i++) {
            Assert.assertEquals(input_data[i], output_data[i]);
        }
    }


    public void testSM2_KeyConvert() throws Exception {
        SM2P256V1Curve curve = new SM2P256V1Curve();
        ECDomainParameters DOMAIN_PARAMS = new ECDomainParameters(curve, curve.createPoint(SM2_ECC_GX, SM2_ECC_GY), curve.getOrder(), curve.getCofactor());

        KeyPair keyPair = SM2Util.getInstance().generateKeyPair(DOMAIN_PARAMS, new SecureRandom());
        Assert.assertNotNull(keyPair);

        BCECPublicKey publicKey = (BCECPublicKey) keyPair.getPublic();
        Assert.assertNotNull(publicKey);
        BCECPrivateKey privateKey = (BCECPrivateKey) keyPair.getPrivate();
        Assert.assertNotNull(privateKey);


        System.out.println("private key data:" + StringUtil.bytesToHexString(privateKey.getD().toByteArray()) + " size: " + privateKey.getD().toByteArray().length);
        System.out.println("public key data:" + StringUtil.bytesToHexString(publicKey.getQ().getEncoded(false)) + " size: " + publicKey.getQ().getEncoded(false).length);

        //PrivateKey key = SecureUtil.generatePrivateKey("SM2", privateKey.getEncoded());

        //System.out.println("private key data:" + StringUtil.bytesToHexString(key.getD().toByteArray()));
    }


    public void testParserKey() {
        String priHex = "5DD701828C424B84C5D56770ECF7C4FE882E654CAC53C7CC89A66B1709068B9D";
        String xHex = "FF6712D3A7FC0D1B9E01FF471A87EA87525E47C7775039D19304E554DEFE0913";
        String yHex = "F632025F692776D4C13470ECA36AC85D560E794E1BCCF53D82C015988E0EB956";
        String encodedPubHex = "04FF6712D3A7FC0D1B9E01FF471A87EA87525E47C7775039D19304E554DEFE0913F632025F692776D4C13470ECA36AC85D560E794E1BCCF53D82C015988E0EB956";
        String signHex = "30450220213C6CD6EBD6A4D5C2D0AB38E29D441836D1457A8118D34864C247D727831962022100D9248480342AC8513CCDF0F89A2250DC8F6EB4F2471E144E9A812E0AF497F801";
        byte[] signBytes = ByteUtils.fromHexString(signHex);
        byte[] src = ByteUtils.fromHexString("0102030405060708010203040506070801020304050607080102030405060708");
        byte[] withId = ByteUtils.fromHexString("31323334353637383132333435363738");

        ECPrivateKeyParameters privateKey = new ECPrivateKeyParameters(
                new BigInteger(ByteUtils.fromHexString(priHex)), SM2Util.DOMAIN_PARAMS);
        ECPublicKeyParameters publicKey = SM2Util.createECPublicKeyParameters(xHex, yHex, SM2Util.CURVE, SM2Util.DOMAIN_PARAMS);
        System.out.println("private key data:" + StringUtil.bytesToHexString(privateKey.getD().toByteArray()) + " size: " + privateKey.getD().toByteArray().length);
        System.out.println("public key data:" + StringUtil.bytesToHexString(publicKey.getQ().getEncoded(false)) + " size: " + publicKey.getQ().getEncoded(false).length);
    }


    public void testParserKey2() {
        String priHex = "055d5686bbbcc3ad82d0747e9156ea576c2c3f6d8c835ee84e9feb9a97e084b2";
        String xHex = "57d89b75344be04a202f0e1ea44c8749677a9d9f1e2fdc72ee3e6c3d111baf13";
        String yHex = "7d73cd69272713576f817091136fcd4b71a713f1bfb71dc69e65ea7ed31104ac";
        String encodedPubHex = "0457d89b75344be04a202f0e1ea44c8749677a9d9f1e2fdc72ee3e6c3d111baf137d73cd69272713576f817091136fcd4b71a713f1bfb71dc69e65ea7ed31104ac";
        /*
        String signHex = "30450220213C6CD6EBD6A4D5C2D0AB38E29D441836D1457A8118D34864C247D727831962022100D9248480342AC8513CCDF0F89A2250DC8F6EB4F2471E144E9A812E0AF497F801";
        byte[] signBytes = ByteUtils.fromHexString(signHex);
        byte[] src = ByteUtils.fromHexString("0102030405060708010203040506070801020304050607080102030405060708");
        byte[] withId = ByteUtils.fromHexString("31323334353637383132333435363738");

         */
        priHex = priHex.toUpperCase();
        xHex = xHex.toUpperCase();
        yHex = yHex.toUpperCase();
        ECPrivateKeyParameters privateKey = new ECPrivateKeyParameters(
                new BigInteger(ByteUtils.fromHexString(priHex)), SM2Util.DOMAIN_PARAMS);
        ECPublicKeyParameters publicKey = SM2Util.createECPublicKeyParameters(xHex, yHex, SM2Util.CURVE, SM2Util.DOMAIN_PARAMS);

        System.out.println("private key data:" + StringUtil.bytesToHexString(privateKey.getD().toByteArray()) + " size: " + privateKey.getD().toByteArray().length);
        System.out.println("public key data:" + StringUtil.bytesToHexString(publicKey.getQ().getEncoded(false)) + " size: " + publicKey.getQ().getEncoded(false).length);


        //private key data:008f5bb6eaf16a30d998e1a3b342951f9eb2d131dc957c50ee18ab046bb9137531 size: 33
        //public key data:0471f39ab071e16e5d5e38944fc8386fc279a90ba282562c511a4072d4b7f200c474b635906c9bb8d8df3ee120641532f5bb09f3032cb090765bc248c0655a7ad7 size: 65

        byte[] input_data = new byte[128];
        for (int i = 0; i < 128; i++) {
            input_data[i] = (byte) i;
        }

        System.out.println("input:" + StringUtil.bytesToHexString(input_data));


        byte[] cipher = SM2Util.getInstance().encode(publicKey, input_data);
        System.out.println("cipher:" + StringUtil.bytesToHexString(cipher));
        byte[] output_data = SM2Util.getInstance().decode(privateKey, cipher);
        System.out.println("output_data:" + StringUtil.bytesToHexString(output_data));

        for (int i = 0; i < 128; i++) {
            Assert.assertEquals(input_data[i], output_data[i]);
        }
    }
}
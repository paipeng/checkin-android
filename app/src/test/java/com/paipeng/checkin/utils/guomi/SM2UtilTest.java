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
import org.bouncycastle.util.encoders.Base64;
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

    public void testParserKey3() {
        String priHex = "055d5686bbbcc3ad82d0747e9156ea576c2c3f6d8c835ee84e9feb9a97e084b2";
        String xHex = "57d89b75344be04a202f0e1ea44c8749677a9d9f1e2fdc72ee3e6c3d111baf13";
        String yHex = "7d73cd69272713576f817091136fcd4b71a713f1bfb71dc69e65ea7ed31104ac";
        String encodedPubHex = "0457d89b75344be04a202f0e1ea44c8749677a9d9f1e2fdc72ee3e6c3d111baf137d73cd69272713576f817091136fcd4b71a713f1bfb71dc69e65ea7ed31104ac";

        priHex = priHex.toUpperCase();
        xHex = xHex.toUpperCase();
        yHex = yHex.toUpperCase();
        ECPrivateKeyParameters privateKey = new ECPrivateKeyParameters(new BigInteger(ByteUtils.fromHexString(priHex)), SM2Util.DOMAIN_PARAMS);
        ECPublicKeyParameters publicKey = SM2Util.createECPublicKeyParameters(xHex, yHex, SM2Util.CURVE, SM2Util.DOMAIN_PARAMS);

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



    public void testParserKey4() {
        String priHex = "055d5686bbbcc3ad82d0747e9156ea576c2c3f6d8c835ee84e9feb9a97e084b2";
        String xHex = "57d89b75344be04a202f0e1ea44c8749677a9d9f1e2fdc72ee3e6c3d111baf13";
        String yHex = "7d73cd69272713576f817091136fcd4b71a713f1bfb71dc69e65ea7ed31104ac";
        String encodedPubHex = "0457d89b75344be04a202f0e1ea44c8749677a9d9f1e2fdc72ee3e6c3d111baf137d73cd69272713576f817091136fcd4b71a713f1bfb71dc69e65ea7ed31104ac";

        /*
        priHex = priHex.toUpperCase();
        xHex = xHex.toUpperCase();
        yHex = yHex.toUpperCase();

         */
        ECPrivateKeyParameters privateKey = SM2Util.getInstance().genereateECPrivateKeyParameters(ByteUtils.fromHexString(priHex));
        ECPublicKeyParameters publicKey = SM2Util.getInstance().genereateECPublicKeyParameters(ByteUtils.fromHexString(xHex), ByteUtils.fromHexString(yHex));

        System.out.println("private key data:" + StringUtil.bytesToHexString(privateKey.getD().toByteArray()) + " size: " + privateKey.getD().toByteArray().length);
        System.out.println("public key data:" + StringUtil.bytesToHexString(publicKey.getQ().getEncoded(false)) + " size: " + publicKey.getQ().getEncoded(false).length);

        byte[] input_data = new byte[128];
        for (int i = 0; i < 128; i++) {
            input_data[i] = (byte) i;
        }

        System.out.println("input:" + StringUtil.bytesToHexString(input_data));


        byte[] cipher = SM2Util.getInstance().encode(publicKey, input_data);
        System.out.println("cipher:" + StringUtil.bytesToHexString(cipher) + " size: " + cipher.length);


        String cipherBase64 = Base64.toBase64String(cipher);
        System.out.println("cipher base64:" + cipherBase64);



        byte[] cipher1 = new byte[224];
        System.arraycopy(cipher, 1, cipher1, 0, cipher1.length);
        System.out.println("cipher1:" + StringUtil.bytesToHexString(cipher1) + " size: " + cipher1.length);

        String cipher1Base64 = Base64.toBase64String(cipher1);
        System.out.println("cipher1 base64:" + cipher1Base64);

        byte[] cipher2 = Base64.decode(cipher1Base64);
        byte[] cipher3 = new byte[225];
        System.arraycopy(cipher2, 0, cipher3, 1, cipher2.length);
        cipher3[0] = 0x04;
        System.out.println("cipher3:" + StringUtil.bytesToHexString(cipher3) + " size: " + cipher3.length);


        byte[] output_data = SM2Util.getInstance().decode(privateKey, cipher3);
        System.out.println("output_data:" + StringUtil.bytesToHexString(output_data));

        for (int i = 0; i < 128; i++) {
            Assert.assertEquals(input_data[i], output_data[i]);
        }

    }


    public void testEncode() {
        String priHex = "055D5686BBBCC3AD82D0747E9156EA576C2C3F6D8C835EE84E9FEB9A97E084B2";
        String xHex = "57D89B75344BE04A202F0E1EA44C8749677A9D9F1E2FDC72EE3E6C3D111BAF13";
        String yHex = "7D73CD69272713576F817091136FCD4B71A713F1BFB71DC69E65EA7ED31104AC";

        ECPrivateKeyParameters privateKey = SM2Util.getInstance().genereateECPrivateKeyParameters(ByteUtils.fromHexString(priHex));
        ECPublicKeyParameters publicKey = SM2Util.getInstance().genereateECPublicKeyParameters(ByteUtils.fromHexString(xHex), ByteUtils.fromHexString(yHex));

        System.out.println("private key data:" + StringUtil.bytesToHexString(privateKey.getD().toByteArray()) + " size: " + privateKey.getD().toByteArray().length);
        System.out.println("public key data:" + StringUtil.bytesToHexString(publicKey.getQ().getEncoded(false)) + " size: " + publicKey.getQ().getEncoded(false).length);

        byte[] input_data = new byte[128];
        for (int i = 0; i < 128; i++) {
            input_data[i] = (byte) i;
        }

        System.out.println("input:" + StringUtil.bytesToHexString(input_data));


        byte[] cipher = SM2Util.getInstance().encode(publicKey, input_data);
        System.out.println("cipher:" + StringUtil.bytesToHexString(cipher) + " size: " + cipher.length);


        String cipherBase64 = Base64.toBase64String(cipher);
        System.out.println("cipher base64:" + cipherBase64);



        byte[] cipher1 = new byte[224];
        System.arraycopy(cipher, 1, cipher1, 0, cipher1.length);
        System.out.println("cipher1:" + StringUtil.bytesToHexString(cipher1) + " size: " + cipher1.length);

        String cipher1Base64 = Base64.toBase64String(cipher1);
        System.out.println("cipher1 base64:" + cipher1Base64);

        byte[] cipher2 = Base64.decode(cipher1Base64);
        byte[] cipher3 = new byte[225];
        System.arraycopy(cipher2, 0, cipher3, 1, cipher2.length);
        cipher3[0] = 0x04;
        System.out.println("cipher3:" + StringUtil.bytesToHexString(cipher3) + " size: " + cipher3.length);


        byte[] output_data = SM2Util.getInstance().decode(privateKey, cipher3);
        System.out.println("output_data:" + StringUtil.bytesToHexString(output_data));

        for (int i = 0; i < 128; i++) {
            Assert.assertEquals(input_data[i], output_data[i]);
        }

    }

    public void testDecode() {
        String priHex = "055d5686bbbcc3ad82d0747e9156ea576c2c3f6d8c835ee84e9feb9a97e084b2";
        String xHex = "57d89b75344be04a202f0e1ea44c8749677a9d9f1e2fdc72ee3e6c3d111baf13";
        String yHex = "7d73cd69272713576f817091136fcd4b71a713f1bfb71dc69e65ea7ed31104ac";
        String encodedPubHex = "0457d89b75344be04a202f0e1ea44c8749677a9d9f1e2fdc72ee3e6c3d111baf137d73cd69272713576f817091136fcd4b71a713f1bfb71dc69e65ea7ed31104ac";

        priHex = priHex.toUpperCase();
        xHex = xHex.toUpperCase();
        yHex = yHex.toUpperCase();
        ECPrivateKeyParameters privateKey = SM2Util.getInstance().genereateECPrivateKeyParameters(ByteUtils.fromHexString(priHex));
        ECPublicKeyParameters publicKey = SM2Util.getInstance().genereateECPublicKeyParameters(ByteUtils.fromHexString(xHex), ByteUtils.fromHexString(yHex));

        String base64 = "";
        base64 = "+sWyhBXOYBkXPJ1O72tQVTA7E8KXXsnXOMBTKJTISR3TPiVMLoYQIBbuoxqO/JGhb8k78PFNEZRb7CllqaWrszOPz60a+sQnzHTnVuc5eBncjRHI0JkzH9qY8rbp86KqeFRbrl+0xp/AXyGqug9lKuPtU+Z6kYKKFvIrdHQ07ikSnWFZ9YFVCmj4tkTKlH6RO9PFcCTz62nmAGRXM5yPmiIzH94G16BclKoCMIjPOSobN8UuPqMOEVRMf6ij6ihRQqE8jrmB6DOAzQ2TpotXpoLs0JHPRMIXtfLO5rnDMd8=";
        byte[] cipher1 = Base64.decode(base64);
        System.out.println("cipher:  " + StringUtil.bytesToHexString(cipher1) + " size: " + cipher1.length);
        String aa = "04" + "2D6BBF83B440A64C78C13546D8E22BBBFE51339CB86057A3AE9DC889EADE127E4EF7DA13EFE254796ED92027AA1954BD2311520E876C426D65FB249D60BF56BF0A2E904161C878097BDFD30AE362A92A5A0F45E67D218027B111C80FDAC238FBA882828D2F8E7BBED4F6D0BEC65D3A5BF6F5991A1229E5F6C855163500835CFFAA76B63B5B29C27035C544852EE6A3FDA1CC1C8C1C069CCAD08EC034934F90B049781A85B2F9AA9761024F39CF4BBB88D94C1A8399628DCEA82DA192DB58AC68B5557198128BE3564EE6F7FA0567D6F5C578C0A47EFCDCD7FED475FD51033F2A";
        //aa = "04BB43FDC85D59E34953816D3109CD3CFFBBF7DB3D519DD67C1BD5FA8FA46E1219572E02351ADD2D2D9AC7C51E81E5648708103DBCA81669AB5F18F618C3F6EEA8D88E0D008FC536300D97F4DD468CF39D5726E5BDA9AB0F191EE7C4E47752F18B975C3D7F84EBC6BEA09470C94A61F91C24F332FE08282D72C677D4656DF5AD368BA265FA371A57552BE51318A0FC355A603FF08DCD5958BBDBCEFB8B0A8D88D19DEE1D209913E74783028C1188A9D5E244D3216894FCF8F3241FDF479CCDC58A2C9A3F4C718769155B27884BE8870075CEB8935F2417EDAF8EFBFE7DAB5A3653";
        System.out.println("cipher:  " + aa + " size: " + aa.length());

        byte[] cipher = new byte[225];
        System.arraycopy(cipher1, 0, cipher, 1, cipher.length-1);
        cipher[0] = 0x04;
        System.out.println("cipher:" + StringUtil.bytesToHexString(cipher) + " size: " + cipher.length);

        // cipher:B7C65C14B1DB0A9BBAD252FE7257CC83577D3FE3FC3F81605792912B91DF6C4B1C2AAAC7E6A632671E8323825FBCA4A5AE76A4C3FADC00F73909B9D7670BFD7459AA177986E20F546EED88D76D75C3B0D6C29D3A11A911837F523932191EC4FB70C6737FA2E232743BA08758CF8EF2A6722AC07D250A48B9F9206586F214F0E8F9FD781B1CA1D67CDDAAA3CD20A1C0F5B0B8D1828C7334F191BC15600A5F3F36C6F196F36710A54EB3D6F87D2DC8A00F4A684C4F3A1D885300055A2DCF1E5DFBE87388E27020EB8CD297467D89876F38E1BA63B57F05D5AA830BB233247767E1

        byte[] output_data = SM2Util.getInstance().decode(privateKey, StringUtil.hexStringToByte(aa));
        System.out.println("output_data:" + StringUtil.bytesToHexString(output_data));

    }
}
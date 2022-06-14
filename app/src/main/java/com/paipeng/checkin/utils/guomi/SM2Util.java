package com.paipeng.checkin.utils.guomi;

import android.os.Build;

import androidx.annotation.RequiresApi;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithID;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.ec.custom.gm.SM2P256V1Curve;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.bouncycastle.util.Strings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * 旧标准的加密排序C1C2C3 新标准 C1C3C2，C1为65字节第1字节为压缩标识，这里固定为0x04，后面64字节为xy分量各32字节。C3为32字节。C2长度与原文一致。
 */
public class SM2Util {
    private static final String SM_EC = "EC";
    public static final SM2P256V1Curve CURVE = new SM2P256V1Curve();
    public final static BigInteger SM2_ECC_P = CURVE.getQ();
    public final static BigInteger SM2_ECC_A = CURVE.getA().toBigInteger();
    public final static BigInteger SM2_ECC_B = CURVE.getB().toBigInteger();
    public final static BigInteger SM2_ECC_N = CURVE.getOrder();
    public final static BigInteger SM2_ECC_H = CURVE.getCofactor();
    public final static BigInteger SM2_ECC_GX = new BigInteger(
            "32C4AE2C1F1981195F9904466A39C9948FE30BBFF2660BE1715A4589334C74C7", 16);
    public final static BigInteger SM2_ECC_GY = new BigInteger(
            "BC3736A2F4F6779C59BDCEE36B692153D0A9877CC62A474002DF32E52139F0A0", 16);

    public static final ECPoint G_POINT = CURVE.createPoint(SM2_ECC_GX, SM2_ECC_GY);
    public static final ECDomainParameters DOMAIN_PARAMS = new ECDomainParameters(CURVE, G_POINT,
            SM2_ECC_N, SM2_ECC_H);
    private static SM2Util INSTANCE;

    public static SM2Util getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SM2Util();
        }
        return INSTANCE;
    }

    public SM2Util() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public KeyPair generateKeyPair(ECDomainParameters domainParameters, SecureRandom random)
            throws NoSuchProviderException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(SM_EC, BouncyCastleProvider.PROVIDER_NAME);
        ECParameterSpec parameterSpec = new ECParameterSpec(domainParameters.getCurve(), domainParameters.getG(),
                domainParameters.getN(), domainParameters.getH());
        kpg.initialize(parameterSpec, random);
        return kpg.generateKeyPair();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void savePrivateKey2File(PrivateKey privateKey, File outPutFile) throws Exception {
        byte[] prks = Base64.getEncoder().encode(privateKey.getEncoded());
        FileOutputStream fos = new FileOutputStream(outPutFile);
        fos.write(prks);
        fos.flush();
        fos.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void savePublicKey2File(PublicKey publicKey, File outPutFile) throws Exception {
        byte[] prks = Base64.getEncoder().encode(publicKey.getEncoded());
        FileOutputStream fos = new FileOutputStream(outPutFile);
        fos.write(prks);
        fos.flush();
        fos.close();
    }

    public byte[] encode(BCECPublicKey publicKey, byte[] dataByte) {
        byte[] sm2Byte = null;
        try {
            ECParameterSpec parameterSpec = publicKey.getParameters();
            ECDomainParameters domainParameters = new ECDomainParameters(parameterSpec.getCurve(), parameterSpec.getG(),
                    parameterSpec.getN(), parameterSpec.getH());
            ECPublicKeyParameters ecPublicKeyParameters = new ECPublicKeyParameters(publicKey.getQ(), domainParameters);
            SM2Engine engine = new SM2Engine(SM2Engine.Mode.C1C2C3);
            ParametersWithRandom pwr = new ParametersWithRandom(ecPublicKeyParameters, new SecureRandom());
            engine.init(true, pwr);
            sm2Byte = engine.processBlock(dataByte, 0, dataByte.length);
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }

        return sm2Byte;
    }

    public byte[] decode(BCECPrivateKey privateKey, byte[] dataByte) {
        byte[] sm2Byte = null;
        try {
            ECParameterSpec parameterSpec = privateKey.getParameters();
            ECDomainParameters domainParameters = new ECDomainParameters(parameterSpec.getCurve(), parameterSpec.getG(),
                    parameterSpec.getN(), parameterSpec.getH());
            ECPrivateKeyParameters ecPrivateKeyParameters = new ECPrivateKeyParameters(privateKey.getD(), domainParameters);
            SM2Engine engine = new SM2Engine(SM2Engine.Mode.C1C2C3);
            engine.init(false, ecPrivateKeyParameters);
            sm2Byte = engine.processBlock(dataByte, 0, dataByte.length);
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }

        return sm2Byte;
    }


    public byte[] encode(ECPublicKeyParameters ecPublicKeyParameters, byte[] dataByte) {
        byte[] sm2Byte = null;
        try {
            SM2Engine engine = new SM2Engine(SM2Engine.Mode.C1C2C3);
            ParametersWithRandom pwr = new ParametersWithRandom(ecPublicKeyParameters, new SecureRandom());
            engine.init(true, pwr);
            sm2Byte = engine.processBlock(dataByte, 0, dataByte.length);
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }

        return sm2Byte;
    }


    public byte[] decode(ECPrivateKeyParameters ecPrivateKeyParameters, byte[] dataByte) {
        byte[] sm2Byte = null;
        try {
            SM2Engine engine = new SM2Engine(SM2Engine.Mode.C1C2C3);
            engine.init(false, ecPrivateKeyParameters);
            sm2Byte = engine.processBlock(dataByte, 0, dataByte.length);
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }

        return sm2Byte;
    }

    public ECPrivateKeyParameters genereateECPrivateKeyParameters(byte[] privateBytes) {
        ECDomainParameters ecDomainParameters = new ECDomainParameters(CURVE, G_POINT, SM2_ECC_N, SM2_ECC_H);
        ECPrivateKeyParameters ecPrivateKeyParameters = new ECPrivateKeyParameters(new BigInteger(privateBytes), ecDomainParameters);
        return ecPrivateKeyParameters;
    }


    public ECPublicKeyParameters genereateECPublicKeyParameters(byte[] xBytes, byte[] yBytes) {
        ECDomainParameters ecDomainParameters = new ECDomainParameters(CURVE, G_POINT, SM2_ECC_N, SM2_ECC_H);
        ECPublicKeyParameters ecPublicKeyParameters = SM2Util.createECPublicKeyParameters(xBytes, yBytes, SM2Util.CURVE, ecDomainParameters);

        return ecPublicKeyParameters;
    }


    public static int getCurveLength(ECKeyParameters ecKey) {
        return getCurveLength(ecKey.getParameters());
    }

    public static int getCurveLength(ECDomainParameters domainParams) {
        return (domainParams.getCurve().getFieldSize() + 7) / 8;
    }


    public static byte[] fixToCurveLengthBytes(int curveLength, byte[] src) {
        if (src.length == curveLength) {
            return src;
        }

        byte[] result = new byte[curveLength];
        if (src.length > curveLength) {
            System.arraycopy(src, src.length - result.length, result, 0, result.length);
        } else {
            System.arraycopy(src, 0, result, result.length - src.length, src.length);
        }
        return result;
    }

    /**
     * @param xHex             十六进制形式的公钥x分量，如果是SM2算法，Hex字符串长度应该是64（即32字节）
     * @param yHex             十六进制形式的公钥y分量，如果是SM2算法，Hex字符串长度应该是64（即32字节）
     * @param curve            EC曲线参数，一般是固定的，如果是SM2算法的可参考{@link SM2Util#CURVE}
     * @param domainParameters EC Domain参数，一般是固定的，如果是SM2算法的可参考{@link SM2Util#DOMAIN_PARAMS}
     * @return
     */
    public static ECPublicKeyParameters createECPublicKeyParameters(
            String xHex, String yHex, ECCurve curve, ECDomainParameters domainParameters) {
        return createECPublicKeyParameters(ByteUtils.fromHexString(xHex), ByteUtils.fromHexString(yHex),
                curve, domainParameters);
    }

    /**
     * @param xBytes           十六进制形式的公钥x分量，如果是SM2算法，应该是32字节
     * @param yBytes           十六进制形式的公钥y分量，如果是SM2算法，应该是32字节
     * @param curve            EC曲线参数，一般是固定的，如果是SM2算法的可参考{@link SM2Util#CURVE}
     * @param domainParameters EC Domain参数，一般是固定的，如果是SM2算法的可参考{@link SM2Util#DOMAIN_PARAMS}
     * @return
     */
    public static ECPublicKeyParameters createECPublicKeyParameters(
            byte[] xBytes, byte[] yBytes, ECCurve curve, ECDomainParameters domainParameters) {
        final byte uncompressedFlag = 0x04;
        int curveLength = getCurveLength(domainParameters);
        xBytes = fixToCurveLengthBytes(curveLength, xBytes);
        yBytes = fixToCurveLengthBytes(curveLength, yBytes);
        byte[] encodedPubKey = new byte[1 + xBytes.length + yBytes.length];
        encodedPubKey[0] = uncompressedFlag;
        System.arraycopy(xBytes, 0, encodedPubKey, 1, xBytes.length);
        System.arraycopy(yBytes, 0, encodedPubKey, 1 + xBytes.length, yBytes.length);
        return new ECPublicKeyParameters(curve.decodePoint(encodedPubKey), domainParameters);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public byte[] sign(ECPrivateKeyParameters ecPrivateKeyParameters, byte[] data, String id) {
        try {
            SM2Signer signer = new SM2Signer();
            CipherParameters param = new ParametersWithRandom(ecPrivateKeyParameters, new SecureRandom());
            ParametersWithID parametersWithID = new ParametersWithID(param, Strings.toByteArray(id));
            //ParametersWithIV parametersWithIV = new ParametersWithIV(param, Strings.toByteArray("PAIPENG"));
            signer.init(true, parametersWithID);
            signer.update(data, 0, data.length);
            return signer.generateSignature();
        } catch (CryptoException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean verifySign(ECPublicKeyParameters ecPublicKeyParameters, byte[] signature, byte[] data, String id) {
        SM2Signer signer = new SM2Signer();
        ParametersWithID parametersWithID = new ParametersWithID(ecPublicKeyParameters, Strings.toByteArray(id));
        signer.init(false, parametersWithID);
        //signer.init(false, ecPublicKeyParameters);
        signer.update(data, 0, data.length);
        return signer.verifySignature(signature);
    }
}

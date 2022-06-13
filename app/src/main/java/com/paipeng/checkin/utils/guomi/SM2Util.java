package com.paipeng.checkin.utils.guomi;

import android.os.Build;

import androidx.annotation.RequiresApi;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;

import java.io.File;
import java.io.FileOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;

public class SM2Util {
    private static final String SM_EC = "EC";

    public static KeyPair generateKeyPair(ECDomainParameters domainParameters, SecureRandom random)
            throws NoSuchProviderException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(SM_EC, BouncyCastleProvider.PROVIDER_NAME);
        ECParameterSpec parameterSpec = new ECParameterSpec(domainParameters.getCurve(), domainParameters.getG(),
                domainParameters.getN(), domainParameters.getH());
        kpg.initialize(parameterSpec, random);
        return kpg.generateKeyPair();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void savePrivateKey2File(PrivateKey privateKey, File outPutFile) throws Exception {
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

    public static byte[] encode(BCECPublicKey publicKey, byte[] dataByte) {
        byte[] sm2Byte = null;
        try {
            ECParameterSpec parameterSpec = publicKey.getParameters();
            ECDomainParameters domainParameters = new ECDomainParameters(parameterSpec.getCurve(), parameterSpec.getG(),
                    parameterSpec.getN(), parameterSpec.getH());
            ECPublicKeyParameters ecPublicKeyParameters = new ECPublicKeyParameters(publicKey.getQ(), domainParameters);
            SM2Engine engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
            ParametersWithRandom pwr = new ParametersWithRandom(ecPublicKeyParameters, new SecureRandom());
            engine.init(true, pwr);
            sm2Byte = engine.processBlock(dataByte, 0, dataByte.length);
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }

        return sm2Byte;
    }

    public static byte[] decode(BCECPrivateKey privateKey, byte[] dataByte) {
        byte[] sm2Byte = null;
        try {
            ECParameterSpec parameterSpec = privateKey.getParameters();
            ECDomainParameters domainParameters = new ECDomainParameters(parameterSpec.getCurve(), parameterSpec.getG(),
                    parameterSpec.getN(), parameterSpec.getH());
            ECPrivateKeyParameters ecPrivateKeyParameters = new ECPrivateKeyParameters(privateKey.getD(), domainParameters);
            SM2Engine engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
            engine.init(false, ecPrivateKeyParameters);
            sm2Byte = engine.processBlock(dataByte, 0, dataByte.length);
        } catch (InvalidCipherTextException e) {
            e.printStackTrace();
        }

        return sm2Byte;
    }

}

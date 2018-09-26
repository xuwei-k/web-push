package nl.martijndwars.webpush;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.BigIntegers;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

import static org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME;

public class Utils {
    public static final String CURVE = "prime256v1";
    public static final String ALGORITHM = "ECDH";

    /**
     * Get the uncompressed encoding of the public key point. The resulting array
     * should be 65 bytes length and start with 0x04 followed by the x and y
     * coordinates (32 bytes each).
     *
     * @param publicKey
     * @return
     */
    public static byte[] savePublicKey(ECPublicKey publicKey) {
        return publicKey.getQ().getEncoded(false);
    }

    public static byte[] savePrivateKey(ECPrivateKey privateKey) {
        return privateKey.getD().toByteArray();
    }

    /**
     * Load the public key from a URL-safe base64 encoded string. Takes into
     * account the different encodings, including point compression.
     *
     * @param encodedPublicKey
     */
    public static PublicKey loadPublicKey(String encodedPublicKey) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] decodedPublicKey = Base64Encoder.decode(encodedPublicKey);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, PROVIDER_NAME);
        ECParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec(CURVE);
        ECCurve curve = parameterSpec.getCurve();
        ECPoint point = curve.decodePoint(decodedPublicKey);
        ECPublicKeySpec pubSpec = new ECPublicKeySpec(point, parameterSpec);

        return keyFactory.generatePublic(pubSpec);
    }

    /**
     * Load the private key from a URL-safe base64 encoded string
     *
     * @param encodedPrivateKey
     * @return
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static PrivateKey loadPrivateKey(String encodedPrivateKey) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] decodedPrivateKey = Base64Encoder.decode(encodedPrivateKey);
        BigInteger s = BigIntegers.fromUnsignedByteArray(decodedPrivateKey);
        ECParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec(CURVE);
        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(s, parameterSpec);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM, PROVIDER_NAME);

        return keyFactory.generatePrivate(privateKeySpec);
    }
}

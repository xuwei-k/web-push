package nl.martijndwars.webpush;


import org.apache.commons.codec.binary.Base64;

/**
 * Java 7 compatible Base64 encode/decode functions. Based on Apache Commons Codec.
 *
 * <p>
 * Note: Once upgrading to Java 8+, replace by native Base64 encoder.
 * </p>
 */
public class Base64Encoder {

    public static byte[] decode(String base64Encoded) {
        return Base64.decodeBase64(base64Encoded);
    }

    public static String encodeWithoutPadding(byte[] bytes) {
        return unpad(Base64.encodeBase64String(bytes));
    }

    public static String encodeUrl(byte[] bytes) {
        return pad(Base64.encodeBase64URLSafeString(bytes));
    }

    public static String encodeUrlWithoutPadding(byte[] bytes) {
        return Base64.encodeBase64URLSafeString(bytes);
    }

    private static String pad(String base64Encoded) {
        int m = base64Encoded.length() % 4;
        if (m == 2) {
            return base64Encoded + "==";
        } else if (m == 3) {
            return base64Encoded + "=";
        } else {
            return base64Encoded;
        }
    }

    private static String unpad(String base64Encoded) {
        if (base64Encoded.endsWith("==")) {
            return base64Encoded.substring(0, base64Encoded.length() - 2);
        } else if (base64Encoded.endsWith("=")) {
            return base64Encoded.substring(0, base64Encoded.length() - 1);
        } else {
            return base64Encoded;
        }
    }
}

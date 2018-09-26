package nl.martijndwars.webpush;

import org.junit.jupiter.api.Test;

import static com.google.common.io.BaseEncoding.base64;
import static com.google.common.io.BaseEncoding.base64Url;
import static java.nio.charset.StandardCharsets.UTF_8;
import static nl.martijndwars.webpush.Base64Encoder.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Base64EncoderTest {

    @Test
    void decodeTest() {
        // first compare with previous guava implementation, make sure non-breaking changes
        assertEquals(new String(base64().decode("")), new String(decode("")));
        assertEquals(new String(base64().decode("dw")), new String(decode("dw")));
        assertEquals(new String(base64().decode("dw==")), new String(decode("dw==")));
        assertEquals(new String(base64().decode("d2U")), new String(decode("d2U")));
        assertEquals(new String(base64().decode("d2Vi")), new String(decode("d2Vi")));
        assertEquals(new String(base64().decode("d2ViLQ")), new String(decode("d2ViLQ")));
        assertEquals(new String(base64().decode("d2ViLQ==")), new String(decode("d2ViLQ==")));
        assertEquals(new String(base64().decode("d2ViLXA")), new String(decode("d2ViLXA")));
        assertEquals(new String(base64().decode("d2ViLXA=")), new String(decode("d2ViLXA=")));
        assertEquals(new String(base64().decode("d2ViLXB1")), new String(decode("d2ViLXB1")));
        assertEquals(new String(base64().decode("d2ViLXB1cw")), new String(decode("d2ViLXB1cw")));
        assertEquals(new String(base64().decode("d2ViLXB1cw==")), new String(decode("d2ViLXB1cw==")));
        assertEquals(new String(base64().decode("d2ViLXB1c2g")), new String(decode("d2ViLXB1c2g")));
        assertEquals(new String(base64().decode("d2ViLXB1c2g=")), new String(decode("d2ViLXB1c2g=")));
        assertEquals(new String(base64().decode("d2ViLXB1c2g/")), new String(decode("d2ViLXB1c2g/")));
        assertEquals(new String(base64Url().decode("d2ViLXB1c2g_")), new String(decode("d2ViLXB1c2g_")));

        assertEquals("", new String(decode("")));
        assertEquals("w", new String(decode("dw")));
        assertEquals("w", new String(decode("dw==")));
        assertEquals("we", new String(decode("d2U")));
        assertEquals("web", new String(decode("d2Vi")));
        assertEquals("web-", new String(decode("d2ViLQ")));
        assertEquals("web-", new String(decode("d2ViLQ==")));
        assertEquals("web-p", new String(decode("d2ViLXA")));
        assertEquals("web-p", new String(decode("d2ViLXA=")));
        assertEquals("web-pu", new String(decode("d2ViLXB1")));
        assertEquals("web-pus", new String(decode("d2ViLXB1cw")));
        assertEquals("web-pus", new String(decode("d2ViLXB1cw==")));
        assertEquals("web-push", new String(decode("d2ViLXB1c2g")));
        assertEquals("web-push", new String(decode("d2ViLXB1c2g=")));
        assertEquals("web-push?", new String(decode("d2ViLXB1c2g/")));
        assertEquals("web-push?", new String(decode("d2ViLXB1c2g_")));
    }

    @Test
    void encodeWithoutPaddingTest() {
        // first verify non breaking changes after removing guava as compile dependency
        assertEquals(base64().omitPadding().encode("".getBytes()), encodeWithoutPadding("".getBytes(UTF_8)));
        assertEquals(base64().omitPadding().encode("w".getBytes()), encodeWithoutPadding("w".getBytes(UTF_8)));
        assertEquals(base64().omitPadding().encode("we".getBytes()), encodeWithoutPadding("we".getBytes(UTF_8)));
        assertEquals(base64().omitPadding().encode("web".getBytes()), encodeWithoutPadding("web".getBytes(UTF_8)));
        assertEquals(base64().omitPadding().encode("web-".getBytes()), encodeWithoutPadding("web-".getBytes(UTF_8)));
        assertEquals(base64().omitPadding().encode("web-p".getBytes()), encodeWithoutPadding("web-p".getBytes(UTF_8)));
        assertEquals(base64().omitPadding().encode("web-pu".getBytes()), encodeWithoutPadding("web-pu".getBytes(UTF_8)));
        assertEquals(base64().omitPadding().encode("web-pus".getBytes()), encodeWithoutPadding("web-pus".getBytes(UTF_8)));
        assertEquals(base64().omitPadding().encode("web-push".getBytes()), encodeWithoutPadding("web-push".getBytes(UTF_8)));
        assertEquals(base64().omitPadding().encode("web-push?".getBytes()), encodeWithoutPadding("web-push?".getBytes(UTF_8)));

        assertEquals("", encodeWithoutPadding("".getBytes(UTF_8)));
        assertEquals("dw", encodeWithoutPadding("w".getBytes(UTF_8)));
        assertEquals("d2U", encodeWithoutPadding("we".getBytes(UTF_8)));
        assertEquals("d2Vi", encodeWithoutPadding("web".getBytes(UTF_8)));
        assertEquals("d2ViLQ", encodeWithoutPadding("web-".getBytes(UTF_8)));
        assertEquals("d2ViLXA", encodeWithoutPadding("web-p".getBytes(UTF_8)));
        assertEquals("d2ViLXB1", encodeWithoutPadding("web-pu".getBytes(UTF_8)));
        assertEquals("d2ViLXB1cw", encodeWithoutPadding("web-pus".getBytes(UTF_8)));
        assertEquals("d2ViLXB1c2g", encodeWithoutPadding("web-push".getBytes(UTF_8)));
        assertEquals("d2ViLXB1c2g/", encodeWithoutPadding("web-push?".getBytes(UTF_8)));
    }

    @Test
    void encodeUrlTest() {
        // first verify non breaking changes after removing guava as compile dependency
        assertEquals(base64Url().encode("".getBytes()), encodeUrl("".getBytes(UTF_8)));
        assertEquals(base64Url().encode("w".getBytes()), encodeUrl("w".getBytes(UTF_8)));
        assertEquals(base64Url().encode("we".getBytes()), encodeUrl("we".getBytes(UTF_8)));
        assertEquals(base64Url().encode("web".getBytes()), encodeUrl("web".getBytes(UTF_8)));
        assertEquals(base64Url().encode("web-".getBytes()), encodeUrl("web-".getBytes(UTF_8)));
        assertEquals(base64Url().encode("web-p".getBytes()), encodeUrl("web-p".getBytes(UTF_8)));
        assertEquals(base64Url().encode("web-pu".getBytes()), encodeUrl("web-pu".getBytes(UTF_8)));
        assertEquals(base64Url().encode("web-pus".getBytes()), encodeUrl("web-pus".getBytes(UTF_8)));
        assertEquals(base64Url().encode("web-push".getBytes()), encodeUrl("web-push".getBytes(UTF_8)));
        assertEquals(base64Url().encode("web-push?".getBytes()), encodeUrl("web-push?".getBytes(UTF_8)));

        assertEquals("", encodeUrl("".getBytes(UTF_8)));
        assertEquals("dw==", encodeUrl("w".getBytes(UTF_8)));
        assertEquals("d2U=", encodeUrl("we".getBytes(UTF_8)));
        assertEquals("d2Vi", encodeUrl("web".getBytes(UTF_8)));
        assertEquals("d2ViLQ==", encodeUrl("web-".getBytes(UTF_8)));
        assertEquals("d2ViLXA=", encodeUrl("web-p".getBytes(UTF_8)));
        assertEquals("d2ViLXB1", encodeUrl("web-pu".getBytes(UTF_8)));
        assertEquals("d2ViLXB1cw==", encodeUrl("web-pus".getBytes(UTF_8)));
        assertEquals("d2ViLXB1c2g=", encodeUrl("web-push".getBytes(UTF_8)));
        assertEquals("d2ViLXB1c2g_", encodeUrl("web-push?".getBytes(UTF_8)));
    }

    @Test
    void encodeUrlWithoutPaddingTest() {
        // first verify non breaking changes after removing guava as compile dependency
        assertEquals(base64Url().omitPadding().encode("".getBytes()), encodeUrlWithoutPadding("".getBytes(UTF_8)));
        assertEquals(base64Url().omitPadding().encode("w".getBytes()), encodeUrlWithoutPadding("w".getBytes(UTF_8)));
        assertEquals(base64Url().omitPadding().encode("we".getBytes()), encodeUrlWithoutPadding("we".getBytes(UTF_8)));
        assertEquals(base64Url().omitPadding().encode("web".getBytes()), encodeUrlWithoutPadding("web".getBytes(UTF_8)));
        assertEquals(base64Url().omitPadding().encode("web-".getBytes()), encodeUrlWithoutPadding("web-".getBytes(UTF_8)));
        assertEquals(base64Url().omitPadding().encode("web-p".getBytes()), encodeUrlWithoutPadding("web-p".getBytes(UTF_8)));
        assertEquals(base64Url().omitPadding().encode("web-pu".getBytes()), encodeUrlWithoutPadding("web-pu".getBytes(UTF_8)));
        assertEquals(base64Url().omitPadding().encode("web-pus".getBytes()), encodeUrlWithoutPadding("web-pus".getBytes(UTF_8)));
        assertEquals(base64Url().omitPadding().encode("web-push".getBytes()), encodeUrlWithoutPadding("web-push".getBytes(UTF_8)));
        assertEquals(base64Url().omitPadding().encode("web-push?".getBytes()), encodeUrlWithoutPadding("web-push?".getBytes(UTF_8)));

        assertEquals("", encodeUrlWithoutPadding("".getBytes(UTF_8)));
        assertEquals("dw", encodeUrlWithoutPadding("w".getBytes(UTF_8)));
        assertEquals("d2U", encodeUrlWithoutPadding("we".getBytes(UTF_8)));
        assertEquals("d2Vi", encodeUrlWithoutPadding("web".getBytes(UTF_8)));
        assertEquals("d2ViLQ", encodeUrlWithoutPadding("web-".getBytes(UTF_8)));
        assertEquals("d2ViLXA", encodeUrlWithoutPadding("web-p".getBytes(UTF_8)));
        assertEquals("d2ViLXB1", encodeUrlWithoutPadding("web-pu".getBytes(UTF_8)));
        assertEquals("d2ViLXB1cw", encodeUrlWithoutPadding("web-pus".getBytes(UTF_8)));
        assertEquals("d2ViLXB1c2g", encodeUrlWithoutPadding("web-push".getBytes(UTF_8)));
        assertEquals("d2ViLXB1c2g_", encodeUrlWithoutPadding("web-push?".getBytes(UTF_8)));
    }
}
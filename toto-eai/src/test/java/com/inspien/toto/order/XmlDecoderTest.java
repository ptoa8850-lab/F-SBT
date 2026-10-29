package com.inspien.toto.order;

import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XmlDecoderTest {

    private static final String TEXT = "<NAME>김철수</NAME>";

    @Test
    void UTF8은_그대로_읽는다() {
        assertEquals(TEXT, XmlDecoder.decode(TEXT.getBytes(StandardCharsets.UTF_8), null));
    }

    @Test
    void EUCKR은_자동으로_판별한다() {
        assertEquals(TEXT, XmlDecoder.decode(TEXT.getBytes(Charset.forName("EUC-KR")), null));
    }

    @Test
    void 헤더에_charset이_있으면_그것을_따른다() {
        assertEquals(TEXT, XmlDecoder.decode(TEXT.getBytes(Charset.forName("EUC-KR")), "EUC-KR"));
    }
}

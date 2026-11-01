package com.inspien.toto.order;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/**
 * 요청 본문을 문자열로 변환한다. 헤더에 charset이 있으면 그것을 쓰고,
 * 없으면 UTF-8로 읽어 보고 실패하면 EUC-KR로 읽는다.
 */
public final class XmlDecoder {

    private XmlDecoder() {
    }

    public static String decode(byte[] body, String declaredCharset) {
        if (declaredCharset != null && !declaredCharset.isBlank()) {
            return new String(body, Charset.forName(declaredCharset));
        }
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(body)).toString();
        } catch (CharacterCodingException e) {
            return new String(body, Charset.forName("EUC-KR"));
        }
    }
}

package com.inspien.toto.order;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/**
 * 요청 본문(byte[])을 글자로 바꾼다.
 * 샘플 데이터가 EUC-KR 이므로 인코딩을 자동으로 맞춘다.
 *  1) 요청 헤더에 charset 이 명시되어 있으면 그것을 사용
 *  2) 없으면 UTF-8 로 엄격하게 읽어보고, 깨지면 EUC-KR 로 읽는다
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

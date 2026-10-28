package com.inspien.toto.crypto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AesUtilTest {

    private static final String PHONE = "010-1234-5678";

    @Test
    void 암호화한_값을_같은_전화번호로_복호화하면_원문이_나온다() {
        String plain = "{\"host\":\"db.example.com\",\"user\":\"홍길동\"}";
        String enc = AesUtil.encrypt(plain, PHONE);
        assertEquals(plain, AesUtil.decrypt(enc, PHONE));
    }

    @Test
    void 키는_항상_16바이트() {
        assertEquals(16, AesUtil.makeKey(PHONE).getEncoded().length);
    }

    @Test
    void 다른_전화번호로는_복호화에_실패한다() {
        String enc = AesUtil.encrypt("secret", PHONE);
        assertThrows(IllegalStateException.class, () -> AesUtil.decrypt(enc, "010-0000-0000"));
    }
}

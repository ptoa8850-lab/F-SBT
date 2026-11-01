package com.inspien.toto.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/** 접속 정보 복호화. 키는 전화번호의 SHA-1 해시 앞 16바이트이고 AES/ECB/PKCS5Padding을 사용한다. */
public final class AesUtil {

    private AesUtil() {
    }

    public static SecretKeySpec makeKey(String phoneNumber) {
        try {
            byte[] sha1 = MessageDigest.getInstance("SHA-1")
                    .digest(phoneNumber.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(Arrays.copyOf(sha1, 16), "AES");
        } catch (Exception e) {
            throw new IllegalStateException("AES 키 생성 실패", e);
        }
    }

    public static String decrypt(String base64CipherText, String phoneNumber) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, makeKey(phoneNumber));
            byte[] plain = cipher.doFinal(Base64.getDecoder().decode(base64CipherText));
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("복호화 실패 (전화번호가 요청 때 보낸 값과 같은지 확인)", e);
        }
    }

    /** 테스트용 */
    public static String encrypt(String plainText, String phoneNumber) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, makeKey(phoneNumber));
            return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("암호화 실패", e);
        }
    }
}

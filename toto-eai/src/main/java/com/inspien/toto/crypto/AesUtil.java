package com.inspien.toto.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * 과제 슬라이드 5의 "접속 정보 복호화 가이드" 구현.
 *
 * 1) 전화번호 문자열 -> UTF-8 바이트
 * 2) SHA-1 해시 (20바이트)
 * 3) 앞 16바이트만 잘라 AES-128 키로 사용
 * 4) 응답 문자열은 Base64 -> AES/ECB/PKCS5Padding 복호화
 */
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

    /** 테스트용: 암호화 (서버가 하는 일을 흉내 냄) */
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

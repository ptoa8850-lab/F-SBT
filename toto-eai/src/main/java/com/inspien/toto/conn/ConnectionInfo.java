package com.inspien.toto.conn;

import java.util.Map;

/**
 * 접속 정보 조회 API 의 (복호화 완료된) 결과.
 *
 * 각 *Conn 은 "키 -> 값" 형태입니다. 정확한 키 이름(host, port, user 등)은
 * 실제 응답을 한 번 받아보면 알 수 있어서, 우선 Map 으로 들고 있다가 확인 후 전용 클래스로 바꿉니다.
 */
public record ConnectionInfo(
        String applicantKey,
        Map<String, String> orderDb,
        Map<String, String> shipmentDb,
        Map<String, String> ftp,
        String sampleXml
) {
}

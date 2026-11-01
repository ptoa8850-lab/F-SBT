package com.inspien.toto.conn;

import java.util.Map;

/** 접속 정보 조회 API의 복호화된 응답. 각 *Conn 값은 키-값 Map이다. */
public record ConnectionInfo(
        String applicantKey,
        Map<String, String> orderDb,
        Map<String, String> shipmentDb,
        Map<String, String> ftp,
        String sampleXml
) {
}

package com.inspien.toto.ftp;

import java.util.Map;

/**
 * 접속 정보 API 의 FTP_CONN(복호화 후)을 FTP 접속 정보로 바꾼다.
 * 키 이름을 아직 모르므로 흔한 이름 후보를 대소문자/밑줄 무시하고 찾는다. 못 찾으면 있는 키 목록(값 제외)을 알려준다.
 */
public final class FtpResolver {

    public record Target(String host, int port, String username, String password, String directory) {
    }

    private FtpResolver() {
    }

    public static Target resolve(Map<String, String> conn) {
        String protocol = find(conn, "protocol", "type", "scheme");
        if (protocol != null && protocol.toLowerCase().contains("sftp")) {
            throw new IllegalStateException("FTP_CONN 이 SFTP 입니다. 현재 구현은 일반 FTP 용입니다 (SFTP 라이브러리로 교체 필요)");
        }
        String host = find(conn, "host", "hostname", "ip", "server", "address", "ftphost");
        String port = find(conn, "port", "ftpport");
        String user = find(conn, "username", "user", "userid", "id", "account");
        String pw = find(conn, "password", "pw", "pwd", "passwd");
        String dir = find(conn, "directory", "dir", "path", "remotepath", "remotedir", "folder", "ftppath", "uploadpath");

        if (host == null) {
            throw fail(conn, "host(서버 주소)");
        }
        if (user == null) {
            throw fail(conn, "username");
        }
        if (pw == null) {
            throw fail(conn, "password");
        }
        int portNo = 21;
        if (port != null) {
            try {
                portNo = Integer.parseInt(port.trim());
            } catch (NumberFormatException e) {
                throw new IllegalStateException("FTP port 값이 숫자가 아닙니다: " + port);
            }
        }
        if (portNo == 22) {
            throw new IllegalStateException("FTP 포트가 22 입니다 -> SFTP 일 가능성이 큽니다 (현재 구현은 일반 FTP)");
        }
        return new Target(host, portNo, user, pw, dir);
    }

    private static String find(Map<String, String> conn, String... candidates) {
        for (String cand : candidates) {
            for (Map.Entry<String, String> e : conn.entrySet()) {
                if (normalize(e.getKey()).equals(cand) && e.getValue() != null && !e.getValue().isBlank()) {
                    return e.getValue().trim();
                }
            }
        }
        return null;
    }

    private static String normalize(String key) {
        return key.toLowerCase().replace("_", "").replace("-", "").replace(" ", "");
    }

    private static IllegalStateException fail(Map<String, String> conn, String missing) {
        return new IllegalStateException("FTP 접속 정보에서 " + missing + " 를 찾지 못했습니다. 응답에 있는 키 = "
                + conn.keySet() + " (FtpResolver 에 이 키 이름을 추가하세요)");
    }
}

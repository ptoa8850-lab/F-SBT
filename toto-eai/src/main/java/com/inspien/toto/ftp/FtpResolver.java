package com.inspien.toto.ftp;

import java.util.Map;

/**
 * FTP 접속 정보 변환. 응답 키는 URL, PORT, ID, PASSWORD, PATH이고,
 * URL에는 ftp:// 나 포트, 경로가 붙어 있을 수 있다.
 */
public final class FtpResolver {

    public record Target(String host, int port, String username, String password, String directory) {
    }

    private FtpResolver() {
    }

    public static Target resolve(Map<String, String> conn) {
        String protocol = find(conn, "protocol", "type", "scheme");
        if (protocol != null && protocol.toLowerCase().contains("sftp")) {
            throw sftp("protocol=" + protocol);
        }

        String rawHost = find(conn, "url", "host", "hostname", "ip", "server", "address", "ftphost");
        String port = find(conn, "port", "ftpport");
        String user = find(conn, "id", "username", "user", "userid", "account");
        String pw = find(conn, "password", "pw", "pwd", "passwd");
        String dir = find(conn, "path", "directory", "dir", "remotepath", "remotedir", "folder", "ftppath", "uploadpath");

        if (rawHost == null) {
            throw fail(conn, "URL(서버 주소)");
        }
        if (user == null) {
            throw fail(conn, "ID(사용자)");
        }
        if (pw == null) {
            throw fail(conn, "PASSWORD");
        }

        // URL에서 호스트만 분리한다
        String host = rawHost;
        String portFromUrl = null;
        int scheme = host.indexOf("://");
        if (scheme >= 0) {
            String s = host.substring(0, scheme).toLowerCase();
            if (s.contains("sftp") || s.equals("ssh")) {
                throw sftp("URL=" + rawHost);
            }
            host = host.substring(scheme + 3);
        }
        int slash = host.indexOf('/');
        if (slash >= 0) {
            if (dir == null) {
                dir = host.substring(slash);   // PATH가 없으면 URL 뒤의 경로를 사용한다
            }
            host = host.substring(0, slash);
        }
        int colon = host.lastIndexOf(':');
        if (colon >= 0) {
            portFromUrl = host.substring(colon + 1);
            host = host.substring(0, colon);
        }

        int portNo = 21;
        String portText = port != null ? port : portFromUrl;
        if (portText != null) {
            try {
                portNo = Integer.parseInt(portText.trim());
            } catch (NumberFormatException e) {
                throw new IllegalStateException("FTP port 값이 숫자가 아닙니다: " + portText);
            }
        }
        if (portNo == 22) {
            throw sftp("PORT=22");
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

    private static IllegalStateException sftp(String why) {
        return new IllegalStateException("SFTP 로 보입니다 (" + why + "). 현재 구현은 일반 FTP 용이라 SFTP 라이브러리로 교체가 필요합니다");
    }

    private static IllegalStateException fail(Map<String, String> conn, String missing) {
        return new IllegalStateException("FTP 접속 정보에서 " + missing + " 를 찾지 못했습니다. 응답에 있는 키 = "
                + conn.keySet());
    }
}

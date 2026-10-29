package com.inspien.toto.db;

import java.util.Map;
import java.util.Set;

/**
 * 접속 정보 API 에서 받은 (복호화된) 키-값 목록을 JDBC 접속 정보로 바꾼다.
 *
 * 응답에 들어있는 키 이름을 아직 모르기 때문에, 흔히 쓰이는 이름 후보를 대소문자/밑줄 무시하고 찾는다.
 * 못 찾으면 어떤 키들이 있었는지(값 제외) 알려주는 예외를 던진다 -> 그 키 이름을 보고 여기 후보만 추가하면 된다.
 */
public final class ProvidedDbResolver {

    public record Target(String url, String username, String password) {
    }

    private ProvidedDbResolver() {
    }

    public static Target resolve(Map<String, String> conn) {
        String url = find(conn, "url", "jdbcurl");
        String user = find(conn, "username", "user", "userid", "id", "account");
        String pw = find(conn, "password", "pw", "pwd", "passwd");

        if (url == null) {
            String host = find(conn, "host", "hostname", "ip", "server", "address");
            String port = find(conn, "port");
            String service = find(conn, "servicename", "service");
            String sid = find(conn, "sid");
            String db = find(conn, "database", "db", "dbname");
            if (port == null) {
                port = "1521";
            }
            if (host == null) {
                throw fail(conn, "host(서버 주소)");
            }
            if (sid != null) {
                url = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
            } else if (service != null || db != null) {
                url = "jdbc:oracle:thin:@//" + host + ":" + port + "/" + (service != null ? service : db);
            } else {
                throw fail(conn, "sid 또는 service name");
            }
        } else if (!url.startsWith("jdbc:")) {
            url = "jdbc:oracle:thin:@" + (url.startsWith("//") ? url : "//" + url);
        }

        if (user == null) {
            throw fail(conn, "username");
        }
        if (pw == null) {
            throw fail(conn, "password");
        }
        return new Target(url, user, pw);
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
        Set<String> keys = conn.keySet();
        return new IllegalStateException("접속 정보에서 " + missing + " 를 찾지 못했습니다. 응답에 있는 키 = " + keys
                + " (ProvidedDbResolver 에 이 키 이름을 추가하세요)");
    }
}

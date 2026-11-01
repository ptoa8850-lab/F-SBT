package com.inspien.toto.db;

import java.util.Map;
import java.util.Set;

/**
 * 접속 정보(키-값)를 JDBC 접속 정보로 변환한다.
 * 키 이름은 대소문자와 밑줄을 무시하고 여러 후보로 찾는다.
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
        return new IllegalStateException("접속 정보에서 " + missing + " 를 찾지 못했습니다. 응답에 있는 키 = " + keys);
    }
}

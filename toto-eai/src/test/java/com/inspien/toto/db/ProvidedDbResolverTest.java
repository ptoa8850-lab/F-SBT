package com.inspien.toto.db;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProvidedDbResolverTest {

    @Test
    void host_port_service로_URL을_만든다() {
        var t = ProvidedDbResolver.resolve(Map.of("HOST", "db.example.com", "PORT", "1522",
                "SERVICE_NAME", "ORCL", "USERNAME", "u1", "PASSWORD", "p1"));
        assertEquals("jdbc:oracle:thin:@//db.example.com:1522/ORCL", t.url());
        assertEquals("u1", t.username());
        assertEquals("p1", t.password());
    }

    @Test
    void sid면_콜론_형식이다() {
        var t = ProvidedDbResolver.resolve(Map.of("host", "h", "sid", "XE", "user", "u", "pw", "p"));
        assertEquals("jdbc:oracle:thin:@h:1521:XE", t.url());
    }

    @Test
    void url이_그대로_오면_사용한다() {
        var t = ProvidedDbResolver.resolve(Map.of("url", "jdbc:oracle:thin:@//a:1521/b", "user", "u", "password", "p"));
        assertEquals("jdbc:oracle:thin:@//a:1521/b", t.url());
    }

    @Test
    void 키를_못_찾으면_어떤_키가_있었는지_알려준다() {
        var ex = assertThrows(IllegalStateException.class,
                () -> ProvidedDbResolver.resolve(Map.of("FOO", "x")));
        assertEquals(true, ex.getMessage().contains("FOO"));
    }
}

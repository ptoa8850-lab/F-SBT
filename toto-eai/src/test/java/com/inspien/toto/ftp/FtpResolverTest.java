package com.inspien.toto.ftp;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FtpResolverTest {

    @Test
    void 실제_응답_키_URL_PORT_ID_PASSWORD_PATH() {
        var t = FtpResolver.resolve(Map.of("URL", "ftp.example.com", "PORT", "21",
                "ID", "u", "PASSWORD", "p", "PATH", "/upload"));
        assertEquals("ftp.example.com", t.host());
        assertEquals(21, t.port());
        assertEquals("u", t.username());
        assertEquals("/upload", t.directory());
    }

    @Test
    void URL에_scheme과_포트와_경로가_섞여도_호스트만_뽑는다() {
        var t = FtpResolver.resolve(Map.of("URL", "ftp://ftp.example.com:2121/in", "ID", "u", "PASSWORD", "p"));
        assertEquals("ftp.example.com", t.host());
        assertEquals(2121, t.port());
        assertEquals("/in", t.directory());
    }

    @Test
    void PORT가_있으면_URL의_포트보다_우선한다() {
        var t = FtpResolver.resolve(Map.of("URL", "h:2121", "PORT", "2200", "ID", "u", "PASSWORD", "p"));
        assertEquals(2200, t.port());
    }

    @Test
    void 포트가_없으면_21() {
        assertEquals(21, FtpResolver.resolve(Map.of("host", "h", "user", "u", "pw", "p")).port());
    }

    @Test
    void SFTP는_안내와_함께_거부한다() {
        assertThrows(IllegalStateException.class,
                () -> FtpResolver.resolve(Map.of("URL", "sftp://h", "ID", "u", "PASSWORD", "p")));
        assertThrows(IllegalStateException.class,
                () -> FtpResolver.resolve(Map.of("URL", "h", "PORT", "22", "ID", "u", "PASSWORD", "p")));
    }

    @Test
    void 키를_못_찾으면_있는_키를_알려준다() {
        var ex = assertThrows(IllegalStateException.class, () -> FtpResolver.resolve(Map.of("FOO", "x")));
        assertTrue(ex.getMessage().contains("FOO"));
    }
}

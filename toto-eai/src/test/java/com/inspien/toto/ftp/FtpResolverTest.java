package com.inspien.toto.ftp;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FtpResolverTest {

    @Test
    void 기본_키로_접속정보를_만든다() {
        var t = FtpResolver.resolve(Map.of("HOST", "ftp.example.com", "PORT", "2121",
                "USERNAME", "u", "PASSWORD", "p", "DIRECTORY", "/upload"));
        assertEquals("ftp.example.com", t.host());
        assertEquals(2121, t.port());
        assertEquals("/upload", t.directory());
    }

    @Test
    void 포트가_없으면_21() {
        assertEquals(21, FtpResolver.resolve(Map.of("host", "h", "user", "u", "pw", "p")).port());
    }

    @Test
    void SFTP는_안내와_함께_거부한다() {
        assertThrows(IllegalStateException.class,
                () -> FtpResolver.resolve(Map.of("host", "h", "user", "u", "pw", "p", "protocol", "SFTP")));
        assertThrows(IllegalStateException.class,
                () -> FtpResolver.resolve(Map.of("host", "h", "port", "22", "user", "u", "pw", "p")));
    }

    @Test
    void 키를_못_찾으면_있는_키를_알려준다() {
        var ex = assertThrows(IllegalStateException.class, () -> FtpResolver.resolve(Map.of("FOO", "x")));
        assertTrue(ex.getMessage().contains("FOO"));
    }
}

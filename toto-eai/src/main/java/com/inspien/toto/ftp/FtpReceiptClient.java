package com.inspien.toto.ftp;

import com.inspien.toto.conn.ConnectionInfo;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;

/**
 * FTP 접속 담당. 이 FTP 는 "목록 조회"와 "새 파일 업로드"만 허용된다.
 * (다운로드/덮어쓰기/이름변경/삭제 불가) -> 그래서 이 클래스에도 그런 기능은 만들지 않았다.
 */
@Component
public class FtpReceiptClient {

    private final ConnectionInfo info;

    public FtpReceiptClient(ConnectionInfo info) {
        this.info = info;
    }

    /** FTP 에 접속한 세션을 연다. try-with-resources 로 사용하면 끝날 때 자동으로 연결이 닫힌다. */
    public Session open() throws IOException {
        // 접속 정보 해석은 실제로 필요할 때 한다 (키 이름이 달라도 앱 기동은 되도록)
        FtpResolver.Target t = FtpResolver.resolve(info.ftp());

        FTPClient client = new FTPClient();
        client.setControlEncoding("UTF-8");        // 한글 파일명
        client.setAutodetectUTF8(true);
        client.setConnectTimeout(10_000);
        client.setDefaultTimeout(10_000);
        client.setDataTimeout(Duration.ofSeconds(30));
        try {
            client.connect(t.host(), t.port());
            if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
                throw new IOException("FTP 서버 연결 거부: " + client.getReplyString());
            }
            if (!client.login(t.username(), t.password())) {
                throw new IOException("FTP 로그인 실패: " + client.getReplyString());
            }
            client.enterLocalPassiveMode();            // 방화벽/공유기 뒤에서도 동작하도록 패시브 모드
            client.setFileType(FTP.BINARY_FILE_TYPE);  // 인코딩 변환 없이 바이트 그대로 전송
            if (t.directory() != null && !client.changeWorkingDirectory(t.directory())) {
                throw new IOException("FTP 디렉터리 이동 실패: " + t.directory() + " / " + client.getReplyString());
            }
            return new Session(client);
        } catch (IOException | RuntimeException e) {
            safeClose(client);
            throw e;
        }
    }

    private static void safeClose(FTPClient client) {
        try {
            if (client.isConnected()) {
                client.disconnect();
            }
        } catch (IOException ignored) {
            // 닫다가 난 오류는 무시
        }
    }

    public static class Session implements AutoCloseable {
        private final FTPClient client;

        private Session(FTPClient client) {
            this.client = client;
        }

        /** 현재 디렉터리에 같은 이름의 파일이 있는지 (목록 조회만 사용) */
        public boolean exists(String fileName) throws IOException {
            String[] names = client.listNames();
            if (names == null) {
                return false;
            }
            for (String n : names) {
                if (n.equals(fileName) || n.endsWith("/" + fileName)) {
                    return true;
                }
            }
            return false;
        }

        /** 새 파일 업로드. 같은 이름이 이미 있으면 서버가 거부할 수 있으므로 먼저 exists() 로 확인할 것 */
        public void upload(String fileName, byte[] content) throws IOException {
            boolean ok = client.storeFile(fileName, new ByteArrayInputStream(content));
            if (!ok) {
                throw new IOException("FTP 업로드 실패 (" + fileName + "): " + client.getReplyString());
            }
        }

        @Override
        public void close() {
            try {
                if (client.isConnected()) {
                    client.logout();
                }
            } catch (IOException ignored) {
                // 로그아웃 실패는 무시
            } finally {
                safeClose(client);
            }
        }
    }
}

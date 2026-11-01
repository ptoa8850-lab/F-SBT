package com.inspien.toto.ftp;

import com.inspien.toto.conn.ConnectionInfo;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/** FTP 접속. 이 서버는 목록 조회와 신규 업로드만 허용하므로 그 기능만 둔다. */
@Component
public class FtpReceiptClient {

    private static final Logger log = LoggerFactory.getLogger(FtpReceiptClient.class);

    private final ConnectionInfo info;

    public FtpReceiptClient(ConnectionInfo info) {
        this.info = info;
    }

    /** FTP에 접속한 세션을 연다. try-with-resources로 닫는다. */
    public Session open() throws IOException {
        // 접속 정보는 사용할 때 해석한다 (앱 기동에는 영향 없음)
        FtpResolver.Target t = FtpResolver.resolve(info.ftp());

        FTPClient client = new FTPClient();
        client.setControlEncoding("UTF-8");        // 한글 파일명용
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
            client.enterLocalPassiveMode();            // 패시브 모드
            client.setFileType(FTP.BINARY_FILE_TYPE);  // 바이너리 전송
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
            // 무시
        }
    }

    public static class Session implements AutoCloseable {
        private final FTPClient client;
        private Set<String> names;   // 현재 디렉터리의 파일명 (처음 한 번만 조회)

        private Session(FTPClient client) {
            this.client = client;
        }

        /** 같은 이름의 파일이 있는지 */
        public boolean exists(String fileName) throws IOException {
            if (names == null) {
                names = new HashSet<>();
                String[] list = client.listNames();
                if (list != null) {
                    for (String n : list) {
                        int slash = n.lastIndexOf('/');
                        names.add(slash >= 0 ? n.substring(slash + 1) : n);
                    }
                }
                log.info("[FTP] 현재 디렉터리 파일 {}개 확인 (예: {})", names.size(),
                        names.stream().limit(3).toList());
            }
            return names.contains(fileName);
        }

        /** 새 파일 업로드. 덮어쓰기가 안 되므로 먼저 exists()로 확인한다. */
        public void upload(String fileName, byte[] content) throws IOException {
            boolean ok = client.storeFile(fileName, new ByteArrayInputStream(content));
            if (!ok) {
                throw new IOException("FTP 업로드 실패 (" + fileName + "): " + client.getReplyString());
            }
            if (names != null) {
                names.add(fileName);
            }
        }

        @Override
        public void close() {
            try {
                if (client.isConnected()) {
                    client.logout();
                }
            } catch (IOException ignored) {
                // 무시
            } finally {
                safeClose(client);
            }
        }
    }
}

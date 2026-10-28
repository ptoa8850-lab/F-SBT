package com.inspien.toto.conn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * 앱이 뜬 직후 한 번 실행되어 접속 정보를 받아오고, 확인용 정보를 출력한다.
 * - 비밀번호 같은 값은 로그에 남기지 않고 "키 이름"만 출력한다. (이 키 이름을 알려주시면 다음 단계에서 정확히 매핑)
 * - 샘플 주문 XML 은 sample/sample-order.xml 로 저장한다. (EUC-KR 원본 그대로 UTF-8 로 변환해 저장)
 */
@Component
public class ConnectionBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ConnectionBootstrap.class);

    private final ConnectionInfoClient client;

    public ConnectionBootstrap(ConnectionInfoClient client) {
        this.client = client;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ConnectionInfo info = client.fetch();
        log.info("APPLICANT_KEY = {}", info.applicantKey());
        logKeys("ORDER_TB_CONN", info.orderDb());
        logKeys("SHIPMENT_TB_CONN", info.shipmentDb());
        logKeys("FTP_CONN", info.ftp());

        if (info.sampleXml() != null) {
            Path out = Path.of("sample", "sample-order.xml");
            Files.createDirectories(out.getParent());
            Files.writeString(out, info.sampleXml(), StandardCharsets.UTF_8);
            log.info("샘플 XML 저장: {}", out.toAbsolutePath());
        }
    }

    private void logKeys(String title, Map<String, String> conn) {
        log.info("{} 키 목록 = {}", title, conn.keySet());
    }
}

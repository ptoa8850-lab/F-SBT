package com.inspien.toto.conn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** 시작할 때 접속 정보와 테이블 접근 여부를 로그로 확인하고, 샘플 XML을 파일로 저장한다. */
@Component
public class ConnectionBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ConnectionBootstrap.class);

    private final ConnectionInfo info;
    private final JdbcTemplate orderJdbc;
    private final JdbcTemplate shipmentJdbc;

    public ConnectionBootstrap(ConnectionInfo info,
                               @Qualifier("orderJdbc") JdbcTemplate orderJdbc,
                               @Qualifier("shipmentJdbc") JdbcTemplate shipmentJdbc) {
        this.info = info;
        this.orderJdbc = orderJdbc;
        this.shipmentJdbc = shipmentJdbc;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("APPLICANT_KEY = {}", info.applicantKey());
        logKeys("ORDER_TB_CONN", info.orderDb());
        logKeys("SHIPMENT_TB_CONN", info.shipmentDb());
        logKeys("FTP_CONN", info.ftp());

        checkTable("ORDER_TB", orderJdbc);
        checkTable("SHIPMENT_TB", shipmentJdbc);

        if (info.sampleXml() != null) {
            Path out = Path.of("sample", "sample-order.xml");
            Files.createDirectories(out.getParent());
            Files.writeString(out, info.sampleXml(), StandardCharsets.UTF_8);
            log.info("샘플 XML 저장: {}", out.toAbsolutePath());
        }
    }

    private void checkTable(String table, JdbcTemplate jdbc) {
        try {
            Integer cnt = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM " + table + " WHERE APPLICANT_KEY = ?", Integer.class, info.applicantKey());
            log.info("{} 접근 OK (내 APPLICANT_KEY 의 행 수 = {})", table, cnt);
        } catch (Exception e) {
            // 접근에 실패해도 앱은 계속 실행하고 원인만 로그에 남긴다
            log.error("{} 접근 실패: {}", table, e.getMessage());
        }
    }

    private void logKeys(String title, Map<String, String> conn) {
        log.info("{} 키 목록 = {}", title, conn.keySet());
    }
}

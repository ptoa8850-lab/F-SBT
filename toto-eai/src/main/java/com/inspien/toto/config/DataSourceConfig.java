package com.inspien.toto.config;

import com.inspien.toto.conn.ConnectionInfo;
import com.inspien.toto.db.ProvidedDbResolver;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 주문 DB(ORDER_TB)와 배송 DB(SHIPMENT_TB) 연결을 만든다.
 * 접속 정보가 yml 이 아니라 실행 중에 정해지므로, 스프링 자동설정 대신 직접 만든다.
 * (DataSource 가 2개라서 자동설정은 어차피 동작하지 않는다)
 */
@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource orderDataSource(ConnectionInfo info, DbProperties props) {
        return create("order-pool", props, info.orderDb());
    }

    @Bean
    public DataSource shipmentDataSource(ConnectionInfo info, DbProperties props) {
        return create("shipment-pool", props, info.shipmentDb());
    }

    @Bean
    public JdbcTemplate orderJdbc(DataSource orderDataSource) {
        return new JdbcTemplate(orderDataSource);
    }

    @Bean
    public JdbcTemplate shipmentJdbc(DataSource shipmentDataSource) {
        return new JdbcTemplate(shipmentDataSource);
    }

    private DataSource create(String poolName, DbProperties props, Map<String, String> providedConn) {
        String url;
        String user;
        String password;
        if (props.isLocal()) {
            DbProperties.Local l = props.local();
            if (l == null || l.url() == null || l.url().isBlank()) {
                throw new IllegalStateException("db.mode=local 인데 db.local.url 이 비어 있습니다 (application-local.yml 확인)");
            }
            url = l.url();
            user = l.username();
            password = l.password();
        } else {
            ProvidedDbResolver.Target t = ProvidedDbResolver.resolve(providedConn);
            url = t.url();
            user = t.username();
            password = t.password();
        }
        HikariDataSource ds = new HikariDataSource();
        ds.setPoolName(poolName);
        ds.setDriverClassName("oracle.jdbc.OracleDriver");
        ds.setJdbcUrl(url);
        ds.setUsername(user);
        ds.setPassword(password);
        ds.setMaximumPoolSize(2);   // 제공 DB 는 여러 지원자가 같이 쓰므로 연결을 적게 잡는다
        return ds;
    }
}

package com.inspien.toto.config;

import com.inspien.toto.conn.ConnectionInfo;
import com.inspien.toto.db.ProvidedDbResolver;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Map;

/** 주문 DB와 배송 DB의 연결을 만든다. 접속 정보를 실행 중에 받아오기 때문에 직접 구성한다. */
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
                throw new IllegalStateException("db.mode=local 인데 db.local.url 이 비어있음 (application-local.yml 확인)");
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
        ds.setMaximumPoolSize(2);   // 공용 서버라서 풀 크기를 작게 잡는다
        return ds;
    }
}

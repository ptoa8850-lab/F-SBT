package com.inspien.toto.shipment;

import com.inspien.toto.conn.ConnectionInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** SHIPMENT_TB(운송회사 DB) 접근 전담 */
@Repository
public class ShipmentRepository {

    private final JdbcTemplate jdbc;
    private final String applicantKey;

    public ShipmentRepository(@Qualifier("shipmentJdbc") JdbcTemplate jdbc, ConnectionInfo info) {
        this.jdbc = jdbc;
        this.applicantKey = info.applicantKey();
    }

    public boolean existsByOrderId(String orderId) {
        Integer cnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM SHIPMENT_TB WHERE APPLICANT_KEY = ? AND ORDER_ID = ?",
                Integer.class, applicantKey, orderId);
        return cnt != null && cnt > 0;
    }

    public String maxShipmentId() {
        return jdbc.queryForObject(
                "SELECT MAX(SHIPMENT_ID) FROM SHIPMENT_TB WHERE APPLICANT_KEY = ?",
                String.class, applicantKey);
    }

    public void insert(String shipmentId, String orderId, String itemId, String address) {
        jdbc.update(
                "INSERT INTO SHIPMENT_TB (SHIPMENT_ID, APPLICANT_KEY, ORDER_ID, ITEM_ID, ADDRESS) VALUES (?, ?, ?, ?, ?)",
                shipmentId, applicantKey, orderId, itemId, address);
    }
}

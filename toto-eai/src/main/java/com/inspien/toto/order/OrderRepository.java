package com.inspien.toto.order;

import com.inspien.toto.conn.ConnectionInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/** ORDER_TB 접근. 모든 쿼리에 APPLICANT_KEY 조건을 포함한다. */
@Repository
public class OrderRepository {

    private final JdbcTemplate jdbc;
    private final String applicantKey;

    public OrderRepository(@Qualifier("orderJdbc") JdbcTemplate jdbc, ConnectionInfo info) {
        this.jdbc = jdbc;
        this.applicantKey = info.applicantKey();
    }

    /** 같은 USER_ID, ITEM_ID 주문이 이미 있는지 (중복 저장 방지) */
    public boolean exists(String userId, String itemId) {
        Integer cnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ORDER_TB WHERE APPLICANT_KEY = ? AND USER_ID = ? AND ITEM_ID = ?",
                Integer.class, applicantKey, userId, itemId);
        return cnt != null && cnt > 0;
    }

    /** 현재 가장 큰 ORDER_ID (없으면 null) */
    public String maxOrderId() {
        return jdbc.queryForObject(
                "SELECT MAX(ORDER_ID) FROM ORDER_TB WHERE APPLICANT_KEY = ?",
                String.class, applicantKey);
    }

    public void insert(String orderId, OrderRow r, String status) {
        jdbc.update(
                "INSERT INTO ORDER_TB (ORDER_ID, APPLICANT_KEY, USER_ID, ITEM_ID, NAME, ADDRESS, ITEM_NAME, PRICE, STATUS) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                orderId, applicantKey, r.userId(), r.itemId(), r.name(), r.address(), r.itemName(), r.price(), status);
    }

    /** 상태별 주문 목록 */
    public List<StoredOrder> findByStatus(String status) {
        return jdbc.query(
                "SELECT ORDER_ID, USER_ID, ITEM_ID, NAME, ADDRESS, ITEM_NAME, PRICE FROM ORDER_TB "
                        + "WHERE APPLICANT_KEY = ? AND STATUS = ? ORDER BY ORDER_ID",
                (rs, i) -> new StoredOrder(rs.getString("ORDER_ID"),
                        new OrderRow(rs.getString("USER_ID"), rs.getString("ITEM_ID"), rs.getString("NAME"),
                                rs.getString("ADDRESS"), rs.getString("ITEM_NAME"), rs.getString("PRICE"))),
                applicantKey, status);
    }

    public void updateStatus(String orderId, String status) {
        jdbc.update("UPDATE ORDER_TB SET STATUS = ? WHERE APPLICANT_KEY = ? AND ORDER_ID = ?",
                status, applicantKey, orderId);
    }

    /** 상태별 주문 건수 */
    public java.util.Map<String, Integer> countByStatus() {
        java.util.Map<String, Integer> result = new java.util.LinkedHashMap<>();
        jdbc.query("SELECT STATUS, COUNT(*) FROM ORDER_TB WHERE APPLICANT_KEY = ? GROUP BY STATUS ORDER BY STATUS",
                (org.springframework.jdbc.core.RowCallbackHandler) rs ->
                        result.put(String.valueOf(rs.getString(1)), rs.getInt(2)),
                applicantKey);
        return result;
    }
}

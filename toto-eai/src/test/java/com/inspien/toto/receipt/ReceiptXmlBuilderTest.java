package com.inspien.toto.receipt;

import com.inspien.toto.order.OrderRow;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReceiptXmlBuilderTest {

    @Test
    void 영수증_XML에_주문정보가_들어간다() {
        OrderRow row = new OrderRow("USER09", "ITEM45", "한지민", "서울특별시 성동구", "선글라스", "95000");
        String xml = ReceiptXmlBuilder.build("A001", row, LocalDateTime.of(2026, 10, 28, 23, 30));
        assertTrue(xml.contains("<ORDER_ID>A001</ORDER_ID>"));
        assertTrue(xml.contains("<NAME>한지민</NAME>"));
        assertTrue(xml.contains("<PRICE>95000</PRICE>"));
        assertTrue(xml.contains("2026-10-28T23:30:00"));
    }

    @Test
    void 특수문자는_안전하게_처리된다() {
        OrderRow row = new OrderRow("U", "I", "A&B", "<서울>", "x", "1");
        String xml = ReceiptXmlBuilder.build("A002", row, LocalDateTime.now());
        assertTrue(xml.contains("A&amp;B"));
        assertTrue(xml.contains("&lt;서울&gt;"));
    }

    @Test
    void 파일명에_지원자_이름이_들어간다() {
        assertEquals("홍길동_A001.xml", ReceiptXmlBuilder.fileName("홍길동", "A001"));
    }
}

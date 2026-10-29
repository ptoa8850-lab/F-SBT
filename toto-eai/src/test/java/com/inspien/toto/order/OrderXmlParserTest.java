package com.inspien.toto.order;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderXmlParserTest {

    private final OrderXmlParser parser = new OrderXmlParser();

    private ParseResult parseSample() throws Exception {
        String xml = Files.readString(Path.of("src/test/resources/sample-order.xml"), StandardCharsets.UTF_8);
        return parser.parse(xml);
    }

    @Test
    void 샘플_개수가_맞다() throws Exception {
        ParseResult r = parseSample();
        assertEquals(15, r.headerCount());
        assertEquals(70, r.itemCount());
        assertEquals(63, r.orders().size());   // 70건 - HEADER 없는 7건
        assertEquals(7, r.errors().size());    // USER16~22 의 상품
        assertEquals(4, r.warnings().size());  // 상품 없는 고객 USER12~15
    }

    @Test
    void 상품에_고객정보가_붙는다() throws Exception {
        OrderRow first = parseSample().orders().stream()
                .filter(o -> o.itemId().equals("ITEM45")).findFirst().orElseThrow();
        assertEquals("USER09", first.userId());
        assertEquals("한지민", first.name());              // USER09 의 HEADER
        assertEquals("서울특별시 성동구", first.address());
        assertEquals("선글라스", first.itemName());
        assertEquals("95000", first.price());
    }

    @Test
    void HEADER_없는_상품은_오류로_기록된다() throws Exception {
        assertTrue(parseSample().errors().stream().anyMatch(s -> s.contains("USER16") && s.contains("ITEM64")));
    }

    @Test
    void 깨진_XML은_예외() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("<ITEM><USER_ID>U1</ITEM>"));
    }
}

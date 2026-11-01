package com.inspien.toto.order;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 주문 XML 파서.
 * 샘플 XML은 루트 태그 없이 HEADER(고객)와 ITEM(상품)이 나열되어 있고 USER_ID로 서로 연결된다.
 * ROOT 태그로 감싸서 파싱한 뒤 ITEM마다 같은 USER_ID의 HEADER를 찾아 주문을 만든다.
 * 짝이 없는 데이터는 errors, 상품이 없는 고객은 warnings에 기록한다.
 */
public class OrderXmlParser {

    public ParseResult parse(String xml) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        Document doc = toDocument(xml);

        Map<String, Element> headers = new LinkedHashMap<>();
        List<Element> items = new ArrayList<>();
        int headerCount = 0;

        NodeList children = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element e = (Element) n;
            switch (e.getTagName()) {
                case "HEADER" -> {
                    headerCount++;
                    String userId = text(e, "USER_ID");
                    if (userId.isEmpty()) {
                        errors.add("HEADER " + headerCount + "번째: USER_ID 없음 -> 건너뜀");
                    } else if (headers.containsKey(userId)) {
                        errors.add("HEADER USER_ID=" + userId + ": 중복 -> 첫 번째 것만 사용");
                    } else {
                        headers.put(userId, e);
                    }
                }
                case "ITEM" -> items.add(e);
                default -> errors.add("알 수 없는 태그 <" + e.getTagName() + "> -> 건너뜀");
            }
        }

        List<OrderRow> orders = new ArrayList<>();
        Set<String> seen = new HashSet<>();      // XML 안에서의 중복 검사
        Set<String> usersWithItem = new HashSet<>();

        for (Element item : items) {
            String userId = text(item, "USER_ID");
            String itemId = text(item, "ITEM_ID");
            String label = "ITEM(USER_ID=" + userId + ", ITEM_ID=" + itemId + ")";

            if (userId.isEmpty() || itemId.isEmpty()) {
                errors.add(label + ": USER_ID 또는 ITEM_ID 없음 -> 건너뜀");
                continue;
            }
            Element header = headers.get(userId);
            if (header == null) {
                errors.add(label + ": 같은 USER_ID 의 HEADER(고객 정보) 없음 -> 건너뜀");
                continue;
            }
            if (!seen.add(userId + "|" + itemId)) {
                errors.add(label + ": XML 안에서 중복 -> 건너뜀");
                continue;
            }
            usersWithItem.add(userId);
            orders.add(new OrderRow(userId, itemId,
                    text(header, "NAME"), text(header, "ADDRESS"),
                    text(item, "ITEM_NAME"), text(item, "PRICE")));
        }

        for (String userId : headers.keySet()) {
            if (!usersWithItem.contains(userId)) {
                warnings.add("HEADER USER_ID=" + userId + ": 주문 상품(ITEM) 없음 -> 주문 생성 안 됨");
            }
        }

        return new ParseResult(headerCount, items.size(), orders, errors, warnings);
    }

    /** ROOT 태그로 감싸 DOM으로 만든다. XXE 방지를 위해 DOCTYPE은 막는다. */
    private Document toDocument(String xml) {
        try {
            String body = xml.replace("﻿", "")
                    .replaceFirst("^\\s*<\\?xml[^>]*\\?>", "");   // XML 선언 제거
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setXIncludeAware(false);
            f.setExpandEntityReferences(false);
            return f.newDocumentBuilder()
                    .parse(new InputSource(new StringReader("<ROOT>" + body + "</ROOT>")));
        } catch (Exception e) {
            throw new IllegalArgumentException("XML 형식 오류: " + e.getMessage(), e);
        }
    }

    /** 자식 태그의 값 (없으면 빈 문자열) */
    private static String text(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        return list.getLength() == 0 ? "" : list.item(0).getTextContent().trim();
    }
}

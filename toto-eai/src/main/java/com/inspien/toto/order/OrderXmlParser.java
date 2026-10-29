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
 *
 * 입력 XML 의 특징 (샘플 기준)
 *  - 맨 바깥을 감싸는 루트 태그가 없고 <HEADER>, <ITEM> 이 나열되어 있다.
 *  - <HEADER> = 고객(USER_ID, NAME, ADDRESS), <ITEM> = 상품(USER_ID, ITEM_ID, ITEM_NAME, PRICE)
 *  - ITEM 은 HEADER 안에 들어있지 않고, USER_ID 가 같은 HEADER 와 짝지어야 한다.
 *
 * 처리 방법
 *  1) 가짜 루트 <ROOT> 로 감싸서 올바른 XML 로 만든 뒤 파싱
 *  2) HEADER 를 USER_ID 로 모아두고, ITEM 마다 같은 USER_ID 의 HEADER 를 찾아 주문 1건으로 조립
 *  3) HEADER 가 없는 ITEM 등 문제 데이터는 버리지 않고 errors 에 사유와 함께 기록
 *
 * Spring 에 의존하지 않는 순수 자바 클래스라서 단독으로 테스트할 수 있다.
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
        Set<String> seen = new HashSet<>();      // 같은 XML 안의 (USER_ID, ITEM_ID) 중복 검사
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

    /** 루트가 없는 XML 을 <ROOT> 로 감싸 DOM 으로 만든다. (XXE 방지를 위해 DOCTYPE 금지) */
    private Document toDocument(String xml) {
        try {
            String body = xml.replace("﻿", "")
                    .replaceFirst("^\\s*<\\?xml[^>]*\\?>", "");   // 이미 있는 XML 선언은 제거
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

    /** 자식 태그의 글자(앞뒤 공백 제거). 없으면 빈 문자열 */
    private static String text(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        return list.getLength() == 0 ? "" : list.item(0).getTextContent().trim();
    }
}

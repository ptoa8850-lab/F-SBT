package com.inspien.toto.receipt;

import com.inspien.toto.order.OrderRow;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 영수증 XML 만들기. (회계팀이 FTP 에서 가져가는 파일)
 * 문자열을 이어붙이지 않고 DOM 으로 만들어서 &, < 같은 특수문자가 자동으로 안전하게 처리된다.
 */
public final class ReceiptXmlBuilder {

    private ReceiptXmlBuilder() {
    }

    public static String build(String orderId, OrderRow r, LocalDateTime issuedAt) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = doc.createElement("RECEIPT");
            doc.appendChild(root);
            add(doc, root, "ORDER_ID", orderId);
            add(doc, root, "USER_ID", r.userId());
            add(doc, root, "NAME", r.name());
            add(doc, root, "ADDRESS", r.address());
            add(doc, root, "ITEM_ID", r.itemId());
            add(doc, root, "ITEM_NAME", r.itemName());
            add(doc, root, "PRICE", r.price());
            add(doc, root, "ISSUED_AT", issuedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            StringWriter out = new StringWriter();
            t.transform(new DOMSource(doc), new StreamResult(out));
            return out.toString();
        } catch (Exception e) {
            throw new IllegalStateException("영수증 XML 생성 실패 (orderId=" + orderId + ")", e);
        }
    }

    /**
     * 파일명: 지원자 이름(한글) 포함이 필수 조건. 주문번호까지 넣어 파일마다 이름이 겹치지 않게 한다.
     * (FTP 는 덮어쓰기/삭제가 안 되므로 이름이 같으면 업로드가 실패한다)
     */
    public static String fileName(String applicantName, String orderId) {
        return applicantName + "_" + orderId + ".xml";
    }

    private static void add(Document doc, Element parent, String tag, String value) {
        Element e = doc.createElement(tag);
        e.setTextContent(value == null ? "" : value);
        parent.appendChild(e);
    }
}

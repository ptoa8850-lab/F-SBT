package com.inspien.toto.order;

/** DB 에 저장된 주문 1건 (부여된 ORDER_ID + 주문 내용) */
public record StoredOrder(String orderId, OrderRow row) {
}

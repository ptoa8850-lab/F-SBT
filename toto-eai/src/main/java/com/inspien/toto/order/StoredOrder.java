package com.inspien.toto.order;

/** 저장된 주문 (ORDER_ID + 주문 내용) */
public record StoredOrder(String orderId, OrderRow row) {
}

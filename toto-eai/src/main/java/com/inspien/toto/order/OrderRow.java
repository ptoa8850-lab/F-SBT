package com.inspien.toto.order;

/** HEADER와 ITEM을 USER_ID로 합친 주문 1행 */
public record OrderRow(String userId, String itemId, String name, String address,
                       String itemName, String price) {
}

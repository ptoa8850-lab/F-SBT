package com.inspien.toto.order;

/** HEADER(고객)와 ITEM(상품)을 USER_ID 로 합쳐 만든, ORDER_TB 한 행 분량의 데이터 */
public record OrderRow(String userId, String itemId, String name, String address,
                       String itemName, String price) {
}

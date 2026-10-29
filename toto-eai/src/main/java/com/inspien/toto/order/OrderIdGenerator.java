package com.inspien.toto.order;

/**
 * ORDER_ID 생성 규칙: 대문자 1자 + 숫자 3자리 (A001 ~ A999, 다음은 B001 ...).
 * 문자가 먼저, 숫자는 0 으로 채워져 있어서 문자열 비교로 "가장 큰 값"을 구할 수 있다.
 */
public final class OrderIdGenerator {

    private OrderIdGenerator() {
    }

    /** @param currentMax 현재 DB 에서 가장 큰 ORDER_ID (없으면 null) */
    public static String next(String currentMax) {
        if (currentMax == null || currentMax.isBlank()) {
            return "A001";
        }
        if (!currentMax.matches("[A-Z][0-9]{3}")) {
            throw new IllegalStateException("예상하지 못한 ORDER_ID 형식: " + currentMax);
        }
        char letter = currentMax.charAt(0);
        int num = Integer.parseInt(currentMax.substring(1)) + 1;
        if (num > 999) {
            if (letter == 'Z') {
                throw new IllegalStateException("ORDER_ID 를 더 만들 수 없습니다 (Z999 초과)");
            }
            letter++;
            num = 1;
        }
        return String.format("%c%03d", letter, num);
    }
}

package com.inspien.toto.order;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderIdGeneratorTest {

    @Test
    void 처음에는_A001() {
        assertEquals("A001", OrderIdGenerator.next(null));
    }

    @Test
    void 하나씩_증가한다() {
        assertEquals("A010", OrderIdGenerator.next("A009"));
    }

    @Test
    void A999_다음은_다음_알파벳() {
        assertEquals("B001", OrderIdGenerator.next("A999"));
    }

    @Test
    void Z999_다음은_예외() {
        assertThrows(IllegalStateException.class, () -> OrderIdGenerator.next("Z999"));
    }

    @Test
    void 형식이_이상하면_예외() {
        assertThrows(IllegalStateException.class, () -> OrderIdGenerator.next("abc"));
    }
}

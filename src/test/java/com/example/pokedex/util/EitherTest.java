
package com.example.pokedex.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class EitherTest {

    @Test
    void testLeftAndRight() {
        Either<String,Integer> left = Either.left("error");
        Either<String,Integer> right = Either.right(42);
        assertTrue(left.isLeft());
        assertTrue(right.isRight());
    }

    @Test
    void testMapAndFold() {
        Either<String,Integer> e = Either.right(10);
        Either<String,String> mapped = e.mapRight(Object::toString);
        assertEquals("10", mapped.getRight());
        String result = mapped.fold(l -> "bad", r -> "ok:" + r);
        assertEquals("ok:10", result);
    }
}

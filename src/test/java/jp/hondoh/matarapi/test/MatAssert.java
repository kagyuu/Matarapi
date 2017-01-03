/*
 * Copyright 2017 HONDOH Atsushi.
 */
package jp.hondoh.matarapi.test;

import jp.hondoh.matarapi.Matrix;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;

/**
 * Matrix assertion
 * @author atsushi
 */
public class MatAssert {

    public static void assertMatrix(Matrix expecteds, Matrix actuals, float delta) {
        assertMatrix(null, expecteds.getData(), actuals.getData(), delta);
    }

    public static void assertMatrix(float[][] expecteds, float[][] actuals, float delta) {
        assertMatrix(null, expecteds, actuals, delta);
    }

    public static void assertMatrix(String message, float[][] expecteds, float[][] actuals, float delta) {
        assertEquals(message, expecteds.length, actuals.length);

        for (int row=0; row < expecteds.length; row++) {
            Assert.assertArrayEquals(message, expecteds[row], actuals[row], delta);
        }
    }

}

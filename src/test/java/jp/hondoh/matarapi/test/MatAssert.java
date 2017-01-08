/*
 * Copyright 2017 HONDOH Atsushi.
 */
package jp.hondoh.matarapi.test;

import jp.hondoh.matarapi.IMatrix;
import jp.hondoh.matarapi.MatKernel;
import jp.hondoh.matarapi.Matrix;
import jp.hondoh.matarapi.MockMatrix;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;

/**
 * Matrix assertion
 *
 * @author atsushi
 */
public class MatAssert {

    public static void assertMatrix(IMatrix expecteds, IMatrix actuals, float delta) {
        assertMatrix(null, expecteds.getData(), actuals.getData(), delta);
    }

    public static void assertMatrix(float[][] expecteds, float[][] actuals, float delta) {
        assertMatrix(null, expecteds, actuals, delta);
    }

    public static void assertMatrix(String message, float[][] expecteds, float[][] actuals, float delta) {
        assertEquals(message, expecteds.length, actuals.length);

        for (int row = 0; row < expecteds.length; row++) {
            Assert.assertArrayEquals(message, expecteds[row], actuals[row], delta);
        }
    }

    /**
     * assert AB=E or not.
     *
     * @param a matrix
     * @param b matrix
     * @param delta allowable margin
     */
    public static void assertInvert(IMatrix a, IMatrix b, float delta) {

        IMatrix actual = new MockMatrix(a.getRowSize(), a.getColSize());
        MatKernel kernel = new MatKernel(a, b, actual) {
            @Override
            public void run() {
                matMul(0, 1, 2);
            }
        };

        kernel.execute(actual.getSize());

        actual = kernel.getMat(2);
        IMatrix expect = new Matrix(a.getRowSize(), a.getColSize()).toE();

        // System.out.println(actual);
        
        assertMatrix(expect, actual, delta);
    }
}

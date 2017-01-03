/*
 * Copyright 2017 HONDOH Atsushi.
 */
package jp.hondoh.matarapi.test;

import jp.hondoh.matarapi.MatUtil;
import jp.hondoh.matarapi.Matrix;
import org.junit.Test;

/**
 * Simple calculation test.
 *
 * @author atsushi
 */
public class MatUtilTest {

    @Test
    public void testLU() throws Exception {
        Matrix m0 = new Matrix(new float[][]{
            {2.0f, 5.0f, 7.0f, 8.0f},
            {4.0f, 13.0f, 20.0f, 25.0f},
            {8.0f, 29.0f, 50.0f, 71.0f},
            {10.0f, 34.0f, 78.0f, 98.0f},
        });
        // m0 will be over-writed to lu-matrix
        Matrix expect = m0.deepcopy();
        
        // m0 => LU
        // oreder is row order for LU dividing
        int[] order = MatUtil.lu(m0);
                
        // divide m0 that is over-writed to LU mixed structure to L and U
        Matrix l = MatUtil.l(m0);
        Matrix u = MatUtil.u(m0);
        
        // L U => mul
        Matrix mul = MatUtil.mul(l, u);
        
        // revert row order
        mul.revertRow(order);
        
//        System.out.println(l.toString());
//        System.out.println(u.toString());
//        System.out.println(mul.toString());
        
        // LU expects m0
        MatAssert.assertMatrix(expect, mul, 1.0E-6f);        
    }
}

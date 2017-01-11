/*
 * Copyright 2017 HONDOH Atsushi.
 */
package com.matarapi.test;

import com.matarapi.IMatrix;
import com.matarapi.MatKernel;
import com.matarapi.Matrix;
import com.matarapi.MockMatrix;
import static com.matarapi.test.MatAssert.assertMatrix;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static com.matarapi.test.MatAssert.assertMatrixInvert;

/**
 * LU calculation test.
 *
 * @author atsushi
 */
public class MatKernelLUTest {

    Matrix m0 = new Matrix(new float[][]{
        {1.0f, 2.0f, 3.0f},
        {4.0f, 5.0f, 6.0f},
        {7.0f, 8.0f, 9.0f}
    });
    Matrix m1 = new Matrix(new float[][]{
        {10.0f, 11.0f, 12.0f},
        {13.0f, 14.0f, 15.0f},
        {16.0f, 17.0f, 18.0f}
    });
    IMatrix m2 = new MockMatrix(3, 3);
    
    @Test
    public void mulLUTest() {

        Matrix l = m0.copyL();
        Matrix u = m0.copyU();
        IMatrix expect = new MockMatrix(3,3);
        IMatrix actual = new MockMatrix(3,3);
        
        MatKernel kernel = new MatKernel(m0, l, u, expect, actual) {

            @Override
            public void run() {
                matMul(1, 2, 3);
                matMulLU(0, 4);
            }
        };

        kernel.execute(m0.getSize());

        // running on OpenCL?
        assertTrue(kernel.isRunningCL());
        
        expect = kernel.getMat(3);
        actual = kernel.getMat(4);
        
//        System.out.println(expect);
//        System.out.println(actual);

        assertMatrix(expect, actual, 1.0E-6f);
    }
    
    @Test
    public void mulULTest() {

        Matrix l = m0.copyL();
        Matrix u = m0.copyU();
        IMatrix expect = new MockMatrix(3,3);
        IMatrix actual = new MockMatrix(3,3);
        
        MatKernel kernel = new MatKernel(m0, l, u, expect, actual) {

            @Override
            public void run() {
                matMul(2, 1, 3);
                matMulUL(0, 4);
            }
        };

        kernel.execute(m0.getSize());

        // running on OpenCL?
        assertTrue(kernel.isRunningCL());
        
        expect = kernel.getMat(3);
        actual = kernel.getMat(4);
        
//        System.out.println(expect);
//        System.out.println(actual);

        assertMatrix(expect, actual, 1.0E-6f);
    }
    
    @Test
    public void invUTest() {

        Matrix u = m0.copyU();
        IMatrix invU = new MockMatrix(3,3);
        
        MatKernel kernel = new MatKernel(m0, invU) {

            @Override
            public void run() {
                matInvU(0, 1);
            }
        };

        // !important, the arg is not rox*col. see javadoc
        kernel.execute(m0.getColSize());

        // running on OpenCL?
        assertTrue(kernel.isRunningCL());
        
        invU = kernel.getMat(1);
        
//        System.out.println(u);
//        System.out.println(invU);
        
        assertMatrixInvert(u, invU, 1.0E-6f);
    }
    
    @Test
    public void invLTest() {

        Matrix l = m0.copyL();
        IMatrix invL = new MockMatrix(3,3);
        
        MatKernel kernel = new MatKernel(m0, invL) {

            @Override
            public void run() {
                matInvL(0, 1);
            }
        };

        // !important, the arg is not rox*col. see javadoc
        kernel.execute(m0.getColSize());

        // running on OpenCL?
        assertTrue(kernel.isRunningCL());
        
        invL = kernel.getMat(1);
        
//        System.out.println(l);
//        System.out.println(invL);
        
        assertMatrixInvert(l, invL, 1.0E-6f);
    }
}

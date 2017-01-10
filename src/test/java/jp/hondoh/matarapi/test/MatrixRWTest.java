/*
 * Copyright 2017 HONDOH Atsushi.
 */
package jp.hondoh.matarapi.test;

import java.util.List;
import jp.hondoh.matarapi.Matrix;
import jp.hondoh.matarapi.MatrixJSON;
import static jp.hondoh.matarapi.test.MatAssert.assertMatrix;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * File IO Test.
 *
 * @author atsushi
 */
public class MatrixRWTest {
    
    @Test
    public void simpleWriteTest() {
        Matrix m0 = new Matrix(new float[][]{
            {1.0f, 2.0f, 3.0f},
            {4.0f, 5.0f, 6.0f},
            {7.0f, 8.0f, 9.0f}
        });
        
        String json = MatrixJSON.encode(m0);
        
        assertThat(json, is(equalTo("[[[1.0,2.0,3.0],[4.0,5.0,6.0],[7.0,8.0,9.0]]]")));
    }
    
    @Test
    public void simpleReadTest() {
        Matrix expect = new Matrix(new float[][]{
            {1.0f, 2.0f, 3.0f},
            {4.0f, 5.0f, 6.0f},
            {7.0f, 8.0f, 9.0f}
        });
        
        List<Matrix> mlist = MatrixJSON.decode("[[[1.0,2.0,3.0],[4.0,5.0,6.0],[7.0,8.0,9.0]]]");
        
        assertThat(mlist.size(), is(equalTo(1)));
        assertMatrix(expect, mlist.get(0), 0f);
    }
}

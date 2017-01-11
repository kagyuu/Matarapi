/*
 * Copyright 2017 HONDOH Atsushi.
 */
package com.matarapi;

/**
 * MockVector.
 * <pre>
 * Mock vector is used to obtain work area for Matarapi library.
 * This class only maintain size information of vector.
 * </pre>
 * @author atsushi
 */
public class MockVector extends MockMatrix {

    public MockVector(int size) {
        super(size, 1);
    }    
}

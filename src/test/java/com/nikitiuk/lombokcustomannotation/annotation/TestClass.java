package com.nikitiuk.lombokcustomannotation.annotation;

import org.apache.log4j.Logger;

@Patchable
public class TestClass {

    private static final Logger log = Logger.getLogger(TestClass.class);

    /*test field*/
    @NullListener
    private String testField;

    public TestClass() {
    }

    public String getTestField() {
        return testField;
    }
}

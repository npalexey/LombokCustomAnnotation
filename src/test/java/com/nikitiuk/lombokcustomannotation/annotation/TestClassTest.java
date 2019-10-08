package com.nikitiuk.lombokcustomannotation.annotation;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class TestClassTest {

    private static final Logger log = Logger.getLogger(TestClassTest.class);

    @Test
    void testPatching() throws NoSuchFieldException {
        TestClass testClass = new TestClass();
//        ArrayList fsd = testClass.getListOfSettedVars();

//        log.info(testClass.getListOfSettedVars());
//        testClass.setListOfSettedVars("ger");
//        log.info(testClass.getListOfSettedVars());
//        testClass.setListOfSettedVars("ger");
//        log.info(testClass.getListOfSettedVars());

        log.info(testClass.getTestField());
        for (Method method : TestClass.class.getMethods()) {
            log.info(method.toString());
        }

        for (Field field : TestClass.class.getFields()) {
            log.info(field.toString());
        }


    }
}

package com.inertia.phyzmo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilsTest {

    @Test
    public void escapedUrlString_isCorrect() {
        String testStrings[] = {"['testitem', \"test&item2\"]", " \"&[]", "test", ""};
        String expectedStrings[] = {"%5B'testitem',%20'test%26item2'%5D", "%20'%26%5B%5D", "test", ""};

        for (int i = 0; i < testStrings.length; i++) {
            assertEquals(expectedStrings[i], expectedStrings[i]);
        }
    }
}
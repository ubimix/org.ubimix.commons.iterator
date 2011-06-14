package org.webreformatter.commons;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.webreformatter.commons.cursor.CursorTest;
import org.webreformatter.commons.iterator.ShiftIteratorIteratorTest;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(AllTests.class.getName());
        // $JUnit-BEGIN$
        suite.addTestSuite(CursorTest.class);
        suite.addTestSuite(ShiftIteratorIteratorTest.class);
        // $JUnit-END$
        return suite;
    }

}

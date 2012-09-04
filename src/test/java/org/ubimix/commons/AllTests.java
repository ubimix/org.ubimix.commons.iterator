package org.ubimix.commons;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ubimix.commons.cursor.CursorTest;
import org.ubimix.commons.iterator.ShiftIteratorIteratorTest;

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

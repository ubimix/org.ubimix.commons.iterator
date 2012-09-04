/**
 * 
 */
package org.ubimix.commons.iterator;

import junit.framework.TestCase;

/**
 * @author kotelnikov
 */
public class ShiftIteratorIteratorTest extends TestCase {

    public ShiftIteratorIteratorTest(String name) {
        super(name);
    }

    public void test() throws Exception {
        test("a", "b", "c", "d", "e");
    }

    private void test(final String... array) {
        int counter = 0;
        ShiftIterator<String> iterator = new ShiftIterator<String>() {
            int fPos;

            @Override
            protected String shiftItem() {
                String result = fPos < array.length ? array[fPos++] : null;
                return result;
            }
        };
        while (iterator.hasNext()) {
            String control = array[counter];
            String test = iterator.next();
            assertEquals(control, test);
            counter++;
        }
        assertEquals(array.length, counter);
    }
}

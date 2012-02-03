/**
 * 
 */
package org.webreformatter.commons.cursor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import junit.framework.TestCase;

import org.webreformatter.commons.cursor.DiffCursor.DiffCursorListener;
import org.webreformatter.commons.cursor.DiffCursor.IDiffCursorListener;
import org.webreformatter.commons.cursor.GroupCursor.GroupListener;
import org.webreformatter.commons.cursor.GroupCursor.IGroupListener;

/**
 * @author kotelnikov
 */
public class CursorTest extends TestCase {

    private static DefaultComparator<String> STRING_COMPARATOR = DefaultComparator
        .newComparator();

    /**
     * @param name
     */
    public CursorTest(String name) {
        super(name);
    }

    private <T> T[] a(T... a) {
        return a;
    }

    private <T> T[] array(T... array) {
        return array;
    }

    private String[] empty() {
        return new String[0];
    }

    protected ICursor<String, RuntimeException> newCharCursor(String value) {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < value.length(); i++) {
            list.add(value.charAt(i) + "");
        }
        IteratorBasedCursor<String, RuntimeException> iterator = new IteratorBasedCursor<String, RuntimeException>(
            list);
        return iterator;
    }

    private RangeCursor<String, RuntimeException> newRangeCursor(
        int pos,
        int count,
        String... array) {
        return new RangeCursor<String, RuntimeException>(
            new IteratorBasedCursor<String, RuntimeException>(array),
            pos,
            count);

    }

    private void test(
        ICursor<String, RuntimeException> cursor,
        String... control) throws RuntimeException {
        try {
            for (String c : control) {
                assertTrue(cursor.loadNext());
                String test = cursor.getCurrent();
                assertEquals(c, test);
            }
            assertFalse(cursor.loadNext());
        } finally {
            cursor.close();
        }
    }

    public void testConditionRangeCursor() throws Exception {
        testConditionRangeCursor("b", "e", empty(), empty());
        testConditionRangeCursor(
            "b",
            "d",
            a("a", "b", "c", "d", "e", "f"),
            a("b", "c", "d"));
    }

    private void testConditionRangeCursor(
        final String first,
        final String last,
        String[] items,
        String[] control) throws Exception {
        ComparableRangeCursor<String, RuntimeException> cursor = new ComparableRangeCursor<String, RuntimeException>(
            new IteratorBasedCursor<String, RuntimeException>(items),
            first,
            last,
            new Comparator<String>() {
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }

            });
        test(cursor, control);
    }

    public void testDiffCursor() throws Exception {
        testDiffCursor("A", "AA", "[~A][+A]", "AAA");
        testDiffCursor("AA", "A", "[~A][-A]", "AAA");
        testDiffCursor("AA", "B", "[-A][-A][+B]", "AAB");
        testDiffCursor("B", "AA", "[+A][+A][-B]", "AAB");
        testDiffCursor("", "", "", "");
        testDiffCursor("A", "", "[-A]", "A");
        testDiffCursor("", "A", "[+A]", "A");
        testDiffCursor("A", "A", "[~A]", "AA");
        testDiffCursor("ABCF", "ADEF", "[~A][-B][-C][+D][+E][~F]", "AABCDEFF");
        testDiffCursor(
            "ABCF",
            "AADEF",
            "[~A][+A][-B][-C][+D][+E][~F]",
            "AAABCDEFF");
        testDiffCursor(
            "AABCF",
            "ADEF",
            "[~A][-A][-B][-C][+D][+E][~F]",
            "AAABCDEFF");
        testDiffCursor(
            "AABCF",
            "ADEF",
            "[~A][-A][-B][-C][+D][+E][~F]",
            "AAABCDEFF");
        testDiffCursor("ACDF", "ABEF", "[~A][+B][-C][-D][+E][~F]", "AABCDEFF");
        testDiffCursor(
            "13AB",
            "2AAC",
            "[-1][+2][-3][~A][+A][-B][+C]",
            "123AAABC");
        testDiffCursor(
            "13AB",
            "24AAC",
            "[-1][+2][-3][+4][~A][+A][-B][+C]",
            "1234AAABC");
        testDiffCursor(
            "1AD",
            "1AAAAABC",
            "[~1][~A][+A][+A][+A][+A][+B][+C][-D]",
            "11AAAAAABCD");
    };

    private void testDiffCursor(
        String first,
        String second,
        String control,
        String result) {
        ICursor<String, RuntimeException> firstCursor = newCharCursor(first);
        ICursor<String, RuntimeException> secondCursor = newCharCursor(second);
        final StringBuffer buf = new StringBuffer();
        IDiffCursorListener<String, RuntimeException> listener = new DiffCursorListener<String, RuntimeException>() {
            @Override
            public void onValueAdded(String value) {
                buf.append("[+").append(value).append("]");
            }

            @Override
            public void onValueRemoved(String value) {
                buf.append("[-").append(value).append("]");
            }

            @Override
            public void onValueUpdated(String firstValue, String secondValue) {
                buf.append("[~").append(firstValue).append("]");
            }

        };
        DiffCursor<String, RuntimeException> cursor = new DiffCursor<String, RuntimeException>(
            STRING_COMPARATOR,
            firstCursor,
            secondCursor,
            listener);
        StringBuilder test = new StringBuilder();
        while (cursor.loadNext()) {
            String str = cursor.getCurrent();
            test.append(str);
        }
        cursor.close();
        assertEquals(control, buf.toString());
        assertEquals(result, test.toString());
    }

    public void testFilteringCursor() throws Exception {
        testFilteringCursor(
            "a",
            a("xyz", "abc", "123", "cde", "acd", "efg", "ade", "fdm"),
            a("abc", "acd", "ade"));

    }

    private void testFilteringCursor(
        final String prefix,
        String[] fullArray,
        String[] control) throws RuntimeException {
        FilteringCursor<String, RuntimeException> cursor = new FilteringCursor<String, RuntimeException>(
            new IteratorBasedCursor<String, RuntimeException>(fullArray)) {
            @Override
            protected boolean accept(String current) {
                return current.startsWith(prefix);
            }

        };
        test(cursor, control);
    }

    public void testGraphCursor() {
        testGraphCursor("[a][a/b][a/c]", 2, "a", "a/b", "a/c");
        testGraphCursor("[a][a/b][a/b/c]", 3, "a", "a/b", "a/b/c");
        testGraphCursor("[a]", 1, "a");
        testGraphCursor(
            "[a][a/b][a/c][a/c/X][a/c/Y][a/d]",
            3,
            "a",
            "a/b",
            "a/c",
            "a/c/X",
            "a/c/Y",
            "a/d");
    }

    private void testGraphCursor(
        String control,
        int levelNumber,
        final String... nodes) {
        final int[] closeCounter = { 0 };
        TreeCursor<String, RuntimeException> cursor = new TreeCursor<String, RuntimeException>(
            "") {
            @Override
            protected ICursor<String, RuntimeException> getChildren(
                String parent) throws RuntimeException {
                List<String> children = new ArrayList<String>();
                for (String node : nodes) {
                    if (!node.equals(parent)
                        && node.startsWith(parent)
                        && node.indexOf("/", parent.length() + 1) < 0) {
                        // Add only direct children
                        children.add(node);
                    }
                }
                if (children.isEmpty()) {
                    return null;
                }
                return new IteratorBasedCursor<String, RuntimeException>(
                    children) {
                    @Override
                    public void close() throws RuntimeException {
                        super.close();
                        closeCounter[0]++;
                    }
                };
            }
        };
        StringBuilder buf = new StringBuilder();
        while (cursor.loadNext()) {
            String str = cursor.getCurrent();
            buf.append("[");
            buf.append(str);
            buf.append("]");
        }
        cursor.close();
        assertEquals("[]" + control, buf.toString());
        assertEquals(levelNumber, closeCounter[0]);
    }

    public void testGroupCursor() {
        testGroupCursor("");
        testGroupCursor("[A:1]", "A");
        testGroupCursor("[A:2]", "A", "A");
        testGroupCursor("[A:4]", "A", "A", "A", "A");
        testGroupCursor("[A:3][B:1][A:2]", "A", "A", "A", "B", "A", "A");

        testGroupMergeCursor("");
        testGroupMergeCursor("[A:1]", "A");
        testGroupMergeCursor("[A:2]", "A", "A");
        testGroupMergeCursor("[A:4]", "AA", "AA");
        testGroupMergeCursor("[A:5][B:1][C:1]", "AAB", "AAAC");
        testGroupMergeCursor("[A:4][B:1][C:1][A:1]", "AAB", "AACA");
        testGroupMergeCursor("[A:4]", "A", "A", "A", "A");
        testGroupMergeCursor("[A:5][B:1]", "A", "AA", "AB", "A");
        testGroupMergeCursor("[A:1][B:1][C:1][D:1]", "A", "B", "C", "D");
        testGroupMergeCursor("[A:1][B:1]", "A", "B");
        testGroupMergeCursor("[A:1][B:1]", "A", "B");
        testGroupMergeCursor("[A:2][B:1][C:1]", "AB", "AC");
        testGroupMergeCursor("[A:2][B:1][C:2]", "AC", "ABC");
        testGroupMergeCursor("[A:2][B:2][C:2]", "AC", "ABC", "B");
        testGroupMergeCursor("[A:2][B:2][C:2][G:1]", "AC", "ABC", "B", "G");
        testGroupMergeCursor("[A:2][B:1][C:2]", "AC", "ABC");
        testGroupMergeCursor("[A:2][B:1][C:2]", "ABC", "AC");
        testGroupMergeCursor("[A:1][B:1][C:1]", "", "ABC");
        testGroupMergeCursor("[A:1][B:1][C:1][D:1][E:1][F:1]", "DEF", "ABC");
        testGroupMergeCursor("[A:2][B:1][C:1][D:1][E:1][F:2]", "ADEF", "ABCF");
        testGroupMergeCursor(
            "[A:2][B:1][C:2][D:1][E:1][F:2][G:1]",
            "ACDEF",
            "ABCFG");
        testGroupMergeCursor(
            "[A:2][B:2][C:3][D:1][E:2][F:2][G:2]",
            "ACDEF",
            "ABCFG",
            "BCEG");
        testGroupMergeCursor("[A:3][B:2][C:1]", "AAB", "ABC");
    }

    public void testGroupCursor(
        String control,
        ICursor<String, RuntimeException> cursor) {
        final StringBuffer buf = new StringBuffer();
        IGroupListener<String, RuntimeException> listener = new GroupListener<String, RuntimeException>() {
            int fCounter;

            @Override
            public void beginGroup(String value) throws RuntimeException {
                fCounter = 0;
            }

            @Override
            public void endGroup(String value) throws RuntimeException {
                buf.append("[" + value + ":" + fCounter + "]");
                fCounter = 0;
            }

            @Override
            public void onGroup(String value) throws RuntimeException {
                fCounter++;
            }
        };
        GroupCursor<String, RuntimeException> groupCursor = new GroupCursor<String, RuntimeException>(
            cursor,
            listener);
        while (groupCursor.loadNext()) {
        }
        groupCursor.close();
        assertEquals(control, buf.toString());
    }

    private void testGroupCursor(String control, String... values) {
        IteratorBasedCursor<String, RuntimeException> cursor = new IteratorBasedCursor<String, RuntimeException>(
            values);
        testGroupCursor(control, cursor);
    }

    private void testGroupMergeCursor(String control, String... values) {
        List<ICursor<String, RuntimeException>> iterators = new ArrayList<ICursor<String, RuntimeException>>();
        for (String value : values) {
            ICursor<String, RuntimeException> iterator = newCharCursor(value);
            iterators.add(iterator);
        }
        MergeCursor<String, RuntimeException> cursor = new MergeCursor<String, RuntimeException>(
            STRING_COMPARATOR,
            iterators);
        testGroupCursor(control, cursor);

    }

    public void testIteratorBasedCursor() throws RuntimeException {
        testIteratorBasedCursor0();
        testIteratorBasedCursor0("a");
        testIteratorBasedCursor0("a", "b", "c", "d", "e", "f");
    }

    private void testIteratorBasedCursor0(String... array)
        throws RuntimeException {
        IteratorBasedCursor<String, RuntimeException> cursor = new IteratorBasedCursor<String, RuntimeException>(
            Arrays.asList(array));
        test(cursor, array);
    }

    public void testMergeCursor() throws Exception {
        testMergeCursor(a("a", "b", "c"), a("a"), a("b"), a("c"));
        testMergeCursor(a("a", "b", "c"), a("c"), a("a"), a("b"));
        testMergeCursor(a("a", "b", "c"), a("c"), a("b"), a("a"));
        String[] a = {};
        testMergeCursor(a);
        testMergeCursor(
            a("a", "b", "c", "d", "e", "f", "g"),
            a("a", "b", "c", "d", "e", "f", "g"));
        testMergeCursor(
            a("a", "b", "c", "d", "e", "f", "g"),
            a("a"),
            a("b"),
            a("c"),
            a("d"),
            a("e"),
            a("f"),
            a("g"));
        testMergeCursor(
            a("a", "b", "c", "d", "e", "f", "g"),
            a("a", "b"),
            a("c", "d", "e"),
            a("f"),
            a("g"));
        testMergeCursor(
            a("a", "b", "c", "d", "e", "f", "g"),
            a("b", "d", "f", "g"),
            a("a", "c", "e"));
    }

    private void testMergeCursor(String[] control, String[]... cursors)
        throws RuntimeException {
        Comparator<String> comparator = new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        };
        List<ICursor<String, RuntimeException>> cursorsToMerge = new ArrayList<ICursor<String, RuntimeException>>();
        for (String[] cursor : cursors) {
            cursorsToMerge
                .add(new IteratorBasedCursor<String, RuntimeException>(cursor));
        }
        MergeCursor<String, RuntimeException> cursor = new MergeCursor<String, RuntimeException>(
            comparator,
            cursorsToMerge);
        test(cursor, control);
    }

    public void testRangeCursor() throws Exception {
        RangeCursor<String, RuntimeException> cursor = newRangeCursor(10, 10);
        test(cursor);

        cursor = newRangeCursor(0, 3, "a", "b", "c", "d", "e");
        test(cursor, "a", "b", "c");
        cursor = newRangeCursor(1, 3, "a", "b", "c", "d", "e");
        test(cursor, "b", "c", "d");
        cursor = newRangeCursor(3, 0, "a", "b", "c", "d", "e");
        test(cursor);
        cursor = newRangeCursor(3, 1, "a", "b", "c", "d", "e");
        test(cursor, "d");
        cursor = newRangeCursor(3, 5, "a", "b", "c", "d", "e");
        test(cursor, "d", "e");
        cursor = newRangeCursor(5, 1, "a", "b", "c", "d", "e");
        test(cursor);
    }

    private void testSequential(final int slice, final String... control)
        throws RuntimeException {
        SequentialCursor<String, RuntimeException> cursor = new SequentialCursor<String, RuntimeException>() {
            @Override
            protected ICursor<String, RuntimeException> loadNextCursor(
                ICursor<String, RuntimeException> cursor) {
                RangeCursor<String, RuntimeException> c = (RangeCursor<String, RuntimeException>) cursor;
                int pos = c != null ? c.getLastPos() : 0;
                if (pos >= control.length) {
                    return null;
                }
                return newRangeCursor(pos, slice, control);
            }
        };
        test(cursor, control);
    }

    private void testSequential(
        final String[][] chuncks,
        final String... control) throws RuntimeException {
        SequentialCursor<String, RuntimeException> cursor = new SequentialCursor<String, RuntimeException>() {
            int fPos;

            @Override
            protected ICursor<String, RuntimeException> loadNextCursor(
                ICursor<String, RuntimeException> cursor)
                throws RuntimeException {
                if (fPos >= chuncks.length) {
                    return null;
                }
                ICursor<String, RuntimeException> result = new IteratorBasedCursor<String, RuntimeException>(
                    Arrays.asList(chuncks[fPos++]));
                return result;
            }
        };
        test(cursor, control);
    }

    public void testSequentialCursor() throws Exception {
        testSequential(3, "a", "b", "c", "d", "e", "f", "g", "h", "i");
        testSequential(1, "a", "b", "c", "d", "e", "f", "g", "h", "i");
        testSequential(30, "a", "b", "c", "d", "e", "f", "g", "h", "i");
        testSequential(
            array(
                array("a", "b"),
                array("c", "d"),
                array("e", "f"),
                array("g", "h", "i")),
            "a",
            "b",
            "c",
            "d",
            "e",
            "f",
            "g",
            "h",
            "i");
    }

}

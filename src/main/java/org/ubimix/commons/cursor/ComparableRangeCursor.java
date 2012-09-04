package org.ubimix.commons.cursor;

import java.util.Comparator;

/**
 * This cursor is used to return a range of values from an ordered set provided
 * by an another cursor. For example if you have a list of messaged ordered by
 * dates then this cursor could be used to get messages from a specific date.
 * 
 * @author kotelnikov
 * @param <T,E>
 */
public class ComparableRangeCursor<T, E extends Exception>
    extends
    AbstractRangeCursor<T, E> {

    private Comparator<T> fComparator;

    private T fFirst;

    private boolean fIncludeFirst;

    private boolean fIncludeLast;

    private T fLast;

    public ComparableRangeCursor(
        ICursor<T, E> cursor,
        T first,
        boolean includeFirst,
        T last,
        boolean includeLast,
        Comparator<T> comparator) {
        super(cursor);
        fComparator = comparator;
        fFirst = first;
        fIncludeFirst = includeFirst;
        fLast = last;
        fIncludeLast = includeLast;
    }

    public ComparableRangeCursor(
        ICursor<T, E> cursor,
        T first,
        T last,
        Comparator<T> comparator) {
        this(cursor, first, true, last, true, comparator);
    }

    @Override
    protected boolean isAfter(int pos, ICursor<T, E> cursor) throws E {
        T current = getCurrent();
        int res = fComparator.compare(current, fLast);
        if (res > 0) {
            return true;
        }
        if (res == 0) {
            return !fIncludeLast;
        }
        return false;
    }

    @Override
    protected boolean isBefore(int pos, ICursor<T, E> cursor) throws E {
        T current = getCurrent();
        int res = fComparator.compare(fFirst, current);
        if (res < 0) {
            return false;
        }
        if (res == 0) {
            return !fIncludeFirst;
        }
        return true;
    }
}
/**
 * 
 */
package org.webreformatter.commons.cursor;

/**
 * This cursor is used to notify about begins/ends of each group of values.
 * 
 * @author kotelnikov
 */
public abstract class GroupCursor<T, E extends Exception>
    implements
    ICursor<T, E> {

    protected ICursor<T, E> fCursor;

    private T fPrevValue;

    /**
     * @param comparator
     * @param cursors
     */
    public GroupCursor(ICursor<T, E> cursor) {
        init(cursor);
    }

    protected void init(ICursor<T, E> cursor) {
        fCursor = cursor;
    }

    /**
     * This method is used to notify that beginning a new group of values with
     * the specified value.
     * 
     * @param value
     */
    protected abstract void beginGroup(T value) throws E;

    /**
     * @see org.webreformatter.commons.cursor.ICursor#close()
     */
    public void close() throws E {
        onEndIterations();
        fCursor.close();
        fCursor = null;
        fPrevValue = null;
    }

    /**
     * This method could be overloaded to re-define the comparison strategy of
     * the given values.
     * 
     * @param prev previous value
     * @param current the current value
     * @return
     * @throws E
     */
    protected boolean compare(T prev, T current) throws E {
        return prev.equals(current);
    }

    /**
     * This method is used to notify about the end of the group of values.
     * 
     * @param value the end of the group of these value.
     */
    protected abstract void endGroup(T value) throws E;

    /**
     * @see org.webreformatter.commons.cursor.ICursor#getCurrent()
     */
    public T getCurrent() throws E {
        return fCursor.getCurrent();
    }

    /**
     * @see org.webreformatter.commons.cursor.ICursor#loadNext()
     */
    public boolean loadNext() throws E {
        boolean result = false;
        if (fCursor.loadNext()) {
            result = true;
            T fCurrent = fCursor.getCurrent();
            boolean equals = fPrevValue != null
                && compare(fPrevValue, fCurrent);
            if (!equals) {
                if (fPrevValue != null) {
                    endGroup(fPrevValue);
                }
                beginGroup(fCurrent);
            }
            onGroup(fCurrent);
            fPrevValue = fCurrent;
        } else {
            onEndIterations();
        }
        return result;
    }

    private void onEndIterations() throws E {
        if (fPrevValue != null) {
            endGroup(fPrevValue);
            fPrevValue = null;
        }
    }

    /**
     * This method is called to notify that the specified cursor returns a group
     * value. Begin and the end of the group are delimited by
     * {@link #beginGroup(Object)} and {@link #endGroup(Object)} method calls.
     * This method should be overloaded in subclasses to do something useful
     * with the group.
     * 
     * @param value
     */
    protected abstract void onGroup(T value) throws E;

}

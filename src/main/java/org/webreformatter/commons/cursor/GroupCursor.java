/**
 * 
 */
package org.webreformatter.commons.cursor;

/**
 * This cursor is used to notify about begins/ends of each group of values.
 * 
 * @author kotelnikov
 */
public class GroupCursor<T, E extends Exception> implements ICursor<T, E> {

    public static class GroupListener<T, E extends Exception>
        implements
        IGroupListener<T, E> {

        public void beginGroup(T value) throws E {
        }

        public void endGroup(T value) throws E {
        }

        public void onGroup(T value) throws E {
        }

        public boolean sameGroup(T prev, T current) throws E {
            return prev == null || current == null ? prev == current : prev
                .equals(current);
        }

    }

    /**
     * @author kotelnikov
     * @param <T>
     * @param <E>
     */
    public static interface IGroupListener<T, E extends Exception> {

        /**
         * This method is used to notify that beginning a new group of values
         * with the specified value.
         * 
         * @param value
         */
        void beginGroup(T value) throws E;

        /**
         * This method is used to notify about the end of the group of values.
         * 
         * @param value the end of the group of these value.
         */
        void endGroup(T value) throws E;

        /**
         * This method is called to notify that the specified cursor returns a
         * group value. Begin and the end of the group are delimited by
         * {@link #beginGroup(Object)} and {@link #endGroup(Object)} method
         * calls. This method should be overloaded in subclasses to do something
         * useful with the group.
         * 
         * @param value
         */
        void onGroup(T value) throws E;

        /**
         * This method could be overloaded to re-define the comparison strategy
         * of the given values.
         * 
         * @param prev previous value
         * @param current the current value
         * @return
         * @throws E
         */
        boolean sameGroup(T prev, T current) throws E;

    }

    protected ICursor<T, E> fCursor;

    private IGroupListener<T, E> fListener;

    private T fPrevValue;

    /**
     * @param comparator
     * @param cursors
     */
    public GroupCursor(ICursor<T, E> cursor, IGroupListener<T, E> listener) {
        this(listener);
        init(cursor);
    }

    /**
     * @param comparator
     */
    public GroupCursor(IGroupListener<T, E> listener) {
        fListener = listener;
    }

    /**
     * @see org.webreformatter.commons.cursor.ICursor#close()
     */
    public void close() throws E {
        if (fCursor != null) {
            onEndIterations();
            fCursor.close();
            fCursor = null;
        }
        fPrevValue = null;
    }

    /**
     * @see org.webreformatter.commons.cursor.ICursor#getCurrent()
     */
    public T getCurrent() {
        return fCursor.getCurrent();
    }

    protected void init(ICursor<T, E> cursor) {
        fCursor = cursor;
    }

    /**
     * @see org.webreformatter.commons.cursor.ICursor#loadNext()
     */
    public boolean loadNext() throws E {
        boolean result = false;
        if (fCursor.loadNext()) {
            result = true;
            T currentValue = fCursor.getCurrent();
            boolean equals = fPrevValue != null
                && fListener.sameGroup(fPrevValue, currentValue);
            if (!equals) {
                if (fPrevValue != null) {
                    fListener.endGroup(fPrevValue);
                }
                fListener.beginGroup(currentValue);
            }
            fListener.onGroup(currentValue);
            fPrevValue = currentValue;
        } else {
            onEndIterations();
        }
        return result;
    }

    private void onEndIterations() throws E {
        if (fPrevValue != null) {
            fListener.endGroup(fPrevValue);
            fPrevValue = null;
        }
    }

}

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

        public boolean compare(T prev, T current) throws E {
            return prev == null || current == null ? prev == current : prev
                .equals(current);
        }

        public void endGroup(T value) throws E {
        }

        public void onGroup(T value) throws E {
        }

    }

    public static interface IGroupListener<T, E extends Exception> {

        /**
         * This method is used to notify that beginning a new group of values
         * with the specified value.
         * 
         * @param value
         */
        void beginGroup(T value) throws E;

        /**
         * This method could be overloaded to re-define the comparison strategy
         * of the given values.
         * 
         * @param prev previous value
         * @param current the current value
         * @return
         * @throws E
         */
        boolean compare(T prev, T current) throws E;

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
        onEndIterations();
        fCursor.close();
        fCursor = null;
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
            T fCurrent = fCursor.getCurrent();
            boolean equals = fPrevValue != null
                && fListener.compare(fPrevValue, fCurrent);
            if (!equals) {
                if (fPrevValue != null) {
                    fListener.endGroup(fPrevValue);
                }
                fListener.beginGroup(fCurrent);
            }
            fListener.onGroup(fCurrent);
            fPrevValue = fCurrent;
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

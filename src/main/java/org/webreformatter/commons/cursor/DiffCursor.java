/**
 * 
 */
package org.webreformatter.commons.cursor;

import java.util.Comparator;

/**
 * @author kotelnikov
 */
public abstract class DiffCursor<T, E extends Exception>
    implements
    ICursor<T, E> {

    private Comparator<T> fComparator;

    private GroupCursor<T, E> fGroupCursor = new GroupCursor<T, E>(null) {

        private T fFirstValue;

        private T fSecondValue;

        /**
         * @see org.webreformatter.commons.cursor.GroupCursor#beginGroup(java.lang.Object)
         */
        @Override
        protected void beginGroup(T value) throws E {
            fFirstValue = null;
            fSecondValue = null;
        }

        @Override
        protected boolean compare(T prev, T current) throws E {
            return fComparator.compare(prev, current) == 0;
        }

        /**
         * @see org.webreformatter.commons.cursor.GroupCursor#endGroup(java.lang.Object)
         */
        @Override
        protected void endGroup(T value) throws E {
            if (fFirstValue == null || fSecondValue == null) {
                if (fFirstValue == null) {
                    onValueAdded(fSecondValue);
                } else {
                    onValueRemoved(fFirstValue);
                }
            } else {
                onValueUpdated(fFirstValue, fSecondValue);
            }
            fFirstValue = null;
            fSecondValue = null;
        }

        /**
         * @see org.webreformatter.commons.cursor.GroupCursor#onGroup(org.webreformatter.commons.cursor.ICursor,
         *      Object)
         */
        @Override
        protected void onGroup(T value) throws E {
            if (fProvidedByFirstCursor) {
                fFirstValue = value;
            } else {
                fSecondValue = value;
            }
        }
    };

    private MergeCursor<T, E> fMergeCursor = new MergeCursor<T, E>() {

        private ICursor<T, E> fFirstCursor;

        @Override
        public void close() throws E {
            fFirstCursor = null;
            super.close();
        }

        @Override
        public void init(Comparator<T> comparator, ICursor<T, E>... cursors) {
            super.init(comparator, cursors);
            fFirstCursor = cursors[0];
        }

        @Override
        protected void setCurrentValue(ICursor<T, E> cursor) throws E {
            fProvidedByFirstCursor = cursor == fFirstCursor;
            super.setCurrentValue(cursor);
        }

    };

    private boolean fProvidedByFirstCursor;

    /**
     * @param comparator
     * @param cursors
     */
    @SuppressWarnings("unchecked")
    public DiffCursor(
        Comparator<T> comparator,
        ICursor<T, E> first,
        ICursor<T, E> second) {
        fMergeCursor.init(comparator, first, second);
        fGroupCursor.init(fMergeCursor);
        fComparator = comparator;
    }

    /**
     * @see org.webreformatter.commons.cursor.ICursor#close()
     */
    public void close() throws E {
        fGroupCursor.close();
    }

    /**
     * @see org.webreformatter.commons.cursor.ICursor#getCurrent()
     */
    public T getCurrent() throws E {
        return fGroupCursor.getCurrent();
    }

    public boolean isValueProvidedByFirstCursor() {
        return fProvidedByFirstCursor;
    }

    /**
     * @see org.webreformatter.commons.cursor.ICursor#loadNext()
     */
    public boolean loadNext() throws E {
        return fGroupCursor.loadNext();
    }

    protected abstract void onValueAdded(T value) throws E;

    protected abstract void onValueRemoved(T value) throws E;

    protected abstract void onValueUpdated(T firstValue, T secondValue)
        throws E;

}

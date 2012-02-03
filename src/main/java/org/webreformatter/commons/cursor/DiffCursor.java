/**
 * 
 */
package org.webreformatter.commons.cursor;

import java.util.Comparator;
import java.util.List;

/**
 * @author kotelnikov
 */
public class DiffCursor<T, E extends Exception> implements ICursor<T, E> {

    /**
     * @author kotelnikov
     * @param <T>
     * @param <E>
     */
    public static class DiffCursorListener<T, E extends Exception>
        implements
        IDiffCursorListener<T, E> {

        public void onValueAdded(T value) throws E {
        }

        public void onValueRemoved(T value) throws E {
        }

        public void onValueUpdated(T firstValue, T secondValue) throws E {
        }

    }

    /**
     * @author kotelnikov
     * @param <T>
     * @param <E>
     */
    public interface IDiffCursorListener<T, E extends Exception> {

        void onValueAdded(T value) throws E;

        void onValueRemoved(T value) throws E;

        void onValueUpdated(T firstValue, T secondValue) throws E;
    }

    private Comparator<T> fComparator;

    private IDiffCursorListener<T, E> fDiffCursorListener;

    private int fFirstStamp;

    private MergeCursor<T, E> fMergeCursor = new MergeCursor<T, E>() {

        private ICursor<T, E> fFirstCursor;

        private int fGroupCounter;

        private T fPrevValue;

        @Override
        public void close() throws E {
            fFirstCursor = null;
            super.close();
        }

        private void finishGroup() throws E {
            if (fGroupCounter % 2 == 1) {
                if (fProvidedByFirstCursor) {
                    fDiffCursorListener.onValueRemoved(fPrevValue);
                } else {
                    fDiffCursorListener.onValueAdded(fPrevValue);
                }
            }
        }

        @Override
        public void init(List<ICursor<T, E>> cursors) {
            super.init(cursors);
            fFirstCursor = cursors.get(0);
        }

        @Override
        protected void onEndIterations() throws E {
            finishGroup();
        }

        @Override
        protected void setCurrentCursor(ICursor<T, E> cursor) throws E {
            super.setCurrentCursor(cursor);
            T currentValue = getCurrent();
            boolean equals = fPrevValue != null
                && fComparator.compare(fPrevValue, currentValue) == 0;
            if (equals) {
                fGroupCounter++;
                if (fGroupCounter % 2 == 0) {
                    if ((cursor == fFirstCursor) != fProvidedByFirstCursor) {
                        if (fProvidedByFirstCursor) {
                            fDiffCursorListener.onValueUpdated(
                                fPrevValue,
                                currentValue);
                        } else {
                            fDiffCursorListener.onValueUpdated(
                                currentValue,
                                fPrevValue);
                        }
                    } else if (fProvidedByFirstCursor) {
                        fDiffCursorListener.onValueRemoved(fPrevValue);
                        fDiffCursorListener.onValueRemoved(currentValue);
                    } else {
                        fDiffCursorListener.onValueAdded(fPrevValue);
                        fDiffCursorListener.onValueAdded(currentValue);
                    }
                }
            } else {
                finishGroup();
                fGroupCounter = 1;
            }
            fProvidedByFirstCursor = cursor == fFirstCursor;
            fPrevValue = currentValue;
            updateCursorTimeStamps();
        }

    };

    private boolean fProvidedByFirstCursor;

    private int fSecondStamp;

    private int fStamp;

    /**
     * @param comparator
     * @param cursors
     */
    @SuppressWarnings("unchecked")
    public DiffCursor(
        final Comparator<T> comparator,
        final ICursor<T, E> first,
        final ICursor<T, E> second,
        IDiffCursorListener<T, E> listener) {
        Comparator<ICursor<T, E>> cursorComparator = new Comparator<ICursor<T, E>>() {
            public int compare(ICursor<T, E> o1, ICursor<T, E> o2) {
                T v1 = o1.getCurrent();
                T v2 = o2.getCurrent();
                int result = comparator.compare(v1, v2);
                if (result == 0) {
                    result = fFirstStamp - fSecondStamp;
                }
                return result;
            };
        };
        fMergeCursor.init(cursorComparator, first, second);
        fDiffCursorListener = listener;
        fComparator = comparator;
        updateCursorTimeStamps();
    }

    /**
     * @see org.webreformatter.commons.cursor.ICursor#close()
     */
    public void close() throws E {
        fMergeCursor.close();
    }

    /**
     * @see org.webreformatter.commons.cursor.ICursor#getCurrent()
     */
    public T getCurrent() {
        return fMergeCursor.getCurrent();
    }

    public boolean isValueProvidedByFirstCursor() {
        return fProvidedByFirstCursor;
    }

    /**
     * @see org.webreformatter.commons.cursor.ICursor#loadNext()
     */
    public boolean loadNext() throws E {
        return fMergeCursor.loadNext();
    }

    private void updateCursorTimeStamps() {
        fStamp++;
        if (fProvidedByFirstCursor) {
            fFirstStamp = fStamp;
        } else {
            fSecondStamp = fStamp;
        }
    }

}

package org.webreformatter.commons.cursor;

/**
 * This implementation of the {@link ICursor} interface loads sequentially
 * {@link ICursor} instances one after another and iterates over them. The next
 * cursor is loaded only when the previous one is finished.
 * 
 * @author kotelnikov
 * @param <T, E>
 */
public abstract class SequentialCursor<T, E extends Exception>
    implements
    ICursor<T, E> {

    private T fCurrent;

    private ICursor<T, E> fCursor;

    public void close() throws E {
        if (fCursor != null) {
            fCursor.close();
            fCursor = null;
        }
    }

    private boolean doLoadNext() throws E {
        boolean result = false;
        if (fCursor.loadNext()) {
            fCurrent = fCursor.getCurrent();
            result = true;
        } else {
            fCursor.close();
            fCursor = null;
        }
        return result;
    }

    public T getCurrent() throws E {
        return fCurrent;
    }

    public boolean loadNext() throws E {
        fCurrent = null;
        boolean result = false;
        ICursor<T, E> prevCursor = fCursor;
        if (fCursor != null) {
            result = doLoadNext();
        }
        if (!result && fCursor == null) {
            fCursor = loadNextCursor(prevCursor);
            while (fCursor != null) {
                result = doLoadNext();
                if (result) {
                    break;
                }
                fCursor = loadNextCursor(fCursor);
            }
        }
        return result;
    }

    protected abstract ICursor<T, E> loadNextCursor(ICursor<T, E> cursor)
        throws E;

    @Override
    public String toString() {
        return "SequentialCursor[" + fCurrent + ":" + fCursor + "]";
    }

}
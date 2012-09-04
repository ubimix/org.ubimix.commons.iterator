/**
 * 
 */
package org.ubimix.commons.iterator;

import org.ubimix.commons.cursor.ICursor;

/**
 * @author kotelnikov
 */
public class CursorAdapter<T, E extends Exception> extends ShiftIterator<T> {

    private ICursor<T, E> fCursor;

    /**
     * 
     */
    public CursorAdapter(ICursor<T, E> cursor) {
        fCursor = cursor;
    }

    public void close() throws E {
        fCursor.close();
    }

    private RuntimeException handleException(Throwable t) {
        if (t instanceof Error) {
            throw (Error) t;
        } else if (t instanceof RuntimeException) {
            return (RuntimeException) t;
        }
        return new RuntimeException(t);
    }

    /**
     * @see org.ubimix.commons.iterator.ShiftIterator#shiftItem()
     */
    @Override
    protected T shiftItem() {
        try {
            if (fCursor.loadNext()) {
                return fCursor.getCurrent();
            } else {
                fCursor.close();
                return null;
            }
        } catch (Throwable t) {
            throw handleException(t);
        }
    }

}

/**
 * 
 */
package org.ubimix.commons.cursor;

/**
 * This cursor filters values from an another cursor and returns only accepted
 * values (see the {@link #accept(Object)} method).
 * 
 * @author kotelnikov
 */
public abstract class FilteringCursor<T, E extends Exception>
    implements
    ICursor<T, E> {

    private T fCurrent;

    private ICursor<T, E> fCursor;

    /**
     * 
     */
    public FilteringCursor(ICursor<T, E> cursor) {
        fCursor = cursor;
    }

    protected abstract boolean accept(T current) throws E;

    /**
     * @see com.cogniumsystems.socialkiosk.gwtapp.server.utils.ICursor#close()
     */
    public void close() throws E {
        fCursor.close();
    }

    /**
     * @see com.cogniumsystems.socialkiosk.gwtapp.server.utils.ICursor#getCurrent()
     */
    public T getCurrent() {
        return fCurrent;
    }

    /**
     * @see com.cogniumsystems.socialkiosk.gwtapp.server.utils.ICursor#loadNext()
     */
    public boolean loadNext() throws E {
        fCurrent = null;
        boolean result = false;
        while (!result && fCursor.loadNext()) {
            T current = fCursor.getCurrent();
            result = accept(current);
            if (result) {
                fCurrent = current;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "FilteringCursor[" + fCurrent + ":" + fCursor + "]";
    }

}

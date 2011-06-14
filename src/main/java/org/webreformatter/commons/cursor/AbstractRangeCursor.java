/**
 * 
 */
package org.webreformatter.commons.cursor;

/**
 * This cursor selects and returns only specific sub-set (range) of elements
 * returned by another cursor. It could be useful to get elements from specific
 * positions (ex: from 10th to 20th elements). Or if the original cursor returns
 * ordered elements then this class can be used to return elements from a
 * specific range of values (ex: from 05/01/2010 to 08/02/2010).
 * 
 * @author kotelnikov
 */
public abstract class AbstractRangeCursor<T, E extends Exception>
    implements
    ICursor<T, E> {

    private T fCurrent;

    private ICursor<T, E> fCursor;

    private boolean fFinished;

    private int fPos = -1;

    /**
     * 
     */
    public AbstractRangeCursor(ICursor<T, E> cursor) {
        fCursor = cursor;
    }

    public void close() throws E {
        fCursor.close();
        fCursor = null;
        fCurrent = null;
    }

    private boolean doLoadNext() throws E {
        boolean result = fCursor.loadNext();
        if (result) {
            fPos++;
            fCurrent = fCursor.getCurrent();
        }
        return result;
    }

    public T getCurrent() throws E {
        return fCurrent;
    }

    protected abstract boolean isAfter(int pos, ICursor<T, E> cursor) throws E;

    protected abstract boolean isBefore(int pos, ICursor<T, E> cursor) throws E;

    public boolean loadNext() throws E {
        fCurrent = null;
        if (fFinished) {
            return false;
        }
        boolean result = true;
        if (fPos == -1) {
            // Skip all entries until the start position
            while (result = doLoadNext()) {
                if (result = !isBefore(fPos, fCursor)) {
                    break;
                }
            }
        } else {
            result = doLoadNext();
        }
        if (result) {
            fFinished = isAfter(fPos, fCursor);
            result &= !fFinished;
        }
        if (!result) {
            fCurrent = null;
        }
        return result;
    }

    @Override
    public String toString() {
        String name = getClass().getName();
        int idx = name.lastIndexOf(".");
        if (idx > 0) {
            name = name.substring(idx + 1);
        }
        return name + "[" + fCurrent + ":" + fCursor + "]";
    }

}

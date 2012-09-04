/**
 * 
 */
package org.ubimix.commons.cursor;

/**
 * This is a wrapper for an another cursor providing access to a range of items.
 * 
 * @author kotelnikov
 */
public class RangeCursor<T, E extends Exception>
    extends
    AbstractRangeCursor<T, E> {

    private int fCount;

    private int fStartPos;

    /**
     * 
     */
    public RangeCursor(ICursor<T, E> cursor, int startPos, int count) {
        super(cursor);
        fStartPos = startPos;
        fCount = count;
    }

    public int getCount() {
        return fCount;
    }

    public int getFirstPos() {
        return fStartPos;
    }

    public int getLastPos() {
        return fStartPos + fCount;
    }

    public int getStartPos() {
        return fStartPos;
    }

    @Override
    protected boolean isAfter(int pos, ICursor<T, E> cursor) throws E {
        return pos >= getLastPos();
    }

    @Override
    protected boolean isBefore(int pos, ICursor<T, E> cursor) throws E {
        return pos < getFirstPos();
    }

}

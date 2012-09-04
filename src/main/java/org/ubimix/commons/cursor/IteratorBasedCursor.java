package org.ubimix.commons.cursor;

import java.util.Arrays;
import java.util.Iterator;

/**
 * It is a simple implementation of the {@link ICursor} interface based on the
 * Java {@link Iterator}s.
 * 
 * @author kotelnikov
 * @param <T, E>
 */
public class IteratorBasedCursor<T, E extends Exception>
    implements
    ICursor<T, E> {

    private T fCurrent;

    private Iterator<? extends T> fIterator;

    public IteratorBasedCursor(Iterable<? extends T> collection) {
        this(collection.iterator());
    }

    public IteratorBasedCursor(Iterator<? extends T> iterator) {
        setIterator(iterator);
    }

    public IteratorBasedCursor(T... list) {
        this(Arrays.asList(list));
    }

    public void close() throws E {
        fIterator = null;
    }

    public T getCurrent() {
        return fCurrent;
    }

    public boolean loadNext() throws E {
        fCurrent = null;
        if (!fIterator.hasNext()) {
            return false;
        }
        fCurrent = fIterator.next();
        return true;
    }

    protected void setIterator(Iterator<? extends T> iterator) {
        fIterator = iterator;
        fCurrent = null;
    }

    @Override
    public String toString() {
        return "IteratorBasedCursor[" + fCurrent + "]";
    }

}
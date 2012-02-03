/**
 * 
 */
package org.webreformatter.commons.cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * This cursor allows to iterate over tree structures when each node can have
 * his own children. The method
 * 
 * @author kotelnikov
 * @param <T>
 * @param <E>
 */
public abstract class TreeCursor<T, E extends Exception>
    implements
    ICursor<T, E> {

    private T fCurrent;

    private List<ICursor<T, E>> fStack = new ArrayList<ICursor<T, E>>();

    public TreeCursor(ICursor<T, E> cursor) {
        fStack.add(cursor);
    }

    public TreeCursor(T... nodes) {
        this(new IteratorBasedCursor<T, E>(nodes));
    }

    /**
     * @see org.webreformatter.commons.cursor.ICursor#close()
     */
    public void close() throws E {
        while (!fStack.isEmpty()) {
            ICursor<T, E> cursor = fStack.remove(fStack.size() - 1);
            cursor.close();
        }
    }

    /**
     * This method a cursor over all children of the given node or
     * <code>null</code> if this node is a leaf node.
     * 
     * @param node the node for which this method should return a list of
     *        children
     * @return a cursor over all children nodes of the given node or
     *         <code>null</code> if this node is a leaf
     * @throws E
     */
    protected abstract ICursor<T, E> getChildren(T node) throws E;

    /**
     * @see org.webreformatter.commons.cursor.ICursor#getCurrent()
     */
    public T getCurrent() {
        return fCurrent;
    }

    /**
     * @see org.webreformatter.commons.cursor.ICursor#loadNext()
     */
    public boolean loadNext() throws E {
        boolean result = false;
        while (!result && !fStack.isEmpty()) {
            ICursor<T, E> cursor = fStack.get(fStack.size() - 1);
            if (cursor.loadNext()) {
                result = true;
                fCurrent = cursor.getCurrent();
                cursor = getChildren(fCurrent);
                if (cursor != null) {
                    fStack.add(cursor);
                }
            } else {
                fStack.remove(fStack.size() - 1);
                cursor.close();
            }
        }
        return result;
    }
}

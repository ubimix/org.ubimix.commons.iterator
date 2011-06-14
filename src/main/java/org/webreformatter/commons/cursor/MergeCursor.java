/**
 * 
 */
package org.webreformatter.commons.cursor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is used to merge iteration results and returns them in order.
 * <p>
 * This class can be very useful when it is required to merge iteration results
 * of many iterators over ordered sets and return them as a single ordered
 * iterator.
 * </p>
 * 
 * <pre>
 * // Example of usage:
 * ICursor<MyType> c1 = ...; // the first ordered iterator
 * ICursor<MyType> c2 = ...; // the second ordered iterator
 * Comparator<MyType> comparator = new Comparator<MyType>() {
 *     public int compare(MyType o1, MyType o2) {
 *          return o1.getTitle().compareTo(o2.getTitle());
 *     }
 * };
 * ICursor<MyType> merge = new MergeCursor<MyType>(comparator, c1, c2);
 * while (merge.loadNext()) {
 *     System.out.println(merge.getCurrent());
 * }
 * </pre>
 * 
 * @author kotelnikov
 */
/**
 * This cursor is used to merge comparable items provided by multiple cursors.
 * 
 * @author kotelnikov
 */
public class MergeCursor<T, E extends Exception> implements ICursor<T, E> {

    /**
     * @author kotelnikov
     */
    protected static class CompositeException extends Error {
        private static final long serialVersionUID = 6180134010081719253L;

        private Set<Throwable> fCause;

        public CompositeException(String msg, Set<Throwable> errors) {
            super(msg);
            fCause = errors;
        }

        public Set<Throwable> getExceptions() {
            return fCause;
        }

    }

    private enum Status {
        NOT_STARTED, STARTED, STOPPED
    }

    protected Comparator<T> fComparator;

    private T fCurrent;

    private List<ICursor<T, E>> fList = new ArrayList<ICursor<T, E>>();

    private Status fStatus = Status.NOT_STARTED;

    public MergeCursor() {

    }

    public MergeCursor(
        Comparator<T> comparator,
        Collection<ICursor<T, E>> cursors) {
        init(comparator, cursors);
    }

    /**
     * 
     */
    public MergeCursor(Comparator<T> comparator, ICursor<T, E>... cursors) {
        init(comparator, cursors);
    }

    public void close() throws E {
        endIterations();
        Set<Throwable> errors = null;
        for (ICursor<T, E> cursor : fList) {
            try {
                cursor.close();
            } catch (Throwable t) {
                if (errors == null) {
                    errors = new HashSet<Throwable>();
                }
                errors.add(t);
            }
        }
        if (errors != null) {
            fireCompositeException("Can not close MergeCursor.", errors);
        }
    }

    private void endIterations() throws E {
        if (fStatus == Status.STARTED) {
            fStatus = Status.STOPPED;
            onEndIterations();
        }
    }

    protected void fireCompositeException(String msg, Set<Throwable> errors)
        throws E {
        throw new CompositeException(msg, errors);
    }

    public T getCurrent() throws E {
        return fCurrent;
    }

    protected void init(Collection<ICursor<T, E>> cursors) {
        fList.clear();
        fCurrent = null;
        fStatus = Status.NOT_STARTED;
        fList.addAll(cursors);
    }

    public void init(Comparator<T> comparator, Collection<ICursor<T, E>> cursors) {
        fComparator = comparator;
        init(cursors);
    }

    public void init(Comparator<T> comparator, ICursor<T, E>... cursors) {
        fComparator = comparator;
        init(Arrays.asList(cursors));
    }

    private void insert(List<ICursor<T, E>> list, ICursor<T, E> cursor)
        throws E {
        int pos = searchPosition(list, cursor);
        if (pos < 0) {
            pos = -(pos + 1);
        }
        list.add(pos, cursor);
    }

    public boolean loadNext() throws E {
        if (fStatus == Status.NOT_STARTED) {
            fStatus = Status.STARTED;
            ArrayList<ICursor<T, E>> list = new ArrayList<ICursor<T, E>>();
            for (int i = fList.size() - 1; i >= 0; i--) {
                ICursor<T, E> cursor = fList.remove(i);
                if (cursor.loadNext()) {
                    insert(list, cursor);
                } else {
                    cursor.close();
                }
            }
            fList = list;
            onBeginIterations();
            if (fList.isEmpty()) {
                endIterations();
            }
        }
        boolean result = false;
        if (!fList.isEmpty()) {
            result = true;
            ICursor<T, E> cursor = fList.remove(0);
            setCurrentValue(cursor);
            if (cursor.loadNext()) {
                insert(fList, cursor);
            } else {
                cursor.close();
            }
            if (fList.isEmpty()) {
                endIterations();
            }
        }
        return result;
    }

    /**
     * This is an utility method used to notify about the beginning of
     * iterations
     * 
     * @throws E
     */
    protected void onBeginIterations() throws E {
    }

    /**
     * This is an utility method used to notify about the end of iterations
     * 
     * @throws E
     */
    protected void onEndIterations() throws E {
    }

    private int searchPosition(List<ICursor<T, E>> list, ICursor<T, E> cursor)
        throws E {
        T obj = cursor.getCurrent();
        int low = 0;
        int high = list.size() - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            ICursor<T, E> midCursor = list.get(mid);
            T midObj = midCursor.getCurrent();
            int cmp = fComparator.compare(midObj, obj);
            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        return -(low + 1); // key not found
    }

    protected void setCurrentValue(ICursor<T, E> cursor) throws E {
        fCurrent = cursor.getCurrent();
    }

    @Override
    public String toString() {
        return "MergeCursor[" + fCurrent + ":" + fList.toString() + "]";
    }

}

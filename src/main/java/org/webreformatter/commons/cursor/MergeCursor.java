/**
 * 
 */
package org.webreformatter.commons.cursor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    public static class DefaultCursorComparator<T, E extends Exception>
        implements
        Comparator<ICursor<T, E>> {

        private Comparator<T> fComparator;

        public DefaultCursorComparator(Comparator<T> comparator) {
            fComparator = comparator;
        }

        public int compare(ICursor<T, E> o1, ICursor<T, E> o2) {
            T v1 = o1.getCurrent();
            T v2 = o2.getCurrent();
            return fComparator.compare(v1, v2);
        }
    }

    private enum Status {
        NOT_STARTED, STARTED, STOPPED
    }

    private Comparator<ICursor<T, E>> fComparator;

    private T fCurrentValue;

    protected List<ICursor<T, E>> fList = new ArrayList<ICursor<T, E>>();

    private Status fStatus = Status.NOT_STARTED;

    public MergeCursor() {
    }

    /**
     * 
     */
    public MergeCursor(Comparator<T> comparator, ICursor<T, E>... cursors) {
        init(new DefaultCursorComparator<T, E>(comparator), cursors);
    }

    public MergeCursor(
        Comparator<T> comparator,
        List<? extends ICursor<T, E>> cursors) {
        init(new DefaultCursorComparator<T, E>(comparator), cursors);
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

    public T getCurrent() {
        return fCurrentValue;
    }

    public void init(
        Comparator<ICursor<T, E>> comparator,
        ICursor<T, E>... cursors) {
        fComparator = comparator;
        init(Arrays.asList(cursors));
    }

    public void init(
        Comparator<ICursor<T, E>> comparator,
        List<? extends ICursor<T, E>> cursors) {
        fComparator = comparator;
        init(cursors);
    }

    protected void init(List<? extends ICursor<T, E>> cursors) {
        fList.clear();
        fCurrentValue = null;
        fStatus = Status.NOT_STARTED;
        fList.addAll(cursors);
    }

    private void insert(List<ICursor<T, E>> list, ICursor<T, E> cursor)
        throws E {
        int pos = Collections.binarySearch(list, cursor, fComparator);
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
            setCurrentCursor(cursor);
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

    protected void setCurrentCursor(ICursor<T, E> cursor) throws E {
        fCurrentValue = cursor.getCurrent();
    }

    @Override
    public String toString() {
        try {
            return "MergeCursor[" + getCurrent() + ":" + fList.toString() + "]";
        } catch (Exception e) {
            return "MergeCursor[" + fList.toString() + "]";
        }
    }

}

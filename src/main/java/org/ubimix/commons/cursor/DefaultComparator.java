/**
 * 
 */
package org.ubimix.commons.cursor;

import java.util.Comparator;

/**
 * @author kotelnikov
 */
public class DefaultComparator<T> implements Comparator<T> {

    public static <T> DefaultComparator<T> newComparator() {
        return new DefaultComparator<T>();
    }

    /**
     * 
     */
    public DefaultComparator() {
    }

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public int compare(T o1, T o2) {
        Comparable<Comparable<?>> c1 = (Comparable<Comparable<?>>) o1;
        Comparable<Comparable<?>> c2 = (Comparable<Comparable<?>>) o2;
        return c1.compareTo(c2);
    }

}

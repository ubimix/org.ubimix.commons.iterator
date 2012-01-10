package org.webreformatter.commons.cursor;

/**
 * Cursor factory. Used to create new cursor instances.
 * 
 * @author kotelnikov
 * @param <P> type of the parameters transfered to the
 *        {@link #getCursor(Object)} method
 * @param <T> the type of the nodes returned by the cursor
 * @param <E> the exception
 */
public interface ICursorProvider<P, T, E extends Exception> {
    /**
     * Returns an iterator corresponding to the specified parameter.
     * 
     * @param parameter the parameter used to generate a new cursor instance
     * @return a new cursor
     */
    ICursor<T, E> getCursor(P parameter) throws E;

}
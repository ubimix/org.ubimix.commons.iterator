/**
 * 
 */
package org.webreformatter.commons.cursor;

/**
 * @author kotelnikov
 */
public interface ICursor<T, E extends Exception> {

    void close() throws E;

    T getCurrent();

    boolean loadNext() throws E;
}

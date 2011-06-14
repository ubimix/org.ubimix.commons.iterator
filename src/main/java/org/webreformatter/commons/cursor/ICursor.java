/**
 * 
 */
package org.webreformatter.commons.cursor;

/**
 * @author kotelnikov
 */
public interface ICursor<T, E extends Exception> {

    void close() throws E;

    T getCurrent() throws E;

    boolean loadNext() throws E;
}
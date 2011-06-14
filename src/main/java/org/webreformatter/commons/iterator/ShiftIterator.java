/* ************************************************************************** *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 * 
 * This file is licensed to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ************************************************************************** */
package org.webreformatter.commons.iterator;

import java.util.Iterator;

/**
 * Shift iterator returns objects until the returned objects are not
 * <code>null</code>.
 * 
 * @author kotelnikov
 */
public abstract class ShiftIterator<T> implements Iterator<T> {

    private boolean fDone;

    protected T fObject;

    /**
     * Constructor for ShiftIterator.
     */
    public ShiftIterator() {
        this(null);
    }

    /**
     * Constructor for ShiftIterator.
     * 
     * @param firstObject the first object returned by this iterator item source
     */
    public ShiftIterator(T firstObject) {
        reset(firstObject);
    }

    /**
     * @return the current object of this iterator
     */
    public T getObject() {
        return fObject;
    }

    /**
     * @return <code>true</code> if there is at least one object to return.
     */
    public boolean hasNext() {
        return step(true);
    }

    /**
     * Returns the next object.
     * 
     * @return the next object.
     */
    public T next() {
        return step(false) ? getObject() : null;
    }

    /**
     * @throws UnsupportedOperationException - this is an unallowed operation.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Method reset.
     */
    public void reset() {
        reset(null);
    }

    /**
     * Method reset.
     * 
     * @param object
     */
    public void reset(T object) {
        fObject = object;
        fDone = (fObject != null);
    }

    protected abstract T shiftItem();

    /**
     * Go to the next node.
     * 
     * @param result
     * @return true if a bew object was successfully loaded
     */
    private boolean step(boolean result) {
        if (!fDone) {
            fObject = shiftItem();
        }
        fDone = result;
        return (fObject != null);
    }

}

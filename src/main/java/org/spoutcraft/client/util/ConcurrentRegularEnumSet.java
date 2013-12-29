/**
 * This file is part of Client, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spoutcraft <http://spoutcraft.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spoutcraft.client.util;

import java.lang.reflect.Array;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
public class ConcurrentRegularEnumSet<E extends Enum<E>> extends AbstractSet<E> {
    private final Class<E> enumClass;
    private final E[] enumConstants;
    private final AtomicLong set = new AtomicLong(0);

    public ConcurrentRegularEnumSet(Class<E> enumClass) {
        this.enumClass = enumClass;
        enumConstants = enumClass.getEnumConstants();
        if (enumConstants.length > 64) {
            throw new IllegalArgumentException("Max enum size is 64");
        }
    }

    @Override
    public int size() {
        return Long.bitCount(set.get());
    }

    @Override
    public boolean isEmpty() {
        return set.get() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return isClass(o, enumClass) && (set.get() & getMask(o)) != 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new AtomicEnumSetIterator<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public E[] toArray() {
        final E[] array = (E[]) Array.newInstance(enumClass, enumConstants.length);
        int i = 0;
        for (int l = 0; l < enumConstants.length; l++) {
            final long mask = 1l << l;
            if ((set.get() & mask) != 0) {
                array[i++] = enumConstants[Long.numberOfTrailingZeros(mask)];
            }
        }
        final E[] finalArray = (E[]) Array.newInstance(enumClass, i);
        System.arraycopy(array, 0, finalArray, 0, i);
        return finalArray;
    }

    @Override
    public <E> E[] toArray(E[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(E e) {
        checkClass(e, enumClass);
        final long mask = getMask(e);
        boolean done = false;
        long oldSet = 0, newSet = 0;
        while (!done) {
            oldSet = set.get();
            newSet = oldSet | mask;
            done = set.compareAndSet(oldSet, newSet);
        }
        return oldSet != newSet;
    }

    @Override
    public boolean remove(Object o) {
        if (!isClass(o, enumClass)) {
            return false;
        }
        final long mask = ~getMask(o);
        boolean done = false;
        long oldSet = 0, newSet = 0;
        while (!done) {
            oldSet = set.get();
            newSet = oldSet & mask;
            done = set.compareAndSet(oldSet, newSet);
        }
        return oldSet != newSet;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (!(c instanceof ConcurrentRegularEnumSet)) {
            return super.containsAll(c);
        }
        final ConcurrentRegularEnumSet<?> other = (ConcurrentRegularEnumSet) c;
        return enumClass == other.enumClass && (other.set.get() & ~set.get()) == 0;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (!(c instanceof ConcurrentRegularEnumSet)) {
            return super.addAll(c);
        }
        final ConcurrentRegularEnumSet<?> other = ((ConcurrentRegularEnumSet) c);
        if (enumClass != other.enumClass) {
            throw new ClassCastException();
        }
        boolean done = false;
        long oldSet = 0, newSet = 0;
        while (!done) {
            oldSet = set.get();
            newSet = oldSet | other.set.get();
            done = set.compareAndSet(oldSet, newSet);
        }
        return oldSet != newSet;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        if (!(c instanceof ConcurrentRegularEnumSet)) {
            return super.retainAll(c);
        }
        final ConcurrentRegularEnumSet<?> other = ((ConcurrentRegularEnumSet) c);
        if (enumClass != other.enumClass) {
            return false;
        }
        boolean done = false;
        long oldSet = 0, newSet = 0;
        while (!done) {
            oldSet = set.get();
            newSet = oldSet & other.set.get();
            done = set.compareAndSet(oldSet, newSet);
        }
        return oldSet != newSet;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (!(c instanceof ConcurrentRegularEnumSet)) {
            return super.removeAll(c);
        }
        final ConcurrentRegularEnumSet<?> other = ((ConcurrentRegularEnumSet) c);
        if (enumClass != other.enumClass) {
            return false;
        }
        boolean done = false;
        long oldSet = 0, newSet = 0;
        while (!done) {
            oldSet = set.get();
            newSet = oldSet & ~other.set.get();
            done = set.compareAndSet(oldSet, newSet);
        }
        return oldSet != newSet;
    }

    @Override
    public void clear() {
        set.set(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConcurrentRegularEnumSet)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final ConcurrentRegularEnumSet<?> that = (ConcurrentRegularEnumSet) o;
        return enumClass == that.enumClass && set.get() == that.set.get();
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + enumClass.hashCode();
        final long l = set.get();
        result = 31 * result + ((int) l ^ (int) (l >>> 32));
        return result;
    }

    private static boolean isClass(Object o, Class<?> c) {
        return o != null && c.isAssignableFrom(o.getClass());
    }

    private static void checkClass(Object o, Class<?> c) {
        if (!isClass(o, c)) {
            throw new ClassCastException();
        }
    }

    private static long getMask(Object o) {
        return 1l << ((Enum) o).ordinal();
    }

    private class AtomicEnumSetIterator<E extends Enum<E>> implements Iterator<E> {
        private int position = 0;

        @Override
        public boolean hasNext() {
            return (set.get() & ~((1l << position) - 1)) != 0;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            while ((set.get() & (1l << position++)) == 0) {
            }
            return (E) enumConstants[Long.numberOfTrailingZeros(1l << position - 1)];
        }

        @Override
        public void remove() {
            if ((set.get() & ~((1l << position - 1) - 1)) == 0) {
                throw new NoSuchElementException();
            }
            boolean done = false;
            long oldSet, newSet;
            while (!done) {
                oldSet = set.get();
                newSet = oldSet & ~(1l << position - 1);
                done = set.compareAndSet(oldSet, newSet);
            }
            position--;
        }
    }
}

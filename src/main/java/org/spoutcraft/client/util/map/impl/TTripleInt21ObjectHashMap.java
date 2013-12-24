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
package org.spoutcraft.client.util.map.impl;

import java.util.Collection;

import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.set.TLongSet;
import org.spoutcraft.client.util.hashing.Int21TripleHashed;
import org.spoutcraft.client.util.map.TripleIntObjectMap;

/**
 * A simplistic map that supports a 3 21 bit integers for keys, using a trove long Object hashmap in the backend. 1 bit is wasted.
 *
 * @see {@link org.spoutcraft.client.util.hashing.Int21TripleHashed}
 */
public class TTripleInt21ObjectHashMap<T> implements TripleIntObjectMap<T> {
    protected final TLongObjectMap<T> map;

    /**
     * Creates a new <code>TTripleInt21ObjectHashMap</code> instance backend by a {@see TLongObjectHashMap} instance with an capacity of 100 and the default load factor.
     */
    public TTripleInt21ObjectHashMap() {
        map = new TLongObjectHashMap<>(100);
    }

    /**
     * Creates a new <code>TTripleInt21ObjectHashMap</code> instance backend by a {@see TLongObjectHashMap} instance with a prime capacity equal to or greater than <code>capacity</code> and with the
     * default load factor.
     *
     * @param capacity an <code>int</code> value
     */
    public TTripleInt21ObjectHashMap(int capacity) {
        map = new TLongObjectHashMap<>(capacity);
    }

    /**
     * Creates a new <code>TTripleInt21ObjectHashMap</code> instance backend by <code>map</code>
     */
    public TTripleInt21ObjectHashMap(TTripleInt21ObjectHashMap<T> map) {
        if (map == null) {
            throw new IllegalArgumentException("The backend can not be null.");
        }

        this.map = map.map;
    }

    /**
     * Associates the specified value with the specified key in this map (optional operation). If the map previously contained a mapping for the key, the old value is replaced by the specified value.
     * (A map m is said to contain a mapping for a key k if and only if {@see #containsKey(int, int, int) m.containsKey(k)} would return <code>true</code>.)
     *
     * @param x an <code>int</code> value
     * @param y an <code>int</code> value
     * @param z an <code>int</code> value
     * @return the previous value associated with <code>key(x, y, z)</code>, or no_entry_value if there was no mapping for <code>key(x, y, z)</code>. (A no_entry_value return can also indicate that
     * the map previously associated <code>null</code> with key, if the implementation supports <code>null</code> values.)
     */
    @Override
    public T put(int x, int y, int z, T value) {
        long key = Int21TripleHashed.key(x, y, z);
        return map.put(key, value);
    }

    @Override
    public T putIfAbsent(int x, int y, int z, T value) {
        return null;
    }

    /**
     * Returns the value to which the specified key is mapped, or <code>null</code> if this map contains no mapping for the key. <p> More formally, if this map contains a mapping from a key
     * <code>k</code> to a value <code>v</code> such that <code>(key==null ? k==null : key.equals(k))</code>, then this method returns <code>v</code>; otherwise it returns <code>null</code>. (There
     * can be at most one such mapping.) <p> If this map permits <code>null</code> values, then a return value of <code>null</code> does not <i>necessarily</i> indicate that the map contains no
     * mapping for the key; it's also possible that the map explicitly maps the key to <code>null</code>. The {@see #containsKey(int, int, int) containsKey} operation may be used to distinguish these
     * two cases.
     *
     * @param x an <code>int</code> value
     * @param y an <code>int</code> value
     * @param z an <code>int</code> value
     * @return the value to which the specified <code>key(x, y, z)</code> is mapped, or <code>null</code> if this map contains no mapping for the key.
     */
    @Override
    public T get(int x, int y, int z) {
        long key = Int21TripleHashed.key(x, y, z);
        return map.get(key);
    }

    /**
     * Returns true if this map contains a mapping for the specified key. More formally, returns <code>true</code> if and only if this map contains a mapping for a key <code>k</code> such that
     * <code>key.equals(k)</code>. (There can be at most one such mapping.)
     *
     * @param x an <code>int</code> value
     * @param y an <code>int</code> value
     * @param z an <code>int</code> value
     * @return <code>true</code> if this map contains a mapping for the specified <code>key(x, y, z)</code>.
     */
    @Override
    public boolean containsKey(int x, int y, int z) {
        long key = Int21TripleHashed.key(x, y, z);
        return map.containsKey(key);
    }

    /**
     * Removes all of the mappings from this map (optional operation). The map will be empty after this call returns.
     */
    @Override
    public void clear() {
        map.clear();
    }

    /**
     * Returns <code>true</code> if this map contains a mapping for the specified key. More formally, returns <code>true</code> if and only if this map contains a mapping for a key <code>k</code> such
     * that <code>key.equals(k)</code>. (There can be at most one such mapping.)
     *
     * @param val value whose presence in this map is to be tested
     * @return <code>true</code> if this map maps one or more keys to the specified value
     */
    @Override
    public boolean containsValue(T val) {
        return map.containsValue(val);
    }

    /**
     * Returns <code>true</code> if this map contains no key-value mappings.
     *
     * @return <code>true</code> if this map contains no key-value mappings.
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns a {@see TLongObjectIterator} with access to this map's keys and values.
     *
     * @return a {@see TLongObjectIterator} with access to this map's keys and values.
     */
    @Override
    public TLongObjectIterator<T> iterator() {
        return map.iterator();
    }

    /**
     * Returns a {@see TLongSet} view of the keys contained in this map. The set is backed by the map, so changes to the map are reflected in the set, and vice-versa. If the map is modified while an
     * iteration over the set is in progress (except through the iterator's own remove operation), the results of the iteration are undefined. The set supports element removal, which removes the
     * corresponding mapping from the map, via the <code>Iterator.remove</code>, <code>Set.remove</code>, <code>removeAll</code>, <code>retainAll</code>, and <code>clear</code> operations. It does not
     * support the add or addAll operations.
     *
     * @return a set view of the keys contained in this map.
     */
    @Override
    public TLongSet keySet() {
        return map.keySet();
    }

    /**
     * Returns a copy of the keys of the map as an array. Changes to the array of keys will not be reflected in the map nor vice-versa.
     *
     * @return a copy of the keys of the map as an array.
     */
    @Override
    public long[] keys() {
        return map.keys();
    }

    /**
     * Removes the mapping for a key from this map if it is present (optional operation). More formally, if this map contains a mapping from key <code>k</code> to value <code>v</code> such that
     * <code>key.equals(k)</code>, that mapping is removed. (The map can contain at most one such mapping.) <p> Returns the value to which this map previously associated the key, or <code>null</code>
     * if the map contained no mapping for the key. </p> If this map permits null values, then a return value of <code>null</code> does not <i>necessarily</i> indicate that the map contained no
     * mapping for the key; it's also possible that the map explicitly mapped the key to <code>null</code>. <p> The map will not contain a mapping for the specified key once the call returns.
     *
     * @param x an <code>int</code> value
     * @param y an <code>int</code> value
     * @param z an <code>int</code> value
     * @return the previous <code>long</code> value associated with <code>key(x, y, z)</code>, or <code>null</code> if there was no mapping for key.
     */
    @Override
    public T remove(int x, int y, int z) {
        long key = Int21TripleHashed.key(x, y, z);
        return map.remove(key);
    }

    /**
     * Returns the number of key-value mappings in this map. If the map contains more than <code>Integer.MAX_VALUE</code> elements, returns <code>Integer.MAX_VALUE</code>.
     *
     * @return the number of key-value mappings in this map
     */
    @Override
    public int size() {
        return map.size();
    }

    /**
     * Returns a {@see Collection} view of the values contained in this map. The collection is backed by the map, so changes to the map are reflected in the collection, and vice-versa. If the map is
     * modified while an iteration over the collection is in progress (except through the iterator's own remove operation), the results of the iteration are undefined. The collection supports element
     * removal, which removes the corresponding mapping from the map, via the <code>Iterator.remove</code>, <code>Collection.remove</code>, <code>removeAll</code>, <code>retainAll</code> and
     * <code>clear</code> operations. It does not support the <code>add</code> or <code>addAll</code> operations.
     */
    @Override
    public Collection<T> valueCollection() {
        return map.valueCollection();
    }

    /**
     * Returns the values of the map as an array of <code>long</code> values. Changes to the array of values will not be reflected in the map nor vice-versa.
     *
     * @return the values of the map as an array of <code>long</code> values.
     */
    @SuppressWarnings ("unchecked")
    public T[] values() {
        return (T[]) map.values();
    }
}

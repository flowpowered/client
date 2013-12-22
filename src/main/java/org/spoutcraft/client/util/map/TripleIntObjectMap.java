/*
 * This file is part of Spout.
 *
 * Copyright (c) 2011 Spout LLC <http://www.spout.org/>
 * Spout is licensed under the Spout License Version 1.
 *
 * Spout is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the Spout License Version 1.
 *
 * Spout is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the Spout License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://spout.in/licensev1> for the full license, including
 * the MIT license.
 */
package org.spoutcraft.client.util.map;

import java.util.Collection;

import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.set.TLongSet;

public interface TripleIntObjectMap<T> {
    /**
     * Gets the value for the given (x, y, z) key, or null if none
     *
     * @return the value
     */
    public T get(int x, int y, int z);

    /**
     * Returns true if this map contains a mapping for the specified key. More formally, returns <code>true</code> if and only if this map contains a mapping for a key <code>k</code> such that
     * <code>key.equals(k)</code>. (There can be at most one such mapping.)
     *
     * @param x an <code>int</code> value
     * @param y an <code>int</code> value
     * @param z an <code>int</code> value
     * @return <code>true</code> if this map contains a mapping for the specified <code>key(x, y, z)</code>.
     */
    public boolean containsKey(int x, int y, int z);

    /**
     * Removes all of the mappings from this map (optional operation). The map will be empty after this call returns.
     */
    public void clear();

    /**
     * Returns <code>true</code> if this map contains a mapping for the specified key. More formally, returns <code>true</code> if and only if this map contains a mapping for a key <code>k</code> such
     * that <code>key.equals(k)</code>. (There can be at most one such mapping.)
     *
     * @param val value whose presence in this map is to be tested
     * @return <code>true</code> if this map maps one or more keys to the specified value
     */
    public boolean containsValue(T val);

    /**
     * Returns <code>true</code> if this map contains no key-value mappings.
     *
     * @return <code>true</code> if this map contains no key-value mappings.
     */
    public boolean isEmpty();

    /**
     * Returns a {@see TLongObjectIterator} with access to this map's keys and values.
     *
     * @return a {@see TLongObjectIterator} with access to this map's keys and values.
     */
    public TLongObjectIterator<T> iterator();

    /**
     * Returns a {@see TLongSet} view of the keys contained in this map. The set is backed by the map, so changes to the map are reflected in the set, and vice-versa. If the map is modified while an
     * iteration over the set is in progress (except through the iterator's own remove operation), the results of the iteration are undefined. The set supports element removal, which removes the
     * corresponding mapping from the map, via the <code>Iterator.remove</code>, <code>Set.remove</code>, <code>removeAll</code>, <code>retainAll</code>, and <code>clear</code> operations. It does not
     * support the add or addAll operations.
     *
     * @return a set view of the keys contained in this map.
     */
    public TLongSet keySet();

    /**
     * Returns a copy of the keys of the map as an array. Changes to the array of keys will not be reflected in the map nor vice-versa.
     *
     * @return a copy of the keys of the map as an array.
     */
    public long[] keys();

    /**
     * Removes the key/value pair for the given (x, y, z) key
     *
     * @return the value removed, or null on failure
     */
    public T remove(int x, int y, int z);

    /**
     * Adds the given key/value pair to the map
     *
     * @param value the non-null value
     * @return the old value
     */
    public T put(int x, int y, int z, T value);

    /**
     * Adds the given key/value pair to the map, but only if the key does not already map to a value
     *
     * @param value the non-null value
     * @return the current value, or null on success
     */
    public T putIfAbsent(int x, int y, int z, T value);

    /**
     * Returns the number of key-value mappings in this map. If the map contains more than <code>Integer.MAX_VALUE</code> elements, returns <code>Integer.MAX_VALUE</code>.
     *
     * @return the number of key-value mappings in this map
     */
    public int size();

    /**
     * Returns a collection containing all the values in the Map
     */
    public Collection<T> valueCollection();
}

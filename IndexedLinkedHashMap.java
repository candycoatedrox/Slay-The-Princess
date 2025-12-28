import java.util.ArrayList;
import java.util.LinkedHashMap;

public class IndexedLinkedHashMap<K,V> extends LinkedHashMap<K,V> {
    
    ArrayList<K> index = new ArrayList<>();

    /**
     * Associates the specified value with the specified key in this map and appends the key to the end of the index list if it was not already present; if the map previously contained a mapping for the key, the old value is replaced
     * @param key key with which the specified value is to be associated
     * @param val value to be associated with the specified key
     * @return the previous value associated with key, or null if there was no mapping for key. (A null return can also indicate that the map previously associated null with key.)
     */
    @Override
    public V put(K key, V val) {
        if (!super.containsKey(key)) {
            index.add(key);
        }

        V returnValue = super.put(key, val);
        return returnValue;
    }

    /**
     * Removes the mapping for the specified key from this map if present
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with key, or null if there was no mapping for key. (A null return can also indicate that the map previously associated null with key.)
     */
    @Override
    public V remove(Object key) {
        index.remove(getIndex((K)key));
        V returnValue = super.remove(key);
        return returnValue;
    }

    /**
     * Removes all of the mappings from this map; the map will be empty after this call returns
     */
    @Override
    public void clear() {
        super.clear();
        index.clear();
    }

    /**
     * Returns the value at the specified position in this map
     * @param i index of the value to return
     * @return the value at the specified position in this map
     */
    public V getValue(int i) {
        return (V) super.get(index.get(i));
    }

    /**
     * Returns the key at the specified position in this map
     * @param i index of the key to return
     * @return the key at the specified position in this map
     */
    public K getKey(int i) {
        return (K) index.get(i);
    }

    /**
     * Returns the index of the specified key in this map, or -1 if this map contains no mapping for the key
     * @param key the key whose associated position is to be returned
     * @return the index of the specified key in this map, or -1 if this map does not contain the key
     */
    public int getIndex(K key) {
        if (super.containsKey(key)) {
            return index.indexOf(key);
        } else {
            return -1;
        }
    }

    /**
     * Returns the index of the first occurrence of the specified value in this map, or -1 if this map does not contain the value
     * @param value the value whose associated position is to be returned
     * @return the index of the the first occurrence of the specified value in this map, or -1 if this map does not contain the value
     */
    public int indexOf(V value) {
        for (int i = 0; i < this.index.size(); i++) {
            if (super.get(index.get(i)) == value) {
                return i;
            }
        }

        return -1;
    }

}

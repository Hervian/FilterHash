package com.hervian.filterhash;

import com.google.common.annotations.VisibleForTesting;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.util.Assert;

/**
 * 
 * I developed the algorithm as spare time project. I was at the time only aware of double hashing, which I wanted to improve. 
 * I was later made aware that the algorithm coincides with a paper by Rasmus Pagh et al. (http://www.itu.dk/people/pagh/papers/d-cuckoo-jour.pdf)
 * The term 'filter hashing' comes from that paper, which I have not yet had time to read. 
 * As such, the relationship between the algorithm presented in the paper, and the present one, should be inspected/verified by anyone wishing to use Pagh et als 'filter hashing'.
 * 
 * The algorithm's performance is like that of uniform open address hashing.
 * @author Anders Høfft
 */
public class FilterHash<K,V> implements Map<K,V>{
    @VisibleForTesting
    Entry<K,V>[] table;
    LinkedList<Subtable> subtables;
    int initialCapacity=2048, totalNumberOfEntries = 0;
    float maximumLoadFactor=0.8f;
    
    public FilterHash(int initialCapacity, float maximumLoadFactor){
        Assert.isTrue((this.initialCapacity=initialCapacity)>0, "The initialCapacity must be a positive integer.");
        Assert.isTrue((this.maximumLoadFactor=maximumLoadFactor)>0 && maximumLoadFactor<1, "The maximum load factor must be a number between 0 and 1.");
        init();
    }
    
    private void init() {
        initTable();
        initSubtables();
    }

    private void initTable() {
        BigDecimal arraySize = new BigDecimal(initialCapacity).divide(new BigDecimal(maximumLoadFactor), 64, RoundingMode.HALF_UP);
        table = new Entry[arraySize.setScale(0, RoundingMode.CEILING).intValue()];
    }
    
    private void initSubtables() {
        subtables = new LinkedList<Subtable>();
        int totalSizeOfSubtables = 0;
        Subtable previousSubtable = new Subtable(0, 0);
        while (totalSizeOfSubtables < table.length) {
            int size = getSize(table.length - totalSizeOfSubtables);
            totalSizeOfSubtables += size;
            subtables.add(new Subtable(size, previousSubtable.startIndex + previousSubtable.size));
            previousSubtable = subtables.getLast();
        }
    }
    
    private int getSize(int remainingIndexes) {
        BigDecimal denominator = BigDecimalHelper.ln(BigDecimal.ONE.subtract(new BigDecimal(maximumLoadFactor)), 64).negate();
        int size = BigDecimal.ONE.divide(denominator, 64, RoundingMode.FLOOR).multiply(new BigDecimal(remainingIndexes*maximumLoadFactor)).setScale(0, RoundingMode.FLOOR).intValue();
        return size<1 ? 1 : size;
    }
    
    @Override
    public V get(Object key) {
        int hashvalue = Math.abs(key.hashCode());//TODO: Don't use Math.abs. See https://www.cs.princeton.edu/~rs/AlgsDS07/10Hashing.pdf
        Entry<K,V> existingEntry, matchingEntry=null;
        for (Subtable subtable : subtables){
            existingEntry = getEntryFromSubtable(subtable, hashvalue);
            if (existingEntry==null || existingEntry.getKey()==key ||existingEntry.getKey().equals(key)){ //We attempt to avoid the, in this context, expensive equals()-method.
                matchingEntry = existingEntry;
                break;
            }
        }
        return matchingEntry==null ? null : matchingEntry.value;
    }
    
    @VisibleForTesting //TODO: add jUnit test
    Entry<K,V> getEntryFromSubtable(Subtable subtable, int hashvalue){
        return subtable.get(hashvalue);
    }
    
    @Override
    public V put(K key, V value) {
        int hashvalue = Math.abs(key.hashCode());//TODO: Don't use Math.abs. See https://www.cs.princeton.edu/~rs/AlgsDS07/10Hashing.pdf
        Entry<K,V> existingEntry = null, newEntry=null;
        for (Subtable subtable : subtables) {
            existingEntry = subtable.get(hashvalue);
            if (existingEntry==null || existingEntry.key==key || existingEntry.key.equals(key)) {
                subtable.put(hashvalue, newEntry=new Entry(key, value));
                break;
            }
        }
        if (newEntry==null) {
            resize();
        }
        return existingEntry==null ? null : existingEntry.value;
    }
    
    private void resizeAndPut(K key, V value) {
        resize();
    }

    private void resize() {
        throw new UnsupportedOperationException("Resizing operation is not supported yet.");
    }

    @Override
    public Set<Map.Entry<K,V>> entrySet() {
        throw new UnsupportedOperationException("Resizing operation is not supported yet.");
    }

    @Override
    public int size() {
        return totalNumberOfEntries;
    }

    @Override
    public boolean isEmpty() {
        return size()==0;
    }

    @Override
    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @VisibleForTesting
    class Entry<K, V> implements Map.Entry<K,V>{
        private K key;
        private V value;
        
        private Entry(K key, V value){
            this.key = key;
            this.value = value;
        }

        @Override
        public final K getKey()        { return key; }
        @Override
        public final V getValue()      { return value; }
        @Override
        public final String toString() { return key + "=" + value; }

        @Override
        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue; 
        }

        @Override
        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                if (Objects.equals(key, e.getKey()) &&
                    Objects.equals(value, e.getValue()))
                    return true;
            }
            return false;
        }
    }
    
    @VisibleForTesting
    public class Subtable {
        int size;
        int startIndex;
        
        public Subtable(int size, int startIndex){
            this.size = size;
            this.startIndex = startIndex;
        }

        public void put(int hashCode, Entry entry) {
            table[hashCode%size+startIndex]=entry;
            totalNumberOfEntries++;
        }
        
        public Entry get(int hashCode) {
            return (Entry) table[hashCode%size+startIndex];
        }

    }

}

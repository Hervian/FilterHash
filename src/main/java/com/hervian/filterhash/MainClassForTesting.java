package com.hervian.filterhash;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * This project contains an open addressing based hash table called, see {@link FilterHash}.
 * 
 * I developed the algorithm as spare time project. I was at the time only aware of double hashing, which I wanted to improve. 
 * I was later made aware that the algorithm coincides with a paper by Rasmus Pagh et al. (http://www.itu.dk/people/pagh/papers/d-cuckoo-jour.pdf)
 * The term 'filter hashing' comes from that paper, which I have not yet had time to read. 
 * As such, the relationship between the algorithm presented in the paper, and the present one, should be inspected/verified by anyone wishing to use Pagh et als 'filter hashing'.
 * 
 * The algorithm's performance is like that of uniform open address hashing.
 * 
 * This main class is an example on how to test the data structure for attributes such as:
 * - Average number of probes per successful search
 * - Average number of probes per unsuccessful search
 * - Probability of call to resize occurring before the data structure reaches the maximum load factor
 * @author Anders Høfft
 */
public class MainClassForTesting {

    private static int numberOfTestObjects = 100000;
    private static float loadFactor = 0.99f;

    public static void main(String[] args) {
        int exceptionsThrown = 0, loops=10, totalNumberOfProbes=0;
        FilterHashForTest filterTable = null;
                
        for (int i=0; i<loops; i++){
            try {
                List<Object> list = new ArrayList();
                for (int j = 0; j < numberOfTestObjects; j++) {
                    list.add(RandomStringUtils.randomAscii(20));
                }

                filterTable = new FilterHashForTest(list.size(), loadFactor);

                for (Object o : list) {
                    filterTable.put(o, o);
                }

                for (Object o : list) {
                    filterTable.get(o);
                }
                
                totalNumberOfProbes+=filterTable.numberOfProbes;
            } catch (UnsupportedOperationException e) {
                exceptionsThrown++;
            } 
        }
        System.out.println("Average umber of successfull probes = " + (float)((float) totalNumberOfProbes / (float)(numberOfTestObjects*(loops-exceptionsThrown))));
        System.out.println("Probability of a resize() = " + (float)((float) exceptionsThrown / (float) loops));
    }

    private static class FilterHashForTest<K, V> extends FilterHash<K, V> {

        int numberOfProbes = 0;

        public FilterHashForTest(int initialCapacity, float maximumLoadFactor) {
            super(initialCapacity, maximumLoadFactor);
        }

        @Override
        Entry getEntryFromSubtable(Subtable subtable, int hashvalue) {
            numberOfProbes++;
            return super.getEntryFromSubtable(subtable, hashvalue);
        }

    }

}

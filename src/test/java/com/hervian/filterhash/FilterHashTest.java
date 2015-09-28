package com.hervian.filterhash;

import com.hervian.filterhash.FilterHash.Subtable;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Anders Høfft
 */
public class FilterHashTest {
    
    @Test (expected = IllegalArgumentException.class)
    public void test_initialization_negativeCapacity(){
        new FilterHash(-10, 0.9f);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void test_initialization_negativeLoadFactor(){
        new FilterHash(10, -0.9f);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void test_initialization_loadFactorBiggerThanOne(){
        new FilterHash(10, 1.9f);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void test_initialization_loadFactorEqualsOne(){
        new FilterHash(10, 1f);
    }
    
    @Test (expected = IllegalArgumentException.class)
    public void test_initialization_loadFactorEqualsZero(){
        new FilterHash(10, 0f);
    }
    
    @Test
    public void test_initialization_initialCapacityCorrectlyAssigned(){
        FilterHash filterHash = new FilterHash(10, 0.9f);
        
        assertEquals(10, filterHash.initialCapacity);
    }
    
    @Test
    public void test_initialization_loadFactorCorrectlyAssigned(){
        FilterHash filterHash = new FilterHash(10, 0.9f);
        
        assertTrue(0.9f==filterHash.maximumLoadFactor);
    }
    
    @Test
    public void test_initialization(){
        FilterHash filterHash = new FilterHash(10, 0.99f);
        
        assertEquals(11, filterHash.table.length);
        
        //Calculated "by hand" the math tells us that, given the arguments, each subtable we have a number of indexeces equal to about 2.5% of the input it recieves.):
        Subtable subtable = (Subtable) filterHash.subtables.get(0);
        assertEquals(2, subtable.size);
        assertEquals(0, subtable.startIndex);
        
        subtable = (Subtable) filterHash.subtables.get(1);
        assertEquals(1, subtable.size);
        assertEquals(2, subtable.startIndex);
        
        subtable = (Subtable) filterHash.subtables.getLast();
        assertEquals(1, subtable.size);
        assertEquals(10, subtable.startIndex);
    }
    
}

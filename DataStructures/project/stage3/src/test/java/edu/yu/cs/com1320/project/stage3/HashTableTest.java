package edu.yu.cs.com1320.project.stage3;

import org.junit.jupiter.api.Test;  
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;  

import edu.yu.cs.com1320.project.impl.HashTableImpl;


public class HashTableTest {
    HashTableImpl<String, String> table;
    Set<String> keys;
    ArrayList<String> values;
    int startingSize;
    @BeforeEach
    public void before(){
        this.table = new HashTableImpl<>();
        this.keys = new HashSet<>();
        this.values = new ArrayList<>();
        this.startingSize = 200;
        for (int i = 0; i< this.startingSize; i++){
            this.table.put("String"+i, i+"String");
            this.keys.add("String"+i);
            this.values.add(i+"String");

        }
    }
    @Test
    public void getTest(){
        assertEquals("8String", this.table.get("String8"));
        assertEquals("12String", this.table.get("String12"));
        assertEquals("18String", this.table.get("String18"));
        assertEquals("2String", this.table.get("String2"));
        assertEquals(null, this.table.get("2String"));
        assertEquals(null, this.table.get("2tring"));
        assertEquals(null, this.table.get("String"));
        assertEquals(null, this.table.get("string5"));

    }

    @Test
    public void overrideVal(){
        assertEquals("11String", this.table.get("String11"));
        assertEquals("11String", this.table.put("String11", "NewString11"));
        assertEquals("NewString11", this.table.get("String11"));
        assertEquals("1String", this.table.get("String1"));
        assertEquals("1String", this.table.put("String1", "NewString1"));
        assertEquals("NewString1", this.table.get("String1"));
        assertFalse("11String".equals(this.table.get("String11")));
        assertFalse("1String".equals(this.table.get("String1")));
        assertEquals(this.startingSize, this.table.size());

    }
    @Test
    public void deleteVal(){
        assertEquals("11String", this.table.get("String11"));
        assertEquals("11String", this.table.put("String11", null));
        assertEquals(null, this.table.get("String11"));
        assertEquals("1String", this.table.get("String1"));
        assertEquals("1String", this.table.put("String1", null));
        assertEquals(null, this.table.get("String1"));
        assertFalse("11String".equals(this.table.get("String11")));
        assertFalse("1String".equals(this.table.get("String1")));
        assertEquals(this.startingSize-2, this.table.size());
    }
    @Test
    public void containsKeyTest(){
        for (int i = 0; i< this.startingSize; i++){
            assertTrue(this.table.containsKey("String"+i));
        }
        assertThrows(NullPointerException.class, () -> this.table.containsKey(null));
        assertFalse(this.table.containsKey("String206540"));
        assertFalse(this.table.containsKey("string1"));
        assertFalse(this.table.containsKey("String"));
        assertFalse(this.table.containsKey("1String"));
    }

    @Test
    public void keySetTest(){
        assertEquals(this.keys, this.table.keySet());
        this.table.put("String13", null);
        assertFalse(this.keys.equals(this.table.keySet()));
    }

    @Test
    public void valueSetTest(){
        assertTrue(this.values.containsAll(this.table.values()));
        assertTrue(this.table.values().containsAll(this.values));
    }

    @Test
    public void sizeTest(){
        for (int i = 0; i< this.startingSize; i++){
            this.table.put("String"+i, null);
            assertEquals(this.startingSize-1-i, this.table.size());
        }
    }

    @Test
    public void arrayResizing(){
        int testSize = 10000;
        for (int i = 0; i< testSize; i++){
            this.table.put("String"+i, i+"String");
        }
        assertTrue(this.table.arrLength > 2000);
    }
}

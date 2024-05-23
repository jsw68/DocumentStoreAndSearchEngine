package edu.yu.cs.com1320.project.stage6;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.stage6.impl.DocumentPersistenceManager;

public class BTreeTest {

    BTreeImpl<Integer, String> st;

    @BeforeEach
    public void before() {
        this.st = new BTreeImpl<>();
        // this.st.setPersistenceManager(new DocumentPersistenceManager(null));
    }

    @Test
    public void BTreeBasicGetPut() {
        for (int i = 0; i < 10; i++) {
            this.st.put(i, "value" + i);
        }
        for (int i = 0; i < 10; i++) {
            assertEquals("value" + i, this.st.get(i));
        }

    }
}

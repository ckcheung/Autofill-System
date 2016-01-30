package Autofill;

import com.itextpdf.text.Rectangle;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class AcroFormFieldTest {
    
    public AcroFormFieldTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // Larger page number
    @Test
    public void testCompareTo1() {
        AcroFormField field1 = new AcroFormField();
        field1.setPage(2);
        AcroFormField field2 = new AcroFormField();
        field2.setPage(1);
        int result = field1.compareTo(field2);
        assertEquals(1, result);
    }
    
    // smaller page number
    @Test
    public void testCompareTo2() {
        AcroFormField field1 = new AcroFormField();
        field1.setPage(1);
        AcroFormField field2 = new AcroFormField();
        field2.setPage(2);
        int result = field1.compareTo(field2);
        assertEquals(-1, result);
    }
    
    // Same page number
    // T,T,T in first condition checking of line comparsion
    @Test
    public void testCompareTo3() {
        AcroFormField field1 = new AcroFormField();
        field1.setPage(1);
        field1.setPosition(new Rectangle(10, 10, 20, 20));
        AcroFormField field2 = new AcroFormField();
        field2.setPage(1);
        field2.setPosition(new Rectangle(30, 30, 40, 40));
        int result = field1.compareTo(field2);
        assertEquals(1, result);
    }
    
    // Same page number, same line
    // T,T,F in first condition checking of line comparsion
    // F,X,X in Second condition checking of line comparsion
    // In the right hand side of another field
    @Test
    public void testCompareTo4() {
        AcroFormField field1 = new AcroFormField();
        field1.setPage(1);
        field1.setPosition(new Rectangle(25, 10, 35, 20));
        AcroFormField field2 = new AcroFormField();
        field2.setPage(1);
        field2.setPosition(new Rectangle(10, 15, 20, 25));
        int result = field1.compareTo(field2);
        assertEquals(1, result);
    }
    
    // Same page number, same line
    // T,F,X in first condition checking of line comparsion
    // F,X,X in second condition checking of line comparsion
    // In the left hand side of another field
    @Test
    public void testCompareTo5() {
        AcroFormField field1 = new AcroFormField();
        field1.setPage(1);
        field1.setPosition(new Rectangle(10, 10, 20, 20));
        AcroFormField field2 = new AcroFormField();
        field2.setPage(1);
        field2.setPosition(new Rectangle(30, 10, 40, 21));
        int result = field1.compareTo(field2);
        assertEquals(-1, result);
    }
    
    // Same page number, same line, same position
    // F,X,X in first condition checking of line comparsion
    // T,T,F in second condition checking of line comparsion
    @Test
    public void testCompareTo6() {
        AcroFormField field1 = new AcroFormField();
        field1.setPage(1);
        field1.setPosition(new Rectangle(10, 11, 20, 21));
        AcroFormField field2 = new AcroFormField();
        field2.setPage(1);
        field2.setPosition(new Rectangle(10, 10, 20, 20));
        int result = field1.compareTo(field2);
        assertEquals(0, result);
    }
    
    // Same page number, same line, same position
    // F,X,X in first condition checking of line comparsion
    // T,F,X in second condition checking of line comparsion
    @Test
    public void testCompareTo7() {
        AcroFormField field1 = new AcroFormField();
        field1.setPage(1);
        field1.setPosition(new Rectangle(10, 10, 20, 21));
        AcroFormField field2 = new AcroFormField();
        field2.setPage(1);
        field2.setPosition(new Rectangle(10, 10, 20, 20));
        int result = field1.compareTo(field2);
        assertEquals(0, result);
    }
    
    // Same page number, same line, same position
    // F,X,X in first condition checking of line comparsion
    // F,F,X in second condition checking of line comparsion
    @Test
    public void testCompareTo8() {
        AcroFormField field1 = new AcroFormField();
        field1.setPage(1);
        field1.setPosition(new Rectangle(10, 10, 20, 20));
        AcroFormField field2 = new AcroFormField();
        field2.setPage(1);
        field2.setPosition(new Rectangle(10, 10, 20, 20));
        int result = field1.compareTo(field2);
        assertEquals(0, result);
    }
    
    // Same page number
    // F,X,X in first condition checking of line comparsion
    // T,T,T in second condition checking of line comparsion
    @Test
    public void testCompareTo9() {
        AcroFormField field1 = new AcroFormField();
        field1.setPage(1);
        field1.setPosition(new Rectangle(10, 30, 20, 40));
        AcroFormField field2 = new AcroFormField();
        field2.setPage(1);
        field2.setPosition(new Rectangle(10, 10, 20, 20));
        int result = field1.compareTo(field2);
        assertEquals(-1, result);
    }
}

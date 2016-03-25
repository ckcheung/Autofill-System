package Autofill;

import com.itextpdf.text.Rectangle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class FormFillerTest {
    
    public FormFillerTest() {
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


    //@Test
    public void testFillPdf() throws Exception {
        System.out.println("fillPdf");
        String dest = "";
        String data = "";
        FormFiller instance = null;
        ArrayList<AcroFormField> expResult = null;
        ArrayList<AcroFormField> result = instance.fillPdf(dest, data);
        assertEquals(expResult, result);
    }
    
    // A > B1 > C1 > D1
    @Test
    public void testIsGroupMatch1() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        FormFiller formfiller = new FormFiller("");
        Method method = FormFiller.class.getDeclaredMethod("isGroupMatch", AcroFormField.class, String.class);
        method.setAccessible(true);
        AcroFormField field = new AcroFormField();
        String group = "";
        boolean isGroupMatch = (boolean)method.invoke(formfiller, field, group);
        assertEquals(true, isGroupMatch);
    }
    
    // A > B2 > C2 > D1
    @Test
    public void testIsGroupMatch2() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        FormFiller formfiller = new FormFiller("");
        Method method = FormFiller.class.getDeclaredMethod("isGroupMatch", AcroFormField.class, String.class);
        method.setAccessible(true);
        AcroFormField field = new AcroFormField();
        field.setGroup("same");
        String group = "same";
        boolean isGroupMatch = (boolean)method.invoke(formfiller, field, group);
        assertEquals(true, isGroupMatch);
    }
    
    // A > B2 > C2 > D2 > EX
    @Test
    public void testIsGroupMatch3() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        FormFiller formfiller = new FormFiller("");
        Method method = FormFiller.class.getDeclaredMethod("isGroupMatch", AcroFormField.class, String.class);
        method.setAccessible(true);
        AcroFormField field = new AcroFormField();
        field.setGroup("work exp");
        String group = "work experience";
        boolean isGroupMatch = (boolean)method.invoke(formfiller, field, group);
        assertEquals(true, isGroupMatch);
    }    
    
    // A > B2 > C2 > D2 > EX > F
    @Test
    public void testIsGroupMatch4() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        FormFiller formfiller = new FormFiller("");
        Method method = FormFiller.class.getDeclaredMethod("isGroupMatch", AcroFormField.class, String.class);
        method.setAccessible(true);
        AcroFormField field = new AcroFormField();
        field.setGroup("abc");
        String group = "edf";
        boolean isGroupMatch = (boolean)method.invoke(formfiller, field, group);
        assertEquals(false, isGroupMatch);
    }
    
    /*
    // Is on the right head side of another field
    @Test
    public void testIsOnRightTrue() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        FormFiller formfiller = new FormFiller("");
        Method method = FormFiller.class.getDeclaredMethod("isOnRight", Rectangle.class, Rectangle.class);
        method.setAccessible(true);
        Rectangle rectangleRight = new Rectangle(30, 10, 40, 20);
        Rectangle rectangleLeft = new Rectangle(10, 10, 20, 20);
        boolean isOnRight = (boolean)method.invoke(formfiller, rectangleRight, rectangleLeft);
        assertEquals(true, isOnRight);
    }
    
    // Is not on the right head side of another field
    @Test
    public void testIsOnRightFalse() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        FormFiller formfiller = new FormFiller("");
        Method method = FormFiller.class.getDeclaredMethod("isOnRight", Rectangle.class, Rectangle.class);
        method.setAccessible(true);
        Rectangle rectangleRight = new Rectangle(30, 10, 40, 20);
        Rectangle rectangleLeft = new Rectangle(10, 10, 20, 20);
        boolean isOnRight = (boolean)method.invoke(formfiller, rectangleLeft, rectangleRight);
        assertEquals(false, isOnRight);
    }
    */
    // Is on the same line
    @Test
    public void testIsOnSameLineTrue() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        FormFiller formfiller = new FormFiller("");
        Method method = FormFiller.class.getDeclaredMethod("isOnSameLine", Rectangle.class, Rectangle.class);
        method.setAccessible(true);
        Rectangle rectangle1 = new Rectangle(30, 10, 40, 20);
        Rectangle rectangle2 = new Rectangle(10, 10, 20, 20);
        boolean isOnSameLine = (boolean)method.invoke(formfiller, rectangle1, rectangle2);
        assertEquals(true, isOnSameLine);
    }
    
    // Is not on the same line
    @Test
    public void testIsOnSameLineFalse() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        FormFiller formfiller = new FormFiller("");
        Method method = FormFiller.class.getDeclaredMethod("isOnSameLine", Rectangle.class, Rectangle.class);
        method.setAccessible(true);
        Rectangle rectangle1 = new Rectangle(10, 40, 20, 50);
        Rectangle rectangle2 = new Rectangle(10, 10, 20, 20);
        boolean isOnSameLine = (boolean)method.invoke(formfiller, rectangle1, rectangle2);
        assertEquals(false, isOnSameLine);
    }
    /*
    // Is separated
    @Test
    public void testIsSeperatedTrue() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        FormFiller formfiller = new FormFiller("");
        Method method = FormFiller.class.getDeclaredMethod("isSeperated", Rectangle.class, Rectangle.class);
        method.setAccessible(true);
        Rectangle rectangleUpper = new Rectangle(10, 40, 20, 50);
        Rectangle rectangleLower = new Rectangle(10, 10, 20, 20);
        boolean isSeperated = (boolean)method.invoke(formfiller, rectangleLower, rectangleUpper);
        assertEquals(true, isSeperated);
    }   
    
    // Is not separated
    @Test
    public void testIsSeperatedFalse() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        FormFiller formfiller = new FormFiller("");
        Method method = FormFiller.class.getDeclaredMethod("isSeperated", Rectangle.class, Rectangle.class);
        method.setAccessible(true);
        Rectangle rectangleUpper = new Rectangle(10, 20, 20, 30);
        Rectangle rectangleLower = new Rectangle(10, 10, 20, 20);
        boolean isSeperated = (boolean)method.invoke(formfiller, rectangleLower, rectangleUpper);
        assertEquals(false, isSeperated);
    } 
    
    // Is new gruop (ie, TT)
    @Test
    public void testIsNewSectionTrue() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        FormFiller formfiller = new FormFiller("");
        Method method = FormFiller.class.getDeclaredMethod("isNewSection", Rectangle.class, Rectangle.class);
        method.setAccessible(true);
        Rectangle rectangle1 = new Rectangle(10, 10, 20, 20);
        Rectangle rectangle2 = new Rectangle(30, 40, 40, 50);
        boolean isNewSection = (boolean)method.invoke(formfiller, rectangle1, rectangle2);
        assertEquals(true, isNewSection);
    }
    
    
    // Is not new group (ie, FF)
    @Test
    public void testIsNewSectionFalse() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        FormFiller formfiller = new FormFiller("");
        Method method = FormFiller.class.getDeclaredMethod("isNewSection", Rectangle.class, Rectangle.class);
        method.setAccessible(true);
        Rectangle rectangle1 = new Rectangle(30, 40, 40, 50);
        Rectangle rectangle2 = new Rectangle(10, 10, 20, 20);
        boolean isNewSection = (boolean)method.invoke(formfiller, rectangle1, rectangle2);
        assertEquals(false, isNewSection);
    }
    
    // Is same line previous field (ie, TT)
    @Test
    public void testIsSameLinePreviousFieldTrue() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        FormFiller formfiller = new FormFiller("");
        Method method = FormFiller.class.getDeclaredMethod("isSameLinePreviousField", Rectangle.class, Rectangle.class);
        method.setAccessible(true);
        Rectangle rectangle1 = new Rectangle(30, 10, 40, 20);
        Rectangle rectangle2 = new Rectangle(10, 10, 20, 20);
        boolean isSameLinePreviousField = (boolean)method.invoke(formfiller, rectangle1, rectangle2);
        assertEquals(true, isSameLinePreviousField);
    }
    
    // Is not same line previous field (ie, FF)
    @Test
    public void testIsSameLinePreviousFieldFalse() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        FormFiller formfiller = new FormFiller("");
        Method method = FormFiller.class.getDeclaredMethod("isSameLinePreviousField", Rectangle.class, Rectangle.class);
        method.setAccessible(true);
        Rectangle rectangle1 = new Rectangle(10, 10, 20, 20);
        Rectangle rectangle2 = new Rectangle(30, 10, 40, 20);
        boolean isSameLinePreviousField = (boolean)method.invoke(formfiller, rectangle1, rectangle2);
        assertEquals(false, isSameLinePreviousField);
    }    
    */
    
    // Decode group name with numbering
    @Test
    public void testDecodeGroupName1() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        FormFiller formfiller = new FormFiller("");
        Method method = FormFiller.class.getDeclaredMethod("decodeGroupName", String.class);
        method.setAccessible(true);
        String resultGroupName = (String)method.invoke(formfiller, "Group Name 1");
        String expectedGroupName = "Group Name";
        assertEquals(expectedGroupName, resultGroupName);
    }   
    
    // Decode group name without numbering (exception thrown)
    @Test
    public void testDecodeGroupName2() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        FormFiller formfiller = new FormFiller("");
        Method method = FormFiller.class.getDeclaredMethod("decodeGroupName", String.class);
        method.setAccessible(true);
        String resultGroupName = (String)method.invoke(formfiller, "Group Name");
        String expectedGroupName = "Group Name";
        assertEquals(expectedGroupName, resultGroupName);
    }  
}

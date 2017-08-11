package micronet.tools.model.variables.test;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import micronet.tools.model.variables.CollectionDescription;
import micronet.tools.model.variables.ComponentDescription;
import micronet.tools.model.variables.EnumDescription;
import micronet.tools.model.variables.MapDescription;
import micronet.tools.model.variables.NumberDescription;
import micronet.tools.model.variables.NumberType;
import micronet.tools.model.variables.VariableDescription;
import micronet.tools.model.variables.VariableType;

/**
 * Unit test for VariableDescription.
 */
public class VariableTest extends TestCase
{

    public VariableTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( VariableTest.class );
    }

    public void testVariableDescription() {
    	VariableDescription string1 = new VariableDescription(VariableType.STRING);
    	VariableDescription string2 = new VariableDescription(VariableType.STRING);
    	VariableDescription bool = new VariableDescription(VariableType.BOOLEAN);
    	
        assertEquals(string1, string2);
        assertFalse(string1.equals(bool));
    }
    
    public void testNumberVariableDescription() {
    	VariableDescription float1 = new NumberDescription(NumberType.FLOAT);
    	VariableDescription float2 = new NumberDescription(NumberType.FLOAT);
    	VariableDescription integer = new NumberDescription(NumberType.INT);
    	VariableDescription string = new VariableDescription(VariableType.STRING);
    	
    	assertEquals(float1, float2);
    	assertFalse(float1.equals(integer));
    	assertFalse(float1.equals(string));
    }
    
    public void testCollectionVariableDescription() {
    	VariableDescription float1 = new NumberDescription(NumberType.FLOAT);
    	VariableDescription integer = new NumberDescription(NumberType.INT);
    	VariableDescription string = new VariableDescription(VariableType.STRING);
    	
    	VariableDescription floatList1 = new CollectionDescription(VariableType.LIST, float1);
    	VariableDescription floatList2 = new CollectionDescription(VariableType.LIST, float1);
    	VariableDescription intList = new CollectionDescription(VariableType.LIST, integer);
    	
    	assertEquals(floatList1, floatList2);
    	assertFalse(floatList1.equals(intList));
    	assertFalse(floatList1.equals(integer));
    	assertFalse(floatList1.equals(string));
    	
    	VariableDescription intSet1 = new CollectionDescription(VariableType.SET, integer);
    	VariableDescription intSet2 = new CollectionDescription(VariableType.SET, integer);
    	VariableDescription stringSet = new CollectionDescription(VariableType.SET, string);
    	
    	assertEquals(intSet1, intSet2);
    	assertFalse(intSet1.equals(stringSet));
    	assertFalse(intSet1.equals(intList));
    	assertFalse(intSet1.equals(floatList1));
    	assertFalse(intSet1.equals(integer));
    	assertFalse(intSet1.equals(string));
    }
    
    public void testComponentVariableDescription() {
    	VariableDescription component1 = new ComponentDescription("ComponentType1");
    	VariableDescription component2 = new ComponentDescription("ComponentType1");
    	VariableDescription component3 = new ComponentDescription("ComponentType2");
    	
    	VariableDescription integer = new NumberDescription(NumberType.INT);
    	VariableDescription string = new VariableDescription(VariableType.STRING);
    	VariableDescription intList = new CollectionDescription(VariableType.LIST, integer);
    	
    	assertEquals(component1, component2);
    	assertFalse(component1.equals(component3));
    	assertFalse(component1.equals(integer));
    	assertFalse(component1.equals(string));
    	assertFalse(component1.equals(intList));
    }
    
    public void testEnumVariableDescription() {
    	VariableDescription enum1 = new EnumDescription("EnumType1");
    	VariableDescription enum2 = new EnumDescription("EnumType1");
    	VariableDescription enum3 = new EnumDescription("EnumType2");
    	
    	VariableDescription integer = new NumberDescription(NumberType.INT);
    	VariableDescription string = new VariableDescription(VariableType.STRING);
    	VariableDescription intList = new CollectionDescription(VariableType.LIST, integer);
    	VariableDescription component = new ComponentDescription("ComponentType");
    	
    	assertEquals(enum1, enum2);
    	assertFalse(enum1.equals(enum3));
    	assertFalse(enum1.equals(integer));
    	assertFalse(enum1.equals(string));
    	assertFalse(enum1.equals(intList));
    	assertFalse(enum1.equals(component));
    }
    
    public void testMapVariableDescription() {
    	VariableDescription float1 = new NumberDescription(NumberType.FLOAT);
    	VariableDescription integer = new NumberDescription(NumberType.INT);
    	VariableDescription string = new VariableDescription(VariableType.STRING);
    	
    	VariableDescription intList = new CollectionDescription(VariableType.LIST, integer);
    	VariableDescription stringSet = new CollectionDescription(VariableType.SET, string);
    	
    	VariableDescription map1 = new MapDescription(integer, string);
    	VariableDescription map2 = new MapDescription(integer, string);
    	VariableDescription map3 = new MapDescription(integer, float1);
    	VariableDescription map4 = new MapDescription(string, string);
    	VariableDescription map5 = new MapDescription(string, intList);
    	
    	assertEquals(map1, map2);
    	assertFalse(map1.equals(map3));
    	assertFalse(map1.equals(map4));
    	assertFalse(map1.equals(map5));
    	assertFalse(map1.equals(float1));
    	assertFalse(map1.equals(integer));
    	assertFalse(map1.equals(string));
    	assertFalse(map1.equals(intList));
    	assertFalse(map1.equals(stringSet));
    }
}


















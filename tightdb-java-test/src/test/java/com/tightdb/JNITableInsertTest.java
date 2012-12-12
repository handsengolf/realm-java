package com.tightdb;

import java.nio.ByteBuffer;
import java.util.Date;

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;


public class JNITableInsertTest {

	public void verifyRow(TableBase tbl, long rowIndex, Object[] values) {
		assertTrue((Boolean)(values[0]) == tbl.getBoolean(0, rowIndex));
		assertEquals(((Number)values[1]).longValue(), tbl.getLong(1, rowIndex));
		assertEquals((String)values[2], tbl.getString(2, rowIndex));
		if (values[3] instanceof byte[])
			assertEquals((byte[])values[3], tbl.getBinaryByteArray(3, rowIndex));
		if (values[3] instanceof ByteBuffer)
			assertEquals((ByteBuffer)values[3], tbl.getBinaryByteBuffer(3, rowIndex));
		assertEquals(((Date)values[4]).getTime()/1000, tbl.getDate(4, rowIndex).getTime()/1000);

		Mixed mix1 = (Mixed)values[5];
		Mixed mix2 =  tbl.getMixed(5, rowIndex);
		assertTrue(mix1.equals(mix2));
		
		TableBase subTable = tbl.getSubTable(6,  rowIndex);
		Object[] subValues = (Object[])values[6];
		for (long i=0; i<subTable.size(); i++) {
			Object[] val = (Object[])subValues[(int) i];
			assertTrue(((Number)val[0]).longValue() == subTable.getLong(0, i));
			assertEquals(((String)val[1]), subTable.getString(1, i));
		}
		assertTrue(tbl.isValid());
	}
	
	@Test()
	public void ShouldInsertAndAddRows() {
		TableBase table = new TableBase();
		TableSpec tableSpec = new TableSpec();
		tableSpec.addColumn(ColumnType.ColumnTypeBool, "bool");
		tableSpec.addColumn(ColumnType.ColumnTypeInt, "number");
		tableSpec.addColumn(ColumnType.ColumnTypeString, "string");
		tableSpec.addColumn(ColumnType.ColumnTypeBinary, "Bin");
		tableSpec.addColumn(ColumnType.ColumnTypeDate, "date");
		tableSpec.addColumn(ColumnType.ColumnTypeMixed, "mix");
		TableSpec subspec = tableSpec.addSubtableColumn("sub");
		subspec.addColumn(ColumnType.ColumnTypeInt, "sub-num");
		subspec.addColumn(ColumnType.ColumnTypeString, "sub-str");
		table.updateFromSpec(tableSpec);
		
		ByteBuffer buf = ByteBuffer.allocateDirect(23);
		Mixed mixedSubTable = new Mixed(ColumnType.ColumnTypeTable);
		Date date = new Date();
		Mixed mixed = new Mixed(123);

		// Check subtable
		Object[][] subTblData = new Object[][] {{234, "row0"},
					 						 	{345, "row1"},
					 						 	{456, "row2"} };
		Object[] rowData0 = new Object[] {false, (short)2, "hi", buf, date, mixed, subTblData};
		table.addRow(rowData0);
		verifyRow(table, 0, rowData0);

		Object[] rowData1 = new Object[] {false, 7, "hi1", new byte[] {0,2,3}, date, new Mixed("mix1"), null};
		Object[] rowData2 = new Object[] {true, 12345567789L, "hello", new byte[] {0}, date, new Mixed(buf), null};
		Object[] rowData3 = new Object[] {false, (byte)17, "hi3", buf, date, mixedSubTable, null};
		table.insertRow(1, rowData1);
		table.addRow(rowData2);
		table.insertRow(3, rowData3);
		
		verifyRow(table, 1, rowData1);
		verifyRow(table, 2, rowData2);
		verifyRow(table, 3, rowData3);	
	}
	
	@Test()
	public void ShouldFailInsert() {
		TableBase table = new TableBase();
		TableSpec tableSpec = new TableSpec();
		tableSpec.addColumn(ColumnType.ColumnTypeBool, "bool");
		tableSpec.addColumn(ColumnType.ColumnTypeInt, "number");
		tableSpec.addColumn(ColumnType.ColumnTypeString, "string");
		tableSpec.addColumn(ColumnType.ColumnTypeBinary, "Bin");
		tableSpec.addColumn(ColumnType.ColumnTypeDate, "date");
		tableSpec.addColumn(ColumnType.ColumnTypeMixed, "mix");
		TableSpec subspec = tableSpec.addSubtableColumn("sub");
		subspec.addColumn(ColumnType.ColumnTypeInt, "sub-num");
		table.updateFromSpec(tableSpec);

		// Wrong number of parameters
		ByteBuffer buf = ByteBuffer.allocateDirect(23);
		try {
			table.insertRow(0, false);
			assertTrue(false);
		} catch (IllegalArgumentException e) {}
		
		// Wrong type of parameter (999 instead of bool)
		try {
			table.insertRow(0, 999, 1, "hi", buf, new Date(), new Mixed(123), null);
			assertTrue(false);
		} catch (IllegalArgumentException e) {}

		// Wrong type of parameter (bool instead of 1)
		try {
			table.insertRow(0, true, false, "hi", buf, new Date(), new Mixed(123), null);
			assertTrue(false);
		} catch (IllegalArgumentException e) {}

		// Wrong type of parameter (999 instead of string)
		try {
			table.insertRow(0, false, 1, 999, buf, new Date(), new Mixed(123), null);
			assertTrue(false);
		} catch (IllegalArgumentException e) {}

		// Wrong type of parameter (999 instead of Binary)
		try {
			table.insertRow(0, false, 1, "hi", 999, new Date(), new Mixed(123), null);
			assertTrue(false);
		} catch (IllegalArgumentException e) {}

		// Wrong type of parameter (999 instead of Date)
		try {
			table.insertRow(0, false, 1, "hi", buf, 999, new Mixed(123), null);
			assertTrue(false);
		} catch (IllegalArgumentException e) {}

		// Wrong type of parameter (999 instead of Mixed)
		try {
			table.insertRow(0, false, 1, "hi", buf, new Date(), 999, null);
			assertTrue(false);
		} catch (IllegalArgumentException e) {}

		// Wrong type of parameter (999 instead of subtable)
		try {
			table.insertRow(0, false, 1, "hi", buf, new Date(), new Mixed(123), 999);
			assertTrue(false);
		} catch (IllegalArgumentException e) {}		

		// Wrong type of parameter (String instead of subtable-Int)
		try {
			table.insertRow(0, false, 1, "hi", buf, new Date(), new Mixed(123), new Object[][] { {"err",2,3}} );
			assertTrue(false);
		} catch (IllegalArgumentException e) {}		

		// Wrong type of parameter (String instead of subtable-Int)
		try {
			table.insertRow(0, false, 1, "hi", buf, new Date(), new Mixed(123), new Object[] {1,2,3} );
			assertTrue(false);
		} catch (IllegalArgumentException e) {}		
	}
	
}

package com.seandbeach.stockticker.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(StockDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<>();
        tableNameHashSet.add(StockContract.StockEntry.TABLE_NAME);

        mContext.deleteDatabase(StockDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new StockDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // Have we created the table we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // Verify that the table has been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // If this fails, it means the database doesn't contain the stock table
        assertTrue("Error: Database was created without the stock entry table",
                tableNameHashSet.isEmpty());

        // Now, does the table contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + StockContract.StockEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> stockColumnHashSet = new HashSet<String>();
        stockColumnHashSet.add(StockContract.StockEntry._ID);
        stockColumnHashSet.add(StockContract.StockEntry.COLUMN_SYMBOL);
        stockColumnHashSet.add(StockContract.StockEntry.COLUMN_NAME);
        stockColumnHashSet.add(StockContract.StockEntry.COLUMN_LAST_TRADE_PRICE);
        stockColumnHashSet.add(StockContract.StockEntry.COLUMN_OPEN);
        stockColumnHashSet.add(StockContract.StockEntry.COLUMN_PREVIOUS_CLOSE);
        stockColumnHashSet.add(StockContract.StockEntry.COLUMN_CHANGE);
        stockColumnHashSet.add(StockContract.StockEntry.COLUMN_PERCENT_CHANGE);
        stockColumnHashSet.add(StockContract.StockEntry.COLUMN_DAY_LOW);
        stockColumnHashSet.add(StockContract.StockEntry.COLUMN_DAY_HIGH);
        stockColumnHashSet.add(StockContract.StockEntry.COLUMN_YEAR_LOW);
        stockColumnHashSet.add(StockContract.StockEntry.COLUMN_YEAR_LOW_CHANGE);
        stockColumnHashSet.add(StockContract.StockEntry.COLUMN_YEAR_LOW_CHANGE_PERCENT);
        stockColumnHashSet.add(StockContract.StockEntry.COLUMN_YEAR_HIGH);
        stockColumnHashSet.add(StockContract.StockEntry.COLUMN_YEAR_HIGH_CHANGE);
        stockColumnHashSet.add(StockContract.StockEntry.COLUMN_YEAR_HIGH_CHANGE_PERCENT);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            stockColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // If this fails, it means the database doesn't contain all of the required stock entry columns
        assertTrue("Error: The database doesn't contain all of the required stock entry columns",
                stockColumnHashSet.isEmpty());
        c.close();
        db.close();
    }

    public void testStockTable() {
        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        StockDbHelper dbHelper = new StockDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create ContentValues of what you want to insert
        // (you can use the createGoogleStockValues if you wish)
        ContentValues testValues = TestUtilities.createGoogleStockValues();

        // Third Step: Insert ContentValues into database and get a row ID back
        long stockRowId;
        stockRowId = db.insert(StockContract.StockEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(stockRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                StockContract.StockEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( "Error: No Records returned from location query", cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Location Query Validation Failed",
                cursor, testValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse( "Error: More than one record returned from location query",
                cursor.moveToNext() );

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();
    }
}

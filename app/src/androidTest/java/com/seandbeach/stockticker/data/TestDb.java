package com.seandbeach.stockticker.data;

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

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        location database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can uncomment out the "createNorthPoleLocationValues" function.  You can
        also make use of the ValidateCurrentRecord function from within TestUtilities.
    */
    public void testLocationTable() {
        // First step: Get reference to writable database

        // Create ContentValues of what you want to insert
        // (you can use the createNorthPoleLocationValues if you wish)

        // Insert ContentValues into database and get a row ID back

        // Query the database and receive a Cursor back

        // Move the cursor to a valid database row

        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)

        // Finally, close the cursor and database

    }

    /*
        Students:  Here is where you will build code to test that we can insert and query the
        database.  We've done a lot of work for you.  You'll want to look in TestUtilities
        where you can use the "createWeatherValues" function.  You can
        also make use of the validateCurrentRecord function from within TestUtilities.
     */
    public void testWeatherTable() {
        // First insert the location, and then use the locationRowId to insert
        // the weather. Make sure to cover as many failure cases as you can.

        // Instead of rewriting all of the code we've already written in testLocationTable
        // we can move this code to insertLocation and then call insertLocation from both
        // tests. Why move it? We need the code to return the ID of the inserted location
        // and our testLocationTable can only return void because it's a test.

        // First step: Get reference to writable database

        // Create ContentValues of what you want to insert
        // (you can use the createWeatherValues TestUtilities function if you wish)

        // Insert ContentValues into database and get a row ID back

        // Query the database and receive a Cursor back

        // Move the cursor to a valid database row

        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)

        // Finally, close the cursor and database
    }


    /*
        Students: This is a helper method for the testWeatherTable quiz. You can move your
        code from testLocationTable to here so that you can call this code from both
        testWeatherTable and testLocationTable.
     */
    public long insertLocation() {
        return -1L;
    }
}

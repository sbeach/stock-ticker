package com.seandbeach.stockticker.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import com.seandbeach.stockticker.data.StockContract.StockEntry;

/*
    Note: This is not a complete set of tests of the Sunshine ContentProvider, but it does test
    that at least the basic functionality has been implemented correctly.
    Students: Uncomment the tests in this class as you implement the functionality in your
    ContentProvider to make sure that you've implemented things reasonably correctly.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.
       Students: Replace the calls to deleteAllRecordsFromDB with this one after you have written
       the delete functionality in the ContentProvider.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                StockEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                StockEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Stock table during delete", 0, cursor.getCount());
        cursor.close();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecordsFromProvider();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
        Students: Uncomment this test to make sure you've correctly registered the WeatherProvider.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                StockProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: StockProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + StockContract.CONTENT_AUTHORITY,
                    providerInfo.authority, StockContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: StockProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
            This test doesn't touch the database.  It verifies that the ContentProvider returns
            the correct type for each type of URI that it can handle.
            Students: Uncomment this test to verify that your implementation of GetType is
            functioning correctly.
         */
    public void testGetType() {
        // content://com.seandbeach.stockticker/stock/
        String type = mContext.getContentResolver().getType(StockEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.seandbeach.stockticker/stock
        assertEquals("Error: the StockEntry CONTENT_URI should return StockEntry.CONTENT_TYPE",
                StockEntry.CONTENT_TYPE, type);

        String testSymbol = "GOOG";
        // content://com.seandbeach.stockticker/stock?Symbol="GOOG"
        type = mContext.getContentResolver().getType(StockEntry.buildStockWithSymbol(testSymbol));
        // vnd.android.cursor.item/com.seandbeach.stockticker/stock?Symbol="GOOG"
        assertEquals("Error: the StockEntry CONTENT_URI with location should return StockEntry.CONTENT_TYPE",
                StockEntry.CONTENT_TYPE, type);

        String testName = "Google Inc.";
        // content://com.seandbeach.stockticker/stock?Name="Google Inc."
        type = mContext.getContentResolver().getType(StockEntry.buildStockWithName(testName));
        // vnd.android.cursor.item/com.seandbeach.stockticker/stock?Name="Google Inc."
        assertEquals("Error: the StockEntry CONTENT_URI with location should return StockEntry.CONTENT_TYPE",
                StockEntry.CONTENT_TYPE, type);

        double testPrice = 560.50;
        // content://com.seandbeach.stockticker/stock?LastTradePriceOnly=560.50
        type = mContext.getContentResolver().getType(StockEntry.buildStockWithPrice(testPrice));
        // vnd.android.cursor.item/com.seandbeach.stockticker/stock?LastTradePriceOnly=560.50
        assertEquals("Error: the StockEntry CONTENT_URI with location and date should return StockEntry.CONTENT_TYPE",
                StockEntry.CONTENT_TYPE, type);
    }


    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.  Uncomment this test to see if the basic weather query functionality
        given in the ContentProvider is working correctly.
     */
    public void testBasicStockQuery() {
        // insert our test records into the database
        StockDbHelper dbHelper = new StockDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createGoogleStockValues();

        long stockRowId = db.insert(StockEntry.TABLE_NAME, null, testValues);
        assertTrue("Unable to Insert StockEntry into the Database", stockRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor stockCursor = mContext.getContentResolver().query(
                StockEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicStockQuery", stockCursor, testValues);
    }

    /*
        This test uses the provider to insert and then update the data. Uncomment this test to
        see if your update stock is functioning correctly.
     */
    public void testUpdateLocation() {
        // Create a new map of values, where column names are the keys
        ContentValues values = TestUtilities.createGoogleStockValues();

        Uri stockUri = mContext.getContentResolver().
                insert(StockEntry.CONTENT_URI, values);
        long stockRowId = ContentUris.parseId(stockUri);

        // Verify we got a row back.
        assertTrue(stockRowId != -1);
        Log.d(LOG_TAG, "New row id: " + stockRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(StockEntry._ID, stockRowId);
        updatedValues.put(StockEntry.COLUMN_LAST_TRADE_PRICE, "Santa's Village");

        // Create a cursor with observer to make sure that the content provider is notifying
        // the observers as expected
        Cursor locationCursor = mContext.getContentResolver().query(StockEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        locationCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                StockEntry.CONTENT_URI, updatedValues, StockEntry._ID + "= ?",
                new String[] { Long.toString(stockRowId)});
        assertEquals(count, 1);

        // Test to make sure our observer is called.  If not, we throw an assertion.
        //
        // Students: If your code is failing here, it means that your content provider
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();

        locationCursor.unregisterContentObserver(tco);
        locationCursor.close();

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                StockEntry.CONTENT_URI,
                null,   // projection
                StockEntry._ID + " = " + stockRowId,
                null,   // Values for the "where" clause
                null    // sort order
        );

        TestUtilities.validateCursor("testUpdateLocation.  Error validating location entry update.",
                cursor, updatedValues);

        cursor.close();
    }


    // Make sure we can still delete after adding/updating stuff
    public void testInsertReadProvider() {
        ContentValues testValues = TestUtilities.createGoogleStockValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(StockEntry.CONTENT_URI, true, tco);

        ContentValues stockValues = TestUtilities.createGoogleStockValues();
        // The TestContentObserver is a one-shot class
        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(StockEntry.CONTENT_URI, true, tco);

        Uri weatherInsertUri = mContext.getContentResolver()
                .insert(StockEntry.CONTENT_URI, stockValues);
        assertTrue(weatherInsertUri != null);

        // Did our content observer get called?
        // If this fails, insert stock in ContentProvider isn't calling
        // getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        // A cursor is your primary interface to the query results.
        Cursor stockCursor = mContext.getContentResolver().query(
                StockEntry.CONTENT_URI,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null // columns to group by
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating StockEntry insert.",
                stockCursor, stockValues);

        // Add the location values in with the weather data so that we can make
        // sure that the join worked and we actually get all the values back
        stockValues.putAll(testValues);

        // Get the joined Stock and Location data
        stockCursor = mContext.getContentResolver().query(
                StockEntry.buildStockWithSymbol(TestUtilities.TEST_SYMBOL),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Stock and Location Data.",
                stockCursor, stockValues);

        // Get the joined Stock and Location data with a start date
        stockCursor = mContext.getContentResolver().query(
                StockEntry.buildStockWithName(TestUtilities.TEST_NAME),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Stock and Location Data with start date.",
                stockCursor, stockValues);

        // Get the joined Stock data for a specific date
        stockCursor = mContext.getContentResolver().query(
                StockEntry.buildStockWithPrice(TestUtilities.TEST_PRICE),
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Stock and Location data for a specific date.",
                stockCursor, stockValues);
    }

    // Make sure we can still delete after adding/updating stuff
    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our location delete.
        TestUtilities.TestContentObserver stockObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(StockEntry.CONTENT_URI, true, stockObserver);

        deleteAllRecordsFromProvider();

        // Students: If either of these fail, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        stockObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(stockObserver);
    }


    static private final int BULK_INSERT_RECORDS_TO_INSERT = 5;
    static ContentValues[] createBulkInsertStockValues() {
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];
        String[] symbols = { "AAPL", "AMD", "GOOG", "INTC", "MSFT" };

        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++) {
            ContentValues stockValues = TestUtilities.createGoogleStockValues();
            stockValues.remove(StockEntry.COLUMN_SYMBOL);
            stockValues.put(StockEntry.COLUMN_SYMBOL, symbols[i]);
            returnContentValues[i] = stockValues;
        }
        return returnContentValues;
    }

    public void testBulkInsert() {
        ContentValues[] bulkInsertContentValues = createBulkInsertStockValues();

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver stockObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(StockEntry.CONTENT_URI, true, stockObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(StockEntry.CONTENT_URI, bulkInsertContentValues);

        // Students:  If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        stockObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(stockObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                StockEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                StockEntry.COLUMN_SYMBOL + " ASC"  // sort order == by SYMBOL ASCENDING
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext()) {
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating StockEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}

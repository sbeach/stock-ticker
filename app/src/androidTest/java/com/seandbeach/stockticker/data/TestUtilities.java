package com.seandbeach.stockticker.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.seandbeach.stockticker.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

public class TestUtilities extends AndroidTestCase {
    static final String TEST_SYMBOL = "GOOG";
    static final String TEST_NAME = "Google Inc.";
    static final double TEST_PRICE = 560.50;

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static ContentValues createGoogleStockValues() {
        // Create a new map of values, where column names are the keys
        ContentValues stockValues = new ContentValues();
        stockValues.put(StockContract.StockEntry.COLUMN_SYMBOL, TEST_SYMBOL);
        stockValues.put(StockContract.StockEntry.COLUMN_NAME, TEST_NAME);
        stockValues.put(StockContract.StockEntry.COLUMN_LAST_TRADE_PRICE, TEST_PRICE);
        stockValues.put(StockContract.StockEntry.COLUMN_OPEN, 537.04);
        stockValues.put(StockContract.StockEntry.COLUMN_PREVIOUS_CLOSE, 539.78);
        stockValues.put(StockContract.StockEntry.COLUMN_CHANGE, -1.42);
        stockValues.put(StockContract.StockEntry.COLUMN_PERCENT_CHANGE, 0.07);
        stockValues.put(StockContract.StockEntry.COLUMN_DAY_LOW, 531.45);
        stockValues.put(StockContract.StockEntry.COLUMN_DAY_HIGH, 538.63);
        stockValues.put(StockContract.StockEntry.COLUMN_YEAR_LOW, 486.23);
        stockValues.put(StockContract.StockEntry.COLUMN_YEAR_LOW_CHANGE, 45.88);
        stockValues.put(StockContract.StockEntry.COLUMN_YEAR_LOW_CHANGE_PERCENT, 9.44);
        stockValues.put(StockContract.StockEntry.COLUMN_YEAR_HIGH, 598.01);
        stockValues.put(StockContract.StockEntry.COLUMN_YEAR_HIGH_CHANGE, -65.90);
        stockValues.put(StockContract.StockEntry.COLUMN_YEAR_HIGH_CHANGE_PERCENT, -11.02);

        return stockValues;
    }

    static long insertGoogleStockValues(Context context) {
        // insert our test records into the database
        StockDbHelper dbHelper = new StockDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createGoogleStockValues();

        long locationRowId;
        locationRowId = db.insert(StockContract.StockEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert North Pole Location Values", locationRowId != -1);

        return locationRowId;
    }

    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}

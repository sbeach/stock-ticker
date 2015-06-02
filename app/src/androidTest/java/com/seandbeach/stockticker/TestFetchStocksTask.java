package com.seandbeach.stockticker;

/**
 * Created by sbeach on 6/1/15.
 */
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.seandbeach.stockticker.data.StockContract;

public class TestFetchStocksTask extends AndroidTestCase{
    static final String ADD_STOCK_SYMBOL = "GOOG";
    static final String ADD_STOCK_NAME = "Google Inc.";
    static final double ADD_STOCK_PRICE = 560.50;
    static final double ADD_STOCK_OPEN = 537.04;
    static final double ADD_STOCK_PREVIOUS_CLOSE = 539.78;
    static final double ADD_STOCK_CHANGE = -1.42;
    static final double ADD_STOCK_PERCENT_CHANGE = 0.07;
    static final double ADD_STOCK_DAY_LOW = 531.45;
    static final double ADD_STOCK_DAY_HIGH = 538.63;
    static final double ADD_STOCK_YEAR_LOW = 486.23;
    static final double ADD_STOCK_YEAR_LOW_CHANGE = 45.88;
    static final double ADD_STOCK_YEAR_LOW_CHANGE_PERCENT = 9.44;
    static final double ADD_STOCK_YEAR_HIGH = 598.01;
    static final double ADD_STOCK_YEAR_HIGH_CHANGE = -65.90;
    static final double ADD_STOCK_YEAR_HIGH_CHANGE_PERCENT = -11.02;

    public void testAddStock() {
        // start from a clean state
        getContext().getContentResolver().delete(StockContract.StockEntry.CONTENT_URI,
                StockContract.StockEntry.COLUMN_SYMBOL + " = ?",
                new String[]{ADD_STOCK_SYMBOL});

        FetchStocksTask fetchStocksTask = new FetchStocksTask(getContext());
        long stockId = fetchStocksTask.addStock(ADD_STOCK_SYMBOL, ADD_STOCK_NAME, ADD_STOCK_PRICE, ADD_STOCK_OPEN,
                ADD_STOCK_PREVIOUS_CLOSE, ADD_STOCK_CHANGE, ADD_STOCK_PERCENT_CHANGE,
                ADD_STOCK_DAY_LOW, ADD_STOCK_DAY_HIGH,
                ADD_STOCK_YEAR_LOW, ADD_STOCK_YEAR_LOW_CHANGE, ADD_STOCK_YEAR_LOW_CHANGE_PERCENT,
                ADD_STOCK_YEAR_HIGH, ADD_STOCK_YEAR_HIGH_CHANGE, ADD_STOCK_YEAR_HIGH_CHANGE_PERCENT);

        // does addLocation return a valid record ID?
        assertFalse("Error: addLocation returned an invalid ID on insert",
                stockId == -1);

        // test all this twice
        for ( int i = 0; i < 2; i++ ) {

            // does the ID point to our location?
            Cursor locationCursor = getContext().getContentResolver().query(
                    StockContract.StockEntry.CONTENT_URI,
                    new String[]{
                            StockContract.StockEntry._ID,
                            StockContract.StockEntry.COLUMN_SYMBOL,
                            StockContract.StockEntry.COLUMN_NAME,
                            StockContract.StockEntry.COLUMN_LAST_TRADE_PRICE,
                            StockContract.StockEntry.COLUMN_OPEN,
                            StockContract.StockEntry.COLUMN_PREVIOUS_CLOSE,
                            StockContract.StockEntry.COLUMN_CHANGE,
                            StockContract.StockEntry.COLUMN_CHANGE_PERCENT,
                            StockContract.StockEntry.COLUMN_DAY_LOW,
                            StockContract.StockEntry.COLUMN_DAY_HIGH,
                            StockContract.StockEntry.COLUMN_YEAR_LOW,
                            StockContract.StockEntry.COLUMN_YEAR_LOW_CHANGE,
                            StockContract.StockEntry.COLUMN_YEAR_LOW_CHANGE_PERCENT,
                            StockContract.StockEntry.COLUMN_YEAR_HIGH,
                            StockContract.StockEntry.COLUMN_YEAR_HIGH_CHANGE,
                            StockContract.StockEntry.COLUMN_YEAR_HIGH_CHANGE_PERCENT
                    },
                    StockContract.StockEntry.COLUMN_SYMBOL + " = ?",
                    new String[]{ADD_STOCK_SYMBOL},
                    null);

            // these match the indices of the projection
            if (locationCursor.moveToFirst()) {
                assertEquals("Error: the queried value of stockId does not match the returned value" +
                        "from addStock", locationCursor.getLong(0), stockId);
                assertEquals("Error: the queried value of symbol is incorrect",
                        locationCursor.getString(1), ADD_STOCK_SYMBOL);
                assertEquals("Error: the queried value of name is incorrect",
                        locationCursor.getString(2), ADD_STOCK_NAME);
                assertEquals("Error: the queried value of price is incorrect",
                        locationCursor.getDouble(3), ADD_STOCK_PRICE);
                assertEquals("Error: the queried value of open is incorrect",
                        locationCursor.getDouble(4), ADD_STOCK_OPEN);
                assertEquals("Error: the queried value of previous close is incorrect",
                        locationCursor.getDouble(5), ADD_STOCK_PREVIOUS_CLOSE);
                assertEquals("Error: the queried value of change is incorrect",
                        locationCursor.getDouble(6), ADD_STOCK_CHANGE);
                assertEquals("Error: the queried value of percent change is incorrect",
                        locationCursor.getDouble(7), ADD_STOCK_PERCENT_CHANGE);
                assertEquals("Error: the queried value of day low is incorrect",
                        locationCursor.getDouble(8), ADD_STOCK_DAY_LOW);
                assertEquals("Error: the queried value of day high is incorrect",
                        locationCursor.getDouble(9), ADD_STOCK_DAY_HIGH);
                assertEquals("Error: the queried value of year low is incorrect",
                        locationCursor.getDouble(10), ADD_STOCK_YEAR_LOW);
                assertEquals("Error: the queried value of year low change is incorrect",
                        locationCursor.getDouble(11), ADD_STOCK_YEAR_LOW_CHANGE);
                assertEquals("Error: the queried value of year low change percent is incorrect",
                        locationCursor.getDouble(12), ADD_STOCK_YEAR_LOW_CHANGE_PERCENT);
                assertEquals("Error: the queried value of year high is incorrect",
                        locationCursor.getDouble(13), ADD_STOCK_YEAR_HIGH);
                assertEquals("Error: the queried value of year high change is incorrect",
                        locationCursor.getDouble(14), ADD_STOCK_YEAR_HIGH_CHANGE);
                assertEquals("Error: the queried value of year high change percent is incorrect",
                        locationCursor.getDouble(15), ADD_STOCK_YEAR_HIGH_CHANGE_PERCENT);
            } else {
                fail("Error: the id you used to query returned an empty cursor");
            }

            // there should be no more records
            assertFalse("Error: there should be only one record returned from a location query",
                    locationCursor.moveToNext());

            // add the location again
            long newLocationId = fetchStocksTask.addStock(ADD_STOCK_SYMBOL, ADD_STOCK_NAME, ADD_STOCK_PRICE, ADD_STOCK_OPEN,
                    ADD_STOCK_PREVIOUS_CLOSE, ADD_STOCK_CHANGE, ADD_STOCK_PERCENT_CHANGE,
                    ADD_STOCK_DAY_LOW, ADD_STOCK_DAY_HIGH,
                    ADD_STOCK_YEAR_LOW, ADD_STOCK_YEAR_LOW_CHANGE, ADD_STOCK_YEAR_LOW_CHANGE_PERCENT,
                    ADD_STOCK_YEAR_HIGH, ADD_STOCK_YEAR_HIGH_CHANGE, ADD_STOCK_YEAR_HIGH_CHANGE_PERCENT);

            assertEquals("Error: inserting a location again should return the same ID",
                    stockId, newLocationId);
        }
        // reset our state back to normal
        getContext().getContentResolver().delete(StockContract.StockEntry.CONTENT_URI,
                StockContract.StockEntry.COLUMN_SYMBOL + " = ?",
                new String[]{ADD_STOCK_SYMBOL});

        // clean up the test so that other tests can use the content provider
        getContext().getContentResolver().
                acquireContentProviderClient(StockContract.StockEntry.CONTENT_URI).
                getLocalContentProvider().shutdown();
    }
}

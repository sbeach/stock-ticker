package com.seandbeach.stockticker.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Defines table and column names for the stocks database.
 */
public class StockContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.seandbeach.stockticker";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.seandbeach.stockticker/stock/ is a valid path for
    // looking at stock data.
    public static final String PATH_STOCK = "stock";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    // Inner class that defines the table contents of the stock table
    public static final class StockEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STOCK).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCK;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STOCK;

        public static final String TABLE_NAME = "stocks";

        public static final String COLUMN_SYMBOL = "Symbol";
        public static final String COLUMN_NAME = "Name";
        public static final String COLUMN_LAST_TRADE_PRICE = "LastTradePriceOnly";
        public static final String COLUMN_CHANGE = "Change";
        public static final String COLUMN_CHANGE_PERCENT = "PercentChange";
        public static final String COLUMN_OPEN = "Open";
        public static final String COLUMN_PREVIOUS_CLOSE = "PreviousClose";
        public static final String COLUMN_DAY_LOW = "DaysLow";
        public static final String COLUMN_DAY_HIGH = "DaysHigh";
        public static final String COLUMN_YEAR_LOW = "YearLow";
        public static final String COLUMN_YEAR_LOW_CHANGE = "ChangeFromYearLow";
        public static final String COLUMN_YEAR_LOW_CHANGE_PERCENT = "PercentChangeFromYearLow";
        public static final String COLUMN_YEAR_HIGH = "YearHigh";
        public static final String COLUMN_YEAR_HIGH_CHANGE = "ChangeFromYearHigh";
        // Yes, this is not spelled correctly; it is a typo in the Yahoo DB
        public static final String COLUMN_YEAR_HIGH_CHANGE_PERCENT = "PercebtChangeFromYearHigh";


        public static Uri buildStockUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildStockWithSymbol(String symbol) {
            return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_SYMBOL, symbol).build();
        }

        public static Uri buildStockWithName(String name) {
            return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_NAME, name).build();
        }

        public static Uri buildStockWithPrice(double price) {
            return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_LAST_TRADE_PRICE, String.valueOf(price)).build();
        }

        public static String getSymbolFromUri(Uri uri) {
            return uri.getQueryParameter(COLUMN_SYMBOL);
        }

        public static String getNameFromUri(Uri uri) {
            return uri.getQueryParameter(COLUMN_NAME);
        }

        public static double getPriceFromUri(Uri uri) {
            String priceString = uri.getQueryParameter(COLUMN_LAST_TRADE_PRICE);
            if (null != priceString && priceString.length() > 0) {
                return Double.parseDouble(priceString);
            } else {
                return 0;
            }
        }
    }
}

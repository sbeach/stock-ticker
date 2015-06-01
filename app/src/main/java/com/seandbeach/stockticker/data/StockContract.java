package com.seandbeach.stockticker.data;

import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Defines table and column names for the stocks database.
 */
public class StockContract {

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    /*
        Inner class that defines the table contents of the location table
        Students: This is where you will add the strings.  (Similar to what has been
        done for WeatherEntry)
     */
    public static final class SymbolEntry implements BaseColumns {
        public static final String TABLE_NAME = "symbols";

    }

    // Inner class that defines the table contents of the stock table
    public static final class StockEntry implements BaseColumns {

        public static final String TABLE_NAME = "stocks";

        public static final String COLUMN_SYMBOL = "Symbol";
        public static final String COLUMN_NAME = "Name";
        public static final String COLUMN_LAST_TRADE_PRICE = "LastTradePriceOnly";
        public static final String COLUMN_OPEN = "Open";
        public static final String COLUMN_PREVIOUS_CLOSE = "PreviousClose";
        public static final String COLUMN_CHANGE = "Change";
        public static final String COLUMN_PERCENT_CHANGE = "PercentChange";
        public static final String COLUMN_DAY_LOW = "DaysLow";
        public static final String COLUMN_DAY_HIGH = "DaysHigh";
        public static final String COLUMN_YEAR_LOW = "YearLow";
        public static final String COLUMN_YEAR_LOW_CHANGE = "ChangeFromYearLow";
        public static final String COLUMN_YEAR_LOW_CHANGE_PERCENT = "PercentChangeFromYearLow";
        public static final String COLUMN_YEAR_HIGH = "YearHigh";
        public static final String COLUMN_YEAR_HIGH_CHANGE = "ChangeFromYearHigh";
        // Yes, this is not spelled correctly; it is a typo in the Yahoo DB
        public static final String COLUMN_YEAR_HIGH_CHANGE_PERCENT = "PercebtChangeFromYearHigh";
    }
}

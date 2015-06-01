package com.seandbeach.stockticker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.seandbeach.stockticker.data.StockContract.StockEntry;

/**
 * Manages a local database for stock data.
 */
public class StockDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "stocks.db";

    public StockDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_STOCK_TABLE = "CREATE TABLE " + StockEntry.TABLE_NAME + " ("
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                + StockEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT"
                
                + ", " + StockEntry.COLUMN_SYMBOL + " TEXT UNIQUE NOT NULL ON CONFLICT IGNORE"

                + ", " + StockEntry.COLUMN_NAME + " TEXT NOT NULL"
                + ", " + StockEntry.COLUMN_LAST_TRADE_PRICE + " REAL NOT NULL"
                + ", " + StockEntry.COLUMN_OPEN + " REAL NOT NULL"
                + ", " + StockEntry.COLUMN_PREVIOUS_CLOSE + " REAL NOT NULL"
                + ", " + StockEntry.COLUMN_CHANGE + " REAL NOT NULL "
                + ", " + StockEntry.COLUMN_PERCENT_CHANGE + " REAL NOT NULL"
                + ", " + StockEntry.COLUMN_DAY_LOW + " REAL NOT NULL"
                + ", " + StockEntry.COLUMN_DAY_HIGH + " REAL NOT NULL"
                + ", " + StockEntry.COLUMN_YEAR_LOW + " REAL NOT NULL"
                + ", " + StockEntry.COLUMN_YEAR_LOW_CHANGE + " REAL NOT NULL"
                + ", " + StockEntry.COLUMN_YEAR_LOW_CHANGE_PERCENT + " REAL NOT NULL"
                + ", " + StockEntry.COLUMN_YEAR_HIGH + " REAL NOT NULL"
                + ", " + StockEntry.COLUMN_YEAR_HIGH_CHANGE + " REAL NOT NULL"
                + ", " + StockEntry.COLUMN_YEAR_HIGH_CHANGE_PERCENT + " REAL NOT NULL"

                + ");";

        sqLiteDatabase.execSQL(SQL_CREATE_STOCK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + StockEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}

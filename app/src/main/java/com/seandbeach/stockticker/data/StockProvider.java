package com.seandbeach.stockticker.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class StockProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private StockDbHelper mOpenHelper;

    static final int STOCK = 100;
    static final int STOCK_WITH_SYMBOL = 101;
    static final int STOCK_WITH_NAME = 102;
    static final int STOCK_WITH_PRICE = 103;

    private static final SQLiteQueryBuilder sStockBySymbolQueryBuilder;

    static{
        sStockBySymbolQueryBuilder = new SQLiteQueryBuilder();

        sStockBySymbolQueryBuilder.setTables(StockContract.StockEntry.TABLE_NAME);
    }

    //stock.Symbol = ?
    private static final String sStockSymbolSelection =
            StockContract.StockEntry.TABLE_NAME+
                    "." + StockContract.StockEntry.COLUMN_SYMBOL + " = ? ";

    //stock.Name = ?
    private static final String sStockNameSelection =
            StockContract.StockEntry.TABLE_NAME+
                    "." + StockContract.StockEntry.COLUMN_NAME + " = ? ";

    //stock.LastTradePriceOnly = ?
    private static final String sStockPriceSelection =
            StockContract.StockEntry.TABLE_NAME+
                    "." + StockContract.StockEntry.COLUMN_LAST_TRADE_PRICE + " = ? ";

    private Cursor getStockBySymbol(Uri uri, String[] projection, String sortOrder) {
        String symbol = StockContract.StockEntry.getSymbolFromUri(uri);

        String[] selectionArgs = new String[]{symbol};
        String selection = sStockSymbolSelection;

        return sStockBySymbolQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getStockByName(Uri uri, String[] projection, String sortOrder) {
        String name = StockContract.StockEntry.getNameFromUri(uri);

        String[] selectionArgs = new String[]{name};
        String selection = sStockNameSelection;

        return sStockBySymbolQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getStockByPrice(Uri uri, String[] projection, String sortOrder) {
        String price = String.valueOf(StockContract.StockEntry.getPriceFromUri(uri));

        String[] selectionArgs = new String[]{price};
        String selection = sStockPriceSelection;

        return sStockBySymbolQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = StockContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, StockContract.PATH_STOCK, STOCK);
        matcher.addURI(authority, StockContract.PATH_STOCK + "/*", STOCK_WITH_SYMBOL);
        matcher.addURI(authority, StockContract.PATH_STOCK + "/*", STOCK_WITH_NAME);
        matcher.addURI(authority, StockContract.PATH_STOCK + "/#", STOCK_WITH_PRICE);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new StockDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case STOCK:
                return StockContract.StockEntry.CONTENT_TYPE;
            case STOCK_WITH_SYMBOL:
                return StockContract.StockEntry.CONTENT_TYPE;
            case STOCK_WITH_NAME:
                return StockContract.StockEntry.CONTENT_TYPE;
            case STOCK_WITH_PRICE:
                return StockContract.StockEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "stock"
            case STOCK:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        StockContract.StockEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            // "stock/*"
            case STOCK_WITH_SYMBOL:
                retCursor = getStockBySymbol(uri, projection, sortOrder);
                break;
            // "stock/*"
            case STOCK_WITH_NAME:
                retCursor = getStockByName(uri, projection, sortOrder);
                break;
            // "stock/#"
            case STOCK_WITH_PRICE:
                retCursor = getStockByPrice(uri, projection, sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case STOCK: {
                long _id = db.insert(StockContract.StockEntry.TABLE_NAME, null, values);
                if ( _id > 0 ) {
                    returnUri = StockContract.StockEntry.buildStockUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        // this makes delete all rows return the number of rows deleted
        if (null == selection) {
            selection = "1";
        }

        switch (match) {
            case STOCK:
                rowsDeleted = db.delete(StockContract.StockEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case STOCK:
                rowsUpdated = db.update(StockContract.StockEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case STOCK:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(StockContract.StockEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}

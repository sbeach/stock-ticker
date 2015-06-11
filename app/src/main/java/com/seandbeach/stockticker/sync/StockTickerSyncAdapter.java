package com.seandbeach.stockticker.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.seandbeach.stockticker.R;
import com.seandbeach.stockticker.StockQuoteFragment;
import com.seandbeach.stockticker.data.StockContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class StockTickerSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = StockTickerSyncAdapter.class.getSimpleName();
    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 60 = 1 hour
    public static final int SYNC_INTERVAL = 60 * 60;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    public StockTickerSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync Called.");
        String sortOrder = StockContract.StockEntry.COLUMN_SYMBOL + " ASC";
        Uri stockUri = StockContract.StockEntry.CONTENT_URI;

        Cursor cursor = getContext().getContentResolver().query(
                stockUri,
                StockQuoteFragment.STOCK_COLUMNS,
                null,
                null,
                sortOrder);

        String[] params = new String[cursor.getCount()];
        if (!cursor.moveToFirst()) {
            cursor.close();
            return;
        }

        for (int i = 0; i < cursor.getCount(); i++) {
            params[i] = cursor.getString(StockQuoteFragment.COL_STOCK_SYMBOL);
            cursor.moveToNext();
        }
        cursor.close();

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String stockJsonStr;

        String format = "json";
        String selectors = " * "; // Name,Change,PercentChange,DaysLow,DaysHigh,YearLow,YearHigh,Open,PreviousClose,Symbol
        String symbols = "\"";
        for (String param : params) {
            if (symbols.length() > 1) {
                symbols += ",";
            }
            symbols += param;
        }
        symbols += "\"";
        String env = "store://datatables.org/alltableswithkeys";
        String callback = "";

        try {
            // Construct the URL for the Yahoo Finance query

            // https://query.yahooapis.com/v1/public/yql?
            // q=
            // select%20*%20
            // from%20yahoo.finance.quotes%20
            // where%20symbol%3D%22AMD%2CATVI%2CMSFT%2CSNE%22
            // &format=json
            // &env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys
            // &callback=

            final String QUOTE_BASE_URL = "https://query.yahooapis.com/v1/public/yql?";
            final String QUERY_PARAM = "q";
            final String SELECTION_PARAM = "select";
            final String TABLE_PARAM = "from yahoo.finance.quotes ";
            final String SYMBOL_PARAM = "where symbol=";
            final String FORMAT_PARAM = "format";
            final String ENV_PARAM = "env";
            final String CALLBACK_PARAM = "callback";

            Uri builtUri = Uri.parse(QUOTE_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, SELECTION_PARAM + selectors + " " + TABLE_PARAM + SYMBOL_PARAM + symbols + " ")
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(ENV_PARAM, env)
                    .appendQueryParameter(CALLBACK_PARAM, callback)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            stockJsonStr = buffer.toString();
            getStockDataFromJson(stockJsonStr);
        } catch (IOException | JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    private String getStockDataFromJson(String stockJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String YF_QUERY = "query";
        final String YF_CREATED = "created";
        final String YF_RESULTS = "results";
        final String YF_QUOTE = "quote";
        final String YF_SYMBOL = "Symbol";
        final String YF_NAME = "Name";
        final String YF_LAST_TRADE_PRICE = "LastTradePriceOnly";
        final String YF_OPEN = "Open";
        final String YF_PREVIOUS_CLOSE = "PreviousClose";
        final String YF_CHANGE = "Change";
        final String YF_PERCENT_CHANGE = "PercentChange";
        final String YF_DAY_LOW = "DaysLow";
        final String YF_DAY_HIGH = "DaysHigh";
        final String YF_YEAR_LOW = "YearLow";
        final String YF_YEAR_LOW_CHANGE = "ChangeFromYearLow";
        final String YF_YEAR_LOW_CHANGE_PERCENT = "PercentChangeFromYearLow";
        final String YF_YEAR_HIGH = "YearHigh";
        final String YF_YEAR_HIGH_CHANGE = "ChangeFromYearHigh";
        // Yes, this is spelled incorrectly; it is a typo in the Yahoo DB
        final String YF_YEAR_HIGH_CHANGE_PERCENT = "PercebtChangeFromYearHigh";

        try {
            JSONObject stockJson = new JSONObject(stockJsonStr);
            JSONArray quoteArray = stockJson.getJSONObject(YF_QUERY).getJSONObject(YF_RESULTS).getJSONArray(YF_QUOTE);
            String timeOfFetch = stockJson.getJSONObject(YF_QUERY).getString(YF_CREATED);

            // Insert the new stock information into the database
            Vector<ContentValues> cVVector = new Vector<>(quoteArray.length());

            for(int i = 0; i < quoteArray.length(); i++) {
                // Get the JSON object representing the day
                JSONObject quote = quoteArray.getJSONObject(i);

                // These are the values that will be collected.
                // For now, using the format "Name (symbol): $price (change, percentChange)"
                String symbol = quote.getString(YF_SYMBOL);
                String name = quote.getString(YF_NAME);
                double price = quote.getDouble(YF_LAST_TRADE_PRICE);
                double open = quote.getDouble(YF_OPEN);
                double previousClose = quote.getDouble(YF_PREVIOUS_CLOSE);
                double change = quote.getDouble(YF_CHANGE);
                double percentChange = Double.parseDouble(quote.getString(YF_PERCENT_CHANGE).replace("%",""));
                double dayLow = quote.getDouble(YF_DAY_LOW);
                double dayHigh = quote.getDouble(YF_DAY_HIGH);
                double yearLow = quote.getDouble(YF_YEAR_LOW);
                double yearLowChange = quote.getDouble(YF_YEAR_LOW_CHANGE);
                double yearLowChangePercent = Double.parseDouble(quote.getString(YF_YEAR_LOW_CHANGE_PERCENT).replace("%", ""));
                double yearHigh = quote.getDouble(YF_YEAR_HIGH);
                double yearHighChange = quote.getDouble(YF_YEAR_HIGH_CHANGE);
                double yearHighChangePercent = Double.parseDouble(quote.getString(YF_YEAR_HIGH_CHANGE_PERCENT).replace("%", ""));

                ContentValues stockValues = new ContentValues();

                stockValues.put(StockContract.StockEntry.COLUMN_SYMBOL, symbol);
                stockValues.put(StockContract.StockEntry.COLUMN_NAME, name);
                stockValues.put(StockContract.StockEntry.COLUMN_LAST_TRADE_PRICE, price);
                stockValues.put(StockContract.StockEntry.COLUMN_OPEN, open);
                stockValues.put(StockContract.StockEntry.COLUMN_PREVIOUS_CLOSE, previousClose);
                stockValues.put(StockContract.StockEntry.COLUMN_CHANGE, change);
                stockValues.put(StockContract.StockEntry.COLUMN_CHANGE_PERCENT, percentChange);
                stockValues.put(StockContract.StockEntry.COLUMN_DAY_LOW, dayLow);
                stockValues.put(StockContract.StockEntry.COLUMN_DAY_HIGH, dayHigh);
                stockValues.put(StockContract.StockEntry.COLUMN_YEAR_LOW, yearLow);
                stockValues.put(StockContract.StockEntry.COLUMN_YEAR_LOW_CHANGE, yearLowChange);
                stockValues.put(StockContract.StockEntry.COLUMN_YEAR_LOW_CHANGE_PERCENT, yearLowChangePercent);
                stockValues.put(StockContract.StockEntry.COLUMN_YEAR_HIGH, yearHigh);
                stockValues.put(StockContract.StockEntry.COLUMN_YEAR_HIGH_CHANGE, yearHighChange);
                stockValues.put(StockContract.StockEntry.COLUMN_YEAR_HIGH_CHANGE_PERCENT, yearHighChangePercent);

                cVVector.add(stockValues);
            }

            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = getContext().getContentResolver().bulkInsert(StockContract.StockEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "FetchWeatherTask complete. " + inserted + " inserted");

            return timeOfFetch;

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        // Since we've created an account
        StockTickerSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        // Without calling setSyncAutomatically, our periodic sync will not be enabled.
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        // Finally, let's do a sync to get things started
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}

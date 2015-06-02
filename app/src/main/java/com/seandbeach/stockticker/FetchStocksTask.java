package com.seandbeach.stockticker;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.seandbeach.stockticker.data.StockContract.StockEntry;

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

public class FetchStocksTask extends AsyncTask<String, Void, String> {

    private final String LOG_TAG = FetchStocksTask.class.getSimpleName();

    private final Context mContext;

    public FetchStocksTask(Context context) {
        mContext = context;
    }

    private boolean DEBUG = true;

    /**
     * Helper method to handle insertion of a new location in the weather database.
     *
     * @param symbol The location string used to request updates from the server.
     * @param name A human-readable city name, e.g "Mountain View"
     * @param price the latitude of the city
     * @param open the longitude of the city
     * @return the row ID of the added location.
     */
    long addStock(String symbol, String name, double price, double open,
                  double previousClose, double change, double percentChange,
                  double dayLow, double dayHigh,
                  double yearLow, double yearLowChange, double yearLowChangePercent,
                  double yearHigh, double yearHighChange, double yearHighChangePercent) {
        long locationId;

        // First, check if the location with this city name exists in the db
        Cursor locationCursor = mContext.getContentResolver().query(
                StockEntry.CONTENT_URI,
                new String[]{StockEntry._ID},
                StockEntry.COLUMN_SYMBOL + " = ?",
                new String[]{symbol},
                null);

        if (locationCursor.moveToFirst()) {
            int locationIdIndex = locationCursor.getColumnIndex(StockEntry._ID);
            locationId = locationCursor.getLong(locationIdIndex);
        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues stockValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            stockValues.put(StockEntry.COLUMN_SYMBOL, symbol);
            stockValues.put(StockEntry.COLUMN_NAME, name);
            stockValues.put(StockEntry.COLUMN_LAST_TRADE_PRICE, price);
            stockValues.put(StockEntry.COLUMN_OPEN, open);
            stockValues.put(StockEntry.COLUMN_PREVIOUS_CLOSE, previousClose);
            stockValues.put(StockEntry.COLUMN_CHANGE, change);
            stockValues.put(StockEntry.COLUMN_PERCENT_CHANGE, percentChange);
            stockValues.put(StockEntry.COLUMN_DAY_LOW, dayLow);
            stockValues.put(StockEntry.COLUMN_DAY_HIGH, dayHigh);
            stockValues.put(StockEntry.COLUMN_YEAR_LOW, yearLow);
            stockValues.put(StockEntry.COLUMN_YEAR_LOW_CHANGE, yearLowChange);
            stockValues.put(StockEntry.COLUMN_YEAR_LOW_CHANGE_PERCENT, yearLowChangePercent);
            stockValues.put(StockEntry.COLUMN_YEAR_HIGH, yearHigh);
            stockValues.put(StockEntry.COLUMN_YEAR_HIGH_CHANGE, yearHighChange);
            stockValues.put(StockEntry.COLUMN_YEAR_HIGH_CHANGE_PERCENT, yearHighChangePercent);

            // Finally, insert location data into the database.
            Uri insertedUri = mContext.getContentResolver().insert(StockEntry.CONTENT_URI, stockValues);

            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            locationId = ContentUris.parseId(insertedUri);
        }

        locationCursor.close();
        // Wait, that worked?  Yes!
        return locationId;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
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

                stockValues.put(StockEntry.COLUMN_SYMBOL, symbol);
                stockValues.put(StockEntry.COLUMN_NAME, name);
                stockValues.put(StockEntry.COLUMN_LAST_TRADE_PRICE, price);
                stockValues.put(StockEntry.COLUMN_OPEN, open);
                stockValues.put(StockEntry.COLUMN_PREVIOUS_CLOSE, previousClose);
                stockValues.put(StockEntry.COLUMN_CHANGE, change);
                stockValues.put(StockEntry.COLUMN_PERCENT_CHANGE, percentChange);
                stockValues.put(StockEntry.COLUMN_DAY_LOW, dayLow);
                stockValues.put(StockEntry.COLUMN_DAY_HIGH, dayHigh);
                stockValues.put(StockEntry.COLUMN_YEAR_LOW, yearLow);
                stockValues.put(StockEntry.COLUMN_YEAR_LOW_CHANGE, yearLowChange);
                stockValues.put(StockEntry.COLUMN_YEAR_LOW_CHANGE_PERCENT, yearLowChangePercent);
                stockValues.put(StockEntry.COLUMN_YEAR_HIGH, yearHigh);
                stockValues.put(StockEntry.COLUMN_YEAR_HIGH_CHANGE, yearHighChange);
                stockValues.put(StockEntry.COLUMN_YEAR_HIGH_CHANGE_PERCENT, yearHighChangePercent);

                cVVector.add(stockValues);
            }

            int inserted = 0;
            // add to database
            if (cVVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = mContext.getContentResolver().bulkInsert(StockEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "FetchWeatherTask complete. " + inserted + " inserted");

            return timeOfFetch;

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected String doInBackground(String... params) {

        // If there's no zip code, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            return null;
        }

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
        String timeFetched;

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
                return null;
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
                return null;
            }
            stockJsonStr = buffer.toString();
            timeFetched = getStockDataFromJson(stockJsonStr);
        } catch (IOException | JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            return null;
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
        return timeFetched;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null && mContext instanceof Activity) {
            View fragment = ((Activity) mContext).getFragmentManager().findFragmentById(R.id.fragment).getView();
            if (fragment != null) {
                TextView time = (TextView) fragment.findViewById(R.id.time);
                if (time != null) { time.setText(result); }
            }
        }
    }
}

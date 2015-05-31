package com.seandbeach.stockticker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * A placeholder fragment containing a simple view.
 */
public class StockQuoteFragment extends Fragment {

    private static final String LOG_TAG = StockQuoteFragment.class.getSimpleName();
    private ArrayList<String> stockValues;
    private ArrayAdapter<String> mStockQuoteAdapter;

    public StockQuoteFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateQuotes();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        stockValues = new ArrayList<>();
        // The ArrayAdapter will take data from a source (like our dummy forecast) and
        // use it to populate the ListView it's attached to.
        mStockQuoteAdapter =
                new ArrayAdapter<>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_quote, // The name of the layout ID.
                        R.id.list_item_quote_textview, // The ID of the textview to populate.
                        new ArrayList<String>());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        final ListView listView = (ListView) rootView.findViewById(R.id.listview_quotes);
        listView.setAdapter(mStockQuoteAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String quote = mStockQuoteAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, quote);
                startActivity(intent);
            }
        });
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            ArrayList<Integer> stocksToRemove = new ArrayList<>();

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (checked) {
                    stocksToRemove.add(position);
                } else {
                    stocksToRemove.remove(stocksToRemove.indexOf(position));
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu_listview_contextual_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                SparseBooleanArray checked = listView.getCheckedItemPositions();

                switch (item.getItemId()) {
                    case R.id.action_delete:
                        ArrayList<Integer> positions = new ArrayList<>();

                        for (int i = 0; i < checked.size(); i++) {
                            if (checked.valueAt(i)) {
                                positions.add(checked.keyAt(i));
                            }
                        }

                        Collections.sort(positions, Collections.reverseOrder());

                        Set<String> stocks = getSavedStocks();
                        for (int position : positions) {
                            String toRemove = stockValues.get(position);
                            mStockQuoteAdapter.remove(toRemove);
                            stocks.remove(toRemove.substring(toRemove.indexOf("(") + 1, toRemove.indexOf(")")));
                        }

                        saveStocks(stocks);
                        listView.clearChoices();
                        break;
                    default:
                        break;
                }

                mode.finish(); // Action picked, so close the CAB
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_stock:
                final EditText symbolInput = new EditText(getActivity());
                symbolInput.setHint(R.string.dialog_add_stock_hint);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.dialog_add_stock_title)
                        .setView(symbolInput)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked OK, so save the mSelectedItems results somewhere
                                // or return them to the component that opened the dialog
                                addNewStock(symbolInput.getText().toString());
                            }
                        })
                        .setNegativeButton(R.string.cancel, null);
                builder.create().show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Set<String> getSavedStocks() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String[] defaultStockArray = getResources().getStringArray(R.array.pref_stocks_displayed_default);
        HashSet<String> defaultStocks = new HashSet<>(Arrays.asList(defaultStockArray));
        Set<String> saved = prefs.getStringSet(getString(R.string.pref_stocks_displayed_key), defaultStocks);
        return saved;
    }

    private void saveStocks(Set<String> stocks) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.edit().putStringSet(getString(R.string.pref_stocks_displayed_key), stocks).apply();
    }

    private boolean isMobileDataEnabled() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return prefs.getBoolean(getString(R.string.pref_mobile_data_key), false);
    }

    private void addNewStock(String newStock) {
        Set<String> stocks = getSavedStocks();
        stocks.add(newStock);
        saveStocks(stocks);
        updateQuotes();
    }

    private void updateQuotes() {
        Set<String> stocks = getSavedStocks();

        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (!isMobileDataEnabled() && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            Toast.makeText(getActivity(), "Mobile data usage is disabled", Toast.LENGTH_SHORT).show();
        } else {
            FetchQuoteTask weatherTask = new FetchQuoteTask();
            weatherTask.execute(stocks.toArray(new String[stocks.size()]));
        }
    }

    public class FetchQuoteTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchQuoteTask.class.getSimpleName();

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getStockDataFromJson(String quoteJsonStr, int numQuotes)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String YF_QUERY = "query";
            final String YF_RESULTS = "results";
            final String YF_QUOTE = "quote";
            final String YF_SYMBOL = "Symbol";
            final String YF_NAME = "Name";
            final String YF_LAST_TRADE_PRICE = "LastTradePriceOnly";
            final String YF_CHANGE = "Change";
            final String YF_PERCENT_CHANGE = "PercentChange";
            final String YF_DAY_LOW = "DaysLow";
            final String YF_DAY_HIGH = "DaysHigh";
            final String YF_YEAR_LOW = "YearLow";
            final String YF_YEAR_HIGH = "YearHigh";
            final String YF_OPEN = "Open";
            final String YF_PREVIOUS_CLOSE = "PreviousClose";

            JSONObject quoteJson = new JSONObject(quoteJsonStr);
            JSONArray quoteArray = quoteJson.getJSONObject(YF_QUERY).getJSONObject(YF_RESULTS).getJSONArray(YF_QUOTE);

            String[] resultStrs = new String[numQuotes];
            for (int i = 0; i < quoteArray.length(); i++) {
                // For now, using the format "Name (symbol): $price (change, percentChange)"
                String name;
                String symbol;
                String price;
                String change;
                String percentChange;
                String open;
                String previousClose;
                String dayHigh;
                String dayLow;
                String yearHigh;
                String yearLow;

                // Get the JSON object representing the day
                JSONObject quote = quoteArray.getJSONObject(i);

                name = quote.getString(YF_NAME);
                symbol = quote.getString(YF_SYMBOL);
                price = "$" + quote.getString(YF_LAST_TRADE_PRICE);
                change = quote.getString(YF_CHANGE);
                percentChange = quote.getString(YF_PERCENT_CHANGE);
                open = "$" + quote.getString(YF_OPEN);
                previousClose = "$" + quote.getString(YF_PREVIOUS_CLOSE);
                dayHigh = "$" + quote.getString(YF_DAY_HIGH);
                dayLow = "$" + quote.getString(YF_DAY_LOW);
                yearHigh = "$" + quote.getString(YF_YEAR_HIGH);
                yearLow = "$" + quote.getString(YF_YEAR_LOW);

                resultStrs[i] = ((name != null && !name.isEmpty() && !name.equals("null")) ? (name + " (" + symbol + "):") : "(" + symbol + "):")
                        + price + " (" + change + ", " + percentChange + ")"
//                        + "; Open: " + open
//                        + "; Previous close: " + previousClose
//                        + "; Today: High - " + dayHigh + ", Low - " + dayLow
//                        + "; Year: High - " + yearHigh + ", Low - " + yearLow
                ;
            }
            return resultStrs;
        }

        @Override
        protected String[] doInBackground(String... params) {

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String quoteJsonStr = null;

            String format = "json";
            String selectors = " * "; // Name,Ask,Change,PercentChange,DaysLow,DaysHigh,YearLow,YearHigh,Open,PreviousClose,Symbol
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

                // Create the request to Yahoo Finance, and open the connection
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
                quoteJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the stock data, there's no point in attempting
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

            try {
                return getStockDataFromJson(quoteJsonStr, params.length);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            if (strings != null) {
                mStockQuoteAdapter.clear();
                mStockQuoteAdapter.addAll(strings);
                stockValues.clear();
                stockValues.addAll(Arrays.asList(strings));
            }
        }
    }
}

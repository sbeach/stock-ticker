package com.seandbeach.stockticker;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.seandbeach.stockticker.data.StockContract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * A placeholder fragment containing a simple view.
 */
public class StockQuoteFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String STOCK_QUOTE_FRAGMENT_TAG = "SQF_TAG";
    private static final String LOG_TAG = StockQuoteFragment.class.getSimpleName();
    private static final int STOCK_LOADER = 0;
    // For the main stock view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    static final String[] STOCK_COLUMNS = {
            StockContract.StockEntry._ID,
            StockContract.StockEntry.COLUMN_SYMBOL,
            StockContract.StockEntry.COLUMN_NAME,
            StockContract.StockEntry.COLUMN_LAST_TRADE_PRICE,
            StockContract.StockEntry.COLUMN_CHANGE,
            StockContract.StockEntry.COLUMN_CHANGE_PERCENT,
            StockContract.StockEntry.COLUMN_OPEN,
            StockContract.StockEntry.COLUMN_PREVIOUS_CLOSE,
            StockContract.StockEntry.COLUMN_DAY_HIGH,
            StockContract.StockEntry.COLUMN_DAY_LOW,
            StockContract.StockEntry.COLUMN_YEAR_LOW,
            StockContract.StockEntry.COLUMN_YEAR_LOW_CHANGE,
            StockContract.StockEntry.COLUMN_YEAR_LOW_CHANGE_PERCENT,
            StockContract.StockEntry.COLUMN_YEAR_HIGH,
            StockContract.StockEntry.COLUMN_YEAR_HIGH_CHANGE,
            StockContract.StockEntry.COLUMN_YEAR_HIGH_CHANGE_PERCENT
    };

    // These indices are tied to STOCK_COLUMNS. If STOCK_COLUMNS changes, these must change.
    static final int COL_STOCK_ID = 0;
    static final int COL_STOCK_SYMBOL = 1;
    static final int COL_STOCK_NAME = 2;
    static final int COL_STOCK_PRICE = 3;
    static final int COL_STOCK_CHANGE = 4;
    static final int COL_STOCK_CHANGE_PERCENT = 5;
    static final int COL_STOCK_OPEN = 6;
    static final int COL_STOCK_PREVIOUS_CLOSE = 7;
    static final int COL_STOCK_DAY_LOW = 8;
    static final int COL_STOCK_DAY_HIGH = 9;
    static final int COL_STOCK_YEAR_LOW = 10;
    static final int COL_STOCK_YEAR_LOW_CHANGE = 11;
    static final int COL_STOCK_YEAR_LOW_CHANGE_PERCENT = 12;
    static final int COL_STOCK_YEAR_HIGH = 13;
    static final int COL_STOCK_YEAR_HIGH_CHANGE = 14;
    static final int COL_STOCK_YEAR_HIGH_CHANGE_PERCENT = 15;

    private ArrayList<String> stockValues;
    private StockAdapter mStockAdapter;

    public StockQuoteFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // The CursorAdapter will take data from our cursor and populate the ListView.
        mStockAdapter = new StockAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        final ListView listView = (ListView) rootView.findViewById(R.id.listview_quotes);
        listView.setAdapter(mStockAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .setData(StockContract.StockEntry.buildStockWithSymbol(cursor.getString(COL_STOCK_SYMBOL)));
                    startActivity(intent);
                }
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
                        // TODO: Implement removal from DB
//                        ArrayList<Integer> positions = new ArrayList<>();
//
//                        for (int i = 0; i < checked.size(); i++) {
//                            if (checked.valueAt(i)) {
//                                positions.add(checked.keyAt(i));
//                            }
//                        }
//
//                        Collections.sort(positions, Collections.reverseOrder());
//
//                        Set<String> stocks = getSavedStocks();
//                        for (int position : positions) {
//                            String toRemove = stockValues.get(position);
//                            mStockAdapter.remove(toRemove);
//                            stocks.remove(toRemove.substring(0, toRemove.indexOf(")")));
//                        }
//
//                        saveStocks(stocks);
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
        updateStocks();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(STOCK_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void updateStocks() {
        Set<String> stocks = getSavedStocks();

        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (!isMobileDataEnabled() && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            Toast.makeText(getActivity(), "Mobile data usage is disabled", Toast.LENGTH_SHORT).show();
        } else {
            FetchStocksTask stockTask = new FetchStocksTask(getActivity());
            stockTask.execute(stocks.toArray(new String[stocks.size()]));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Sort order:  Ascending, by date.
        String sortOrder = StockContract.StockEntry.COLUMN_SYMBOL + " ASC";
        Uri stockUri = StockContract.StockEntry.CONTENT_URI;

        return new CursorLoader(getActivity(),
                stockUri,
                STOCK_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mStockAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mStockAdapter.swapCursor(null);
    }
}

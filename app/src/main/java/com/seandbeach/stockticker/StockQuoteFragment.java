package com.seandbeach.stockticker;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
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
import com.seandbeach.stockticker.sync.StockTickerSyncAdapter;

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
    private static final String SELECTED_KEY = "selected_position";

    private static final int STOCK_LOADER = 0;
    // For the main stock view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    public static final String[] STOCK_COLUMNS = {
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
    public static final int COL_STOCK_ID = 0;
    public static final int COL_STOCK_SYMBOL = 1;
    public static final int COL_STOCK_NAME = 2;
    public static final int COL_STOCK_PRICE = 3;
    public static final int COL_STOCK_CHANGE = 4;
    public static final int COL_STOCK_CHANGE_PERCENT = 5;
    public static final int COL_STOCK_OPEN = 6;
    public static final int COL_STOCK_PREVIOUS_CLOSE = 7;
    public static final int COL_STOCK_DAY_LOW = 8;
    public static final int COL_STOCK_DAY_HIGH = 9;
    public static final int COL_STOCK_YEAR_LOW = 10;
    public static final int COL_STOCK_YEAR_LOW_CHANGE = 11;
    public static final int COL_STOCK_YEAR_LOW_CHANGE_PERCENT = 12;
    public static final int COL_STOCK_YEAR_HIGH = 13;
    public static final int COL_STOCK_YEAR_HIGH_CHANGE = 14;
    public static final int COL_STOCK_YEAR_HIGH_CHANGE_PERCENT = 15;

    private StockAdapter mStockAdapter;
    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;

    public StockQuoteFragment() {
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri dateUri);
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
        mListView = (ListView) rootView.findViewById(R.id.listview_quotes);
        mListView.setAdapter(mStockAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity())
                            .onItemSelected(StockContract.StockEntry
                                    .buildStockWithSymbol(cursor.getString(COL_STOCK_SYMBOL)));
                }
                mPosition = position;
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

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
                SparseBooleanArray checked = mListView.getCheckedItemPositions();

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
                        mListView.clearChoices();
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
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
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
                                addNewStock(symbolInput.getText().toString().toUpperCase());
                            }
                        })
                        .setNegativeButton(R.string.cancel, null);
                builder.create().show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isMobileDataEnabled() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return prefs.getBoolean(getString(R.string.pref_mobile_data_key), false);
    }

    private void addNewStock(String newStock) {
        double placeholder = 0.00;

        ContentValues stockValues = new ContentValues();

        // Since the data will be fetched immediately after this method,
        // we can simply insert placeholder data. It will be overwritten on sync.
        stockValues.put(StockContract.StockEntry.COLUMN_SYMBOL, newStock);
        stockValues.put(StockContract.StockEntry.COLUMN_NAME, "");
        stockValues.put(StockContract.StockEntry.COLUMN_LAST_TRADE_PRICE, placeholder);
        stockValues.put(StockContract.StockEntry.COLUMN_OPEN, placeholder);
        stockValues.put(StockContract.StockEntry.COLUMN_PREVIOUS_CLOSE, placeholder);
        stockValues.put(StockContract.StockEntry.COLUMN_CHANGE, placeholder);
        stockValues.put(StockContract.StockEntry.COLUMN_CHANGE_PERCENT, placeholder);
        stockValues.put(StockContract.StockEntry.COLUMN_DAY_LOW, placeholder);
        stockValues.put(StockContract.StockEntry.COLUMN_DAY_HIGH, placeholder);
        stockValues.put(StockContract.StockEntry.COLUMN_YEAR_LOW, placeholder);
        stockValues.put(StockContract.StockEntry.COLUMN_YEAR_LOW_CHANGE, placeholder);
        stockValues.put(StockContract.StockEntry.COLUMN_YEAR_LOW_CHANGE_PERCENT, placeholder);
        stockValues.put(StockContract.StockEntry.COLUMN_YEAR_HIGH, placeholder);
        stockValues.put(StockContract.StockEntry.COLUMN_YEAR_HIGH_CHANGE, placeholder);
        stockValues.put(StockContract.StockEntry.COLUMN_YEAR_HIGH_CHANGE_PERCENT, placeholder);

        getActivity().getContentResolver().insert(StockContract.StockEntry.CONTENT_URI, stockValues);

        updateStocks();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(STOCK_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void updateStocks() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (!isMobileDataEnabled() && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            Toast.makeText(getActivity(), "Mobile data usage is disabled", Toast.LENGTH_SHORT).show();
        } else {
            StockTickerSyncAdapter.syncImmediately(getActivity());
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
        if (mPosition != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mStockAdapter.swapCursor(null);
    }
}

package com.seandbeach.stockticker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * A placeholder fragment containing a simple view.
 */
public class StockQuoteFragment extends Fragment {

    private static final String LOG_TAG = StockQuoteFragment.class.getSimpleName();
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
    public void onStart() {
        super.onStart();
        updateStocks();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Sort order:  Ascending, by date.
        String sortOrder = StockContract.StockEntry.COLUMN_SYMBOL + " ASC";

        Cursor cur = getActivity().getContentResolver().query(StockContract.StockEntry.CONTENT_URI,
            null, null, null, sortOrder);

        // The CursorAdapter will take data from our cursor and populate the ListView
        // However, we cannot use FLAG_AUTO_REQUERY since it is deprecated, so we will end
        // up with an empty list the first time we run.
        mStockAdapter = new StockAdapter(getActivity(), cur, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        final ListView listView = (ListView) rootView.findViewById(R.id.listview_quotes);
        listView.setAdapter(mStockAdapter);
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
}

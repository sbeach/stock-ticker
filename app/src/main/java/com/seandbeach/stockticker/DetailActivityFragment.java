package com.seandbeach.stockticker;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String YAHOO_FINANCE_URL_BASE = "http://finance.yahoo.com/q?s=";
    public static final String GOOGLE_PLAY_BITLINK = "http://bit.ly/1GtB1Ns";

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private static final int DETAIL_LOADER = 0;
    private String mShareString;
    private ShareActionProvider mShareActionProvider;

    private TextView mSymbolView;
    private TextView mNameView;
    private TextView mPriceView;
    private TextView mChangeView;
    private TextView mChangePercentView;
    private TextView mOpenView;
    private TextView mPreviousCloseView;
    private TextView mDayLowView;
    private TextView mDayHighView;
    private TextView mYearLowView;
    private TextView mYearLowChangeView;
    private TextView mYearLowChangePercentView;
    private TextView mYearHighView;
    private TextView mYearHighChangeView;
    private TextView mYearHighChangePercentView;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mSymbolView = (TextView) rootView.findViewById(R.id.detail_symbol);
        mNameView = (TextView) rootView.findViewById(R.id.detail_name);
        mPriceView = (TextView) rootView.findViewById(R.id.detail_price);
        mChangeView = (TextView) rootView.findViewById(R.id.detail_change);
        mChangePercentView = (TextView) rootView.findViewById(R.id.detail_change_percent);
        mOpenView = (TextView) rootView.findViewById(R.id.detail_open);
        mPreviousCloseView = (TextView) rootView.findViewById(R.id.detail_previous_close);
        mDayLowView = (TextView) rootView.findViewById(R.id.detail_day_low);
        mDayHighView = (TextView) rootView.findViewById(R.id.detail_day_high);
        mYearLowView = (TextView) rootView.findViewById(R.id.detail_year_low);
        mYearLowChangeView = (TextView) rootView.findViewById(R.id.detail_year_low_change);
        mYearLowChangePercentView = (TextView) rootView.findViewById(R.id.detail_year_low_change_percent);
        mYearHighView = (TextView) rootView.findViewById(R.id.detail_year_high);
        mYearHighChangeView = (TextView) rootView.findViewById(R.id.detail_year_high_change);
        mYearHighChangePercentView = (TextView) rootView.findViewById(R.id.detail_year_high_change_percent);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_detail_fragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (mShareString != null) {
            mShareActionProvider.setShareIntent(createShareQuoteIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }
    }

    private Intent createShareQuoteIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mShareString);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return null;
        }

        return new CursorLoader(getActivity(),
                intent.getData(),
                StockQuoteFragment.STOCK_COLUMNS,
                intent.getData().getQuery(),
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {

            String name = cursor.getString(StockQuoteFragment.COL_STOCK_NAME);
            if (name != null && !name.isEmpty() && !name.equals("null")) {
                mNameView.setText(name);
            } else {
                mNameView.setVisibility(View.GONE);
            }

            String symbol = cursor.getString(StockQuoteFragment.COL_STOCK_SYMBOL);
//            if (getActivity().getActionBar() != null) {
//                getActivity().getActionBar().setTitle(symbol);
//                mSymbolView.setVisibility(View.GONE);
//            } else if (getActivity() instanceof AppCompatActivity
//                    && ((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
//                ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(symbol);
//                mSymbolView.setVisibility(View.GONE);
//            } else {
//                mSymbolView.setText(symbol);
//            }
            mSymbolView.setText(symbol);

            String pricePattern = "\u00A4#,##0.00";
            String changePattern = "#,##0.00";
            String changePercentPattern = "#,##0.00%";

            DecimalFormat fmt = new DecimalFormat(pricePattern);

            double price = cursor.getDouble(StockQuoteFragment.COL_STOCK_PRICE);
            mPriceView.setText(fmt.format(price));

            double previousClose = cursor.getDouble(StockQuoteFragment.COL_STOCK_PREVIOUS_CLOSE);
            mPreviousCloseView.setText(fmt.format(previousClose));

            double open = cursor.getDouble(StockQuoteFragment.COL_STOCK_OPEN);
            mOpenView.setText(fmt.format(open));

            double low = cursor.getDouble(StockQuoteFragment.COL_STOCK_DAY_LOW);
            mDayLowView.setText(fmt.format(low));

            double high = cursor.getDouble(StockQuoteFragment.COL_STOCK_DAY_HIGH);
            mDayHighView.setText(fmt.format(high));

            double yearLow = cursor.getDouble(StockQuoteFragment.COL_STOCK_YEAR_LOW);
            mYearLowView.setText(fmt.format(yearLow));

            double yearHigh = cursor.getDouble(StockQuoteFragment.COL_STOCK_YEAR_HIGH);
            mYearHighView.setText(fmt.format(yearHigh));

            fmt.applyPattern(changePattern);

            double change = cursor.getDouble(StockQuoteFragment.COL_STOCK_CHANGE);
            mChangeView.setText(fmt.format(change));

            double yearLowChange = cursor.getDouble(StockQuoteFragment.COL_STOCK_YEAR_LOW_CHANGE);
            mYearLowChangeView.setText(fmt.format(yearLowChange));

            double yearHighChange = cursor.getDouble(StockQuoteFragment.COL_STOCK_YEAR_HIGH_CHANGE);
            mYearHighChangeView.setText(fmt.format(yearHighChange));

            fmt.applyPattern(changePercentPattern);

            double quoteChangePercent = cursor.getDouble(StockQuoteFragment.COL_STOCK_CHANGE_PERCENT);
            mChangePercentView.setText("(" + fmt.format(quoteChangePercent / 100) + ")");

            double yearLowChangePercent = cursor.getDouble(StockQuoteFragment.COL_STOCK_YEAR_LOW_CHANGE_PERCENT);
            mYearLowChangePercentView.setText("(" + fmt.format(yearLowChangePercent / 100) + ")");

            double yearHighChangePercent = cursor.getDouble(StockQuoteFragment.COL_STOCK_YEAR_HIGH_CHANGE_PERCENT);
            mYearHighChangePercentView.setText("(" + fmt.format(yearHighChangePercent / 100) + ")");


            fmt.applyPattern(changePattern);
            mShareString = symbol + " stock"
                    + (change > 0 ? " up " : " down ")
                    + fmt.format(Math.abs(change)) + "\n"
                    + YAHOO_FINANCE_URL_BASE + symbol
            + "\n\nGet Stock Ticker for Android:\n" + GOOGLE_PLAY_BITLINK;

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareQuoteIntent());
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) { }
}

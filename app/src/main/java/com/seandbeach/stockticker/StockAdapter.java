package com.seandbeach.stockticker;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * {@link StockAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class StockAdapter extends CursorAdapter {
    public StockAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    // This is ported from FetchStockTask --- but now we go straight from the cursor to the string.
    private String convertCursorRowToUXFormat(Cursor cursor) {
        String name = cursor.getString(StockQuoteFragment.COL_STOCK_NAME);

        return cursor.getString(StockQuoteFragment.COL_STOCK_SYMBOL)
                + (name != null && !name.isEmpty() && !name.equals("null") ? (" (" + name + "): ") : ": ")
                + cursor.getDouble(StockQuoteFragment.COL_STOCK_PRICE)
                + " ("
                    + cursor.getDouble(StockQuoteFragment.COL_STOCK_CHANGE)
                    + ", " + cursor.getDouble(StockQuoteFragment.COL_STOCK_CHANGE_PERCENT) + "%"
                + ")";
    }

    // Remember that these views are reused as needed.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_quote, parent, false);

        return view;
    }

    // This is where we fill-in the views with the contents of the cursor.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.
        TextView tv = (TextView)view;
        tv.setText(convertCursorRowToUXFormat(cursor));
    }
}

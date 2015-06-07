package com.seandbeach.stockticker;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;

/**
 * {@link StockAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class StockAdapter extends CursorAdapter {
    public StockAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public static class StockQuoteHolder {
        public final TextView symbolView;
        public final TextView nameView;
        public final TextView priceView;
        public final TextView changeView;
        public final TextView changePercentView;

        public StockQuoteHolder(View view) {
            symbolView = (TextView) view.findViewById(R.id.list_item_stock_symbol);
            nameView = (TextView) view.findViewById(R.id.list_item_stock_name);
            priceView = (TextView) view.findViewById(R.id.list_item_quote_price);
            changeView = (TextView) view.findViewById(R.id.list_item_quote_change);
            changePercentView = (TextView) view.findViewById(R.id.list_item_quote_change_percent);
        }
    }

    // Remember that these views are reused as needed.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_quote, parent, false);
        StockQuoteHolder stockQuoteHolder = new StockQuoteHolder(view);
        view.setTag(stockQuoteHolder);
        return view;
    }

    // This is where we fill-in the views with the contents of the cursor.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        StockQuoteHolder holder = (StockQuoteHolder) view.getTag();

        String stockSymbol = cursor.getString(StockQuoteFragment.COL_STOCK_SYMBOL);
        holder.symbolView.setText(stockSymbol);

        String stockName = cursor.getString(StockQuoteFragment.COL_STOCK_NAME);
        holder.nameView.setText(stockName);;

        String pricePattern = "\u00A4#,##0.00";
        DecimalFormat fmt = new DecimalFormat(pricePattern);
        double quotePrice = cursor.getDouble(StockQuoteFragment.COL_STOCK_PRICE);
        holder.priceView.setText(fmt.format(quotePrice));

        String changePattern = "#,##0.00";
        fmt.applyPattern(changePattern);
        double quoteChange = cursor.getDouble(StockQuoteFragment.COL_STOCK_CHANGE);
        holder.changeView.setText(fmt.format(quoteChange));

        String changePercentPattern = "#,##0.00%";
        fmt.applyPattern(changePercentPattern);
        double quoteChangePercent = cursor.getDouble(StockQuoteFragment.COL_STOCK_CHANGE_PERCENT);
        holder.changePercentView.setText("(" + fmt.format(quoteChangePercent / 100) + ")");

        if (quoteChange >= 0) {
            holder.changeView.setTextColor(context.getResources().getColor(R.color.stock_change_positive));
            holder.changePercentView.setTextColor(context.getResources().getColor(R.color.stock_change_positive));
        } else {
            holder.changeView.setTextColor(context.getResources().getColor(R.color.stock_change_negative));
            holder.changePercentView.setTextColor(context.getResources().getColor(R.color.stock_change_negative));
        }
    }
}

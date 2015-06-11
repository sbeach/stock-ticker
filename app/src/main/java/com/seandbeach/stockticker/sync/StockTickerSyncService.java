package com.seandbeach.stockticker.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class StockTickerSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static StockTickerSyncAdapter stockTickerSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("SunshineSyncService", "onCreate - SunshineSyncService");
        synchronized (sSyncAdapterLock) {
            if (stockTickerSyncAdapter == null) {
                stockTickerSyncAdapter = new StockTickerSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return stockTickerSyncAdapter.getSyncAdapterBinder();
    }
}
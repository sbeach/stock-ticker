package com.seandbeach.stockticker.network.service;


import com.seandbeach.stockticker.network.response.Query;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

public interface YahooFinanceV1Service {

    @GET("/{path}")
    Observable<Query> getQuote(@Path(value = "path") String path);

}

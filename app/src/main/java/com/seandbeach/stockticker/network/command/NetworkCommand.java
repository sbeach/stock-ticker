package com.seandbeach.stockticker.network.command;

import com.seandbeach.stockticker.network.response.Query;

import rx.Observable;

public interface NetworkCommand {

    Observable<Query> run();
}

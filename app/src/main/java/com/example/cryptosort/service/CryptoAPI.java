package com.example.cryptosort.service;

import com.example.cryptosort.model.CryptoModel;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface CryptoAPI {
    //GET,POST,UPDATE,DELETE
    //https://api.nomics.com/v1/currencies/ticker?key=demo-26240835858194712a4f8cc0dc635c7a&interval=1d,30&per-page=100&page=1
    @GET("currencies/ticker?key=0f941184a4272d3b28b8735cd2978aad&interval=1d,30&status=active")
    Observable<List<CryptoModel>> getData();


}

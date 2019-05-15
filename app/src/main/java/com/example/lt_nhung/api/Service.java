package com.example.lt_nhung.api;

import com.example.lt_nhung.model.Record;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface Service {
    @GET("weather")
    Observable<Record> getRecord(@QueryMap Map<String,String> options);
    @POST("weather")
    Observable<Record> sendRecord(@QueryMap Map<String,String> options);
}

package com.example.lt_nhung.api;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.lt_nhung.Config.BASE_URL;

public class RetrofitInstance {
    private static Retrofit retrofit;
    public static Retrofit getRetrofit() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://vtcc.ai/voice/api/asr/v1/rest/decode_file")
                .post(null)
                .addHeader("Content-Type", "audio/vnd.wave")
                .addHeader("token", "z-44QoH3eIf-ovEGom6q4A7dPZYfFuuCl9s6i4A9A9bqV1-nSY7x5nJVzRsPh1WR")
                .addHeader("User-Agent", "PostmanRuntime/7.13.0")
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Postman-Token", "f7b666a9-01b5-4fc2-97b7-2bca47b053e7,c4b844f5-370a-4683-8019-db6f3d5d12cf")
                .addHeader("Host", "vtcc.ai")
                .addHeader("accept-encoding", "gzip, deflate")
                .addHeader("content-length", "161324")
                .addHeader("Connection", "keep-alive")
                .addHeader("cache-control", "no-cache")
                .build();
        if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

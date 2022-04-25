package com.example.weathercheck

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RetrofitInstanceFactory {
    private var retrofit:Retrofit?=null

    fun  instance():Retrofit{

        val okHttpClientBuilder=OkHttpClient.Builder()

        val appInterceptor=AppIDInterceptor()

        okHttpClientBuilder.addInterceptor(appInterceptor)

        if (retrofit==null){
            retrofit=Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(MoshiConverterFactory.create())
                .client(okHttpClientBuilder.build())
                .build()

        }
        return retrofit!!
    }
}
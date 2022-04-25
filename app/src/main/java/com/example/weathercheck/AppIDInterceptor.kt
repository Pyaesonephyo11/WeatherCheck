package com.example.weathercheck

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class AppIDInterceptor :Interceptor {

    companion object{
        private const val API_KEY="eff4c23a63ae5e0550cc30d86262d2fa"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val url=chain.request().url

        val newUrl=url.newBuilder()
            .addQueryParameter("appid", API_KEY)
            .build()

        val request=Request.Builder().url(newUrl).build()
        return chain.proceed(request)

    }
}
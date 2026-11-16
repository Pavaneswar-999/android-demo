package com.example.androiddemo.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.net.ssl.HttpsURLConnection

object NetworkClient {

    private const val BASE_URL = "https://flood-api.openmeteo.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .hostnameVerifier { hostname, session ->
            hostname.endsWith("openmeteo.com") || HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session)
        }
        .build()

    val floodApi: OpenMeteoFloodApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenMeteoFloodApi::class.java)
    }
}

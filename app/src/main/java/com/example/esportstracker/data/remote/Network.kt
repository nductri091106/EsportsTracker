package com.example.esportstracker.data.remote

import com.example.esportstracker.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object Network {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Authorization", "Bearer ${BuildConfig.PANDASCORE_KEY}")
                .build()
            chain.proceed(request)
        }
        .build()

    val api: PandaScoreApi = Retrofit.Builder()
        .baseUrl("https://api.pandascore.co/")
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(PandaScoreApi::class.java)
}
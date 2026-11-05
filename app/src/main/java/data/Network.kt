package com.example.esportstracker.data

import com.example.esportstracker.data.PandaScoreApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.example.esportstracker.BuildConfig

object Network {
    private val json = Json { ignoreUnknownKeys = true }

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
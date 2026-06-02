package com.stephan.mobil.data.api

import android.content.Context
import com.stephan.mobil.security.SecurityUtil
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // ⚠️ PORT 8000 requis — Laravel est lancé avec: php artisan serve --host=0.0.0.0 --port=8000
    private const val BASE_URL = "http://192.168.209.90:8000/api/"

    private var retrofit: Retrofit? = null

    /** Réinitialise le client (utile si l'IP change) */
    fun reset() { retrofit = null }

    fun getClient(context: Context): Retrofit {
        if (retrofit == null) {

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val authInterceptor = Interceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                val token = SecurityUtil.getAuthToken(context)
                if (!token.isNullOrEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                requestBuilder.addHeader("X-Device-Id", SecurityUtil.getDeviceId(context))
                requestBuilder.addHeader("Accept", "application/json")
                chain.proceed(requestBuilder.build())
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}

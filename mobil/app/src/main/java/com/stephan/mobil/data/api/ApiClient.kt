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
    // Dev: HTTP local. En production, remplacer par https:// et activer le certificate pinning.
    private const val BASE_URL = "http://192.168.1.146:8000/api/"
    private const val PROD_HOST = "api.scpay.mg"

    // SHA-256 pins à remplacer par le vrai certificat en production (keytool / openssl)
    private val CERT_PINS = listOf(
        "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=", // leaf
        "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=", // backup
    )

    private val certificatePinner = CertificatePinner.Builder()
        .apply { CERT_PINS.forEach { add(PROD_HOST, it) } }
        .build()

    private var retrofit: Retrofit? = null

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

            val builder = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)

            // Activate certificate pinning only when targeting the production host (HTTPS)
            if (BASE_URL.startsWith("https://")) {
                builder.certificatePinner(certificatePinner)
            }

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}

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
    // Remplacer par l'IP de la machine locale ou du serveur de production
    private const val BASE_URL = "https://10.0.2.2:8000/api/" 
    
    // Remplacer par le hash SHA-256 réel du certificat du serveur pour le pinning
    private const val CERT_HASH = "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=" 
    private const val HOSTNAME = "10.0.2.2" // ou votre nom de domaine

    private var retrofit: Retrofit? = null

    fun getClient(context: Context): Retrofit {
        if (retrofit == null) {
            
            // 1. Certificate Pinning pour forcer un certificat spécifique (HTTPS forcé et sécurisé)
            val certificatePinner = CertificatePinner.Builder()
                .add(HOSTNAME, CERT_HASH)
                .build()

            // 2. Logging (utile en debug)
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // 3. Interceptor pour ajouter le Token Sanctum dans l'en-tête (Bearer)
            val authInterceptor = Interceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                
                // Récupération sécurisée du token depuis EncryptedSharedPreferences
                val token = SecurityUtil.getAuthToken(context)
                if (!token.isNullOrEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                
                requestBuilder.addHeader("Accept", "application/json")
                chain.proceed(requestBuilder.build())
            }

            // Configuration du client OkHttp
            val okHttpClient = OkHttpClient.Builder()
                .certificatePinner(certificatePinner) // Pinning actif
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            // Configuration de Retrofit
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}

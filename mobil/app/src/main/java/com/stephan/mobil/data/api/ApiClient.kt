package com.stephan.mobil.data.api

import android.content.Context
import com.stephan.mobil.BuildConfig
import com.stephan.mobil.security.SecurityUtil
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val PREFS_NAME = "scpay_app"
    private const val KEY_CUSTOM_URL = "custom_api_url"

    // URL et host injectés via BuildConfig (debug = local.properties, release = production HTTPS)
    private val PROD_HOST get() = BuildConfig.API_HOST

    fun getBaseUrl(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            .getString(KEY_CUSTOM_URL, null)
            ?.takeIf { it.isNotBlank() }
            ?: BuildConfig.API_BASE_URL

    fun saveCustomUrl(context: Context, url: String) {
        context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            .edit().putString(KEY_CUSTOM_URL, url.trimEnd('/') + "/").apply()
        reset()
    }

    // Pins SHA-256 du certificat de production.
    // Obtenir avec : openssl s_client -connect api.scpay.mg:443 | openssl x509 -pubkey -noout | openssl pkey -pubin -outform DER | openssl dgst -sha256 -binary | base64
    private val CERT_PINS = listOf(
        "sha256/REPLACE_WITH_REAL_LEAF_CERT_PIN=",   // certificat leaf
        "sha256/REPLACE_WITH_REAL_BACKUP_PIN=",       // certificat intermédiaire / backup
    )

    private val certificatePinner = CertificatePinner.Builder()
        .apply { CERT_PINS.forEach { add(PROD_HOST, it) } }
        .build()

    private var retrofit: Retrofit? = null
    private var builtForUrl: String = ""

    fun reset() { retrofit = null; builtForUrl = "" }

    fun getClient(context: Context): Retrofit {
        val currentUrl = getBaseUrl(context)
        if (retrofit == null || builtForUrl != currentUrl) {
            builtForUrl = currentUrl

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
                .addInterceptor(authInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)

            // Logs HTTP uniquement en debug
            if (BuildConfig.ENABLE_HTTP_LOGGING) {
                builder.addInterceptor(
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                )
            }

            // Certificate pinning uniquement en HTTPS (production)
            if (currentUrl.startsWith("https://")) {
                builder.certificatePinner(certificatePinner)
            }

            retrofit = Retrofit.Builder()
                .baseUrl(currentUrl)
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}

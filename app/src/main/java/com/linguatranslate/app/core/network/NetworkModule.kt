package com.linguatranslate.app.core.network

import com.linguatranslate.app.BuildConfig
import com.linguatranslate.app.data.remote.api.TranslationApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CONNECT_TIMEOUT_SECONDS = 15L
    private const val READ_TIMEOUT_SECONDS = 30L

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            // Bounded retry: OkHttp's default connection-failure retry is fine;
            // we deliberately do NOT add unbounded application-level retry.
            .retryOnConnectionFailure(true)

        if (BuildConfig.DEBUG_LOGGING) {
            val logging = HttpLoggingInterceptor().apply {
                // Body-level logging only in debug builds, and even then this
                // never leaves the device - it's local Logcat only. Still,
                // avoid logging full user text where possible.
                level = HttpLoggingInterceptor.Level.BASIC
            }
            builder.addInterceptor(logging)
        }

        // Attaches the shared app credential to every backend request.
        // See core/network/README notes / backend middleware/apiKeyAuth.ts:
        // this is a real barrier against anonymous scraping/abuse, not a
        // substitute for per-user authentication - a secret baked into a
        // distributed APK can in principle be extracted by a determined
        // attacker who decompiles the app.
        builder.addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("X-App-Key", BuildConfig.APP_API_KEY)
                .build()
            chain.proceed(request)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit {
        val baseUrl = if (BuildConfig.DEBUG) BuildConfig.BASE_URL_DEBUG else BuildConfig.BASE_URL_RELEASE
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideTranslationApi(retrofit: Retrofit): TranslationApi =
        retrofit.create(TranslationApi::class.java)
}

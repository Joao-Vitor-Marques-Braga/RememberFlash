package com.rememberflash.app.di

import com.rememberflash.app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @OptIn(io.github.jan.supabase.annotations.SupabaseInternal::class, io.github.jan.supabase.annotations.SupabaseExperimental::class)
    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        val url = if (BuildConfig.SUPABASE_URL.startsWith("http://") || BuildConfig.SUPABASE_URL.startsWith("https://")) {
            BuildConfig.SUPABASE_URL
        } else {
            "https://placeholder.supabase.co"
        }
        val key = BuildConfig.SUPABASE_ANON_KEY.ifBlank { "placeholder-anon-key" }

        return createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = key
        ) {
            httpEngine = io.ktor.client.engine.okhttp.OkHttp.create {
                config {
                    retryOnConnectionFailure(true)
                    connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                }
            }
            requestTimeout = kotlin.time.Duration.parse("30s")
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }

    @Provides
    @Singleton
    fun provideSupabasePostgrest(client: SupabaseClient): Postgrest {
        return client.postgrest
    }

    @Provides
    @Singleton
    fun provideSupabaseAuth(client: SupabaseClient): Auth {
        return client.auth
    }

    @Provides
    @Singleton
    fun provideSupabaseStorage(client: SupabaseClient): Storage {
        return client.storage
    }
}

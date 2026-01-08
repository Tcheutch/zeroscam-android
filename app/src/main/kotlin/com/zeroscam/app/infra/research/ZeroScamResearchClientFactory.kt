package com.zeroscam.app.infra.research

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ZeroScamResearchClientFactory {
    fun create(baseUrl: String): ZeroScamResearchApi {
        val moshi =
            Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

        val retrofit =
            Retrofit.Builder()
                .baseUrl(baseUrl.ensureEndsWithSlash())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

        return retrofit.create(ZeroScamResearchApi::class.java)
    }
}

/**
 * Retrofit exige un baseUrl qui finit par "/".
 */
private fun String.ensureEndsWithSlash(): String = if (endsWith("/")) this else "$this/"

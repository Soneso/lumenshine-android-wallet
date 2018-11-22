package com.soneso.lumenshine.networking

import android.text.TextUtils
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.soneso.lumenshine.BuildConfig
import com.soneso.lumenshine.networking.api.LsApi
import okhttp3.OkHttpClient
import okhttp3.internal.platform.Platform
import timber.log.Timber
import java.util.concurrent.TimeUnit

object NetworkUtil {

    const val TAG = "NetworkUtil"

    fun isNetworkAvailable(): Boolean {
        val runtime = Runtime.getRuntime()
        try {
            val ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
            val exitValue = ipProcess.waitFor()
            return exitValue == 0
        } catch (e: Exception) {
            Timber.e(e)
        }

        return false
    }

    fun lsHttpClient(): OkHttpClient {

        val okHttpBuilder = OkHttpClient.Builder()

        if (BuildConfig.DEBUG) {
            val interceptor = LoggingInterceptor.Builder()
                    .loggable(BuildConfig.DEBUG)
                    .setLevel(Level.BASIC)
                    .log(Platform.WARN)
                    .request("Ls Request")
                    .response("Ls Response")
                    .enableAndroidStudio_v3_LogsHack(true) // enable fix for logCat logging issues with pretty format /
                    .build()
            okHttpBuilder.addInterceptor(interceptor)
        }

        okHttpBuilder.connectTimeout(60, TimeUnit.SECONDS)
        okHttpBuilder.writeTimeout(60, TimeUnit.SECONDS)
        okHttpBuilder.readTimeout(60, TimeUnit.SECONDS)

        okHttpBuilder.addInterceptor { chain ->

            val request = chain.request()
            val requestBuilder = request.newBuilder()

            if (TextUtils.isEmpty(request.header(LsApi.HEADER_NAME_CONTENT_TYPE))) {
                requestBuilder.addHeader(LsApi.HEADER_NAME_CONTENT_TYPE, LsApi.HEADER_VALUE_CONTENT_TYPE)
            }

            if (TextUtils.isEmpty(request.header(LsApi.HEADER_NAME_AUTHORIZATION))) {
                requestBuilder.addHeader(LsApi.HEADER_NAME_AUTHORIZATION, LsSessionProfile.jwtToken)
            }

            if (TextUtils.isEmpty(request.header(LsApi.HEADER_NAME_LANGUAGE))) {
                requestBuilder.addHeader(LsApi.HEADER_NAME_LANGUAGE, LsSessionProfile.langKey)
            }

            chain.proceed(requestBuilder.build())
        }
        return okHttpBuilder.build()
    }
}
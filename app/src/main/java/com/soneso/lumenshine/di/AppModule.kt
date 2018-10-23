package com.soneso.lumenshine.di

import android.content.Context
import android.text.SpannableStringBuilder
import androidx.room.Room
import com.commonsware.cwac.saferoom.SafeHelperFactory
import com.soneso.lumenshine.networking.NetworkUtil
import com.soneso.lumenshine.networking.api.SgApi
import com.soneso.lumenshine.networking.dto.Parse
import com.soneso.lumenshine.persistence.SgPrefs
import com.soneso.lumenshine.persistence.room.DbNames
import com.soneso.lumenshine.persistence.room.LsDatabase
import dagger.Module
import dagger.Provides
import org.stellar.sdk.Server
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import javax.inject.Singleton

/**
 * Dagger module.
 * Created by cristi.paval on 3/9/18.
 */
@Module
class AppModule(private val context: Context) {

    @Provides
    fun provideContext() = context

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {

        // cristi.paval, 3/28/18 - retrofit builder
        return Retrofit.Builder()
                .baseUrl(SgApi.BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create(Parse.createMapper()))
                .client(NetworkUtil.sgHttpClient())
                .build()!!
    }

    @Provides
    @Singleton
    fun provideDatabase(): LsDatabase {

        val factory = SafeHelperFactory.fromUser(SpannableStringBuilder(SgPrefs.appPass))

        return Room.databaseBuilder(context, LsDatabase::class.java, DbNames.DB_NAME)
                .openHelperFactory(factory)
                .allowMainThreadQueries()
                .build()
    }

    @Provides
    @Singleton
    fun provideStellarServer(): Server {
        return Server("https://horizon-testnet.stellar.org")
    }
}
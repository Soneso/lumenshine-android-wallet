package com.soneso.lumenshine

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.soneso.lumenshine.di.AppModule
import com.soneso.lumenshine.di.DaggerAppComponent
import org.bouncycastle.jce.provider.BouncyCastleProvider
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.security.Security



/**
 * Custom App class.
 * Created by cristi.paval on 3/8/18.
 */
class LsApp : MultiDexApplication() {

    val appComponent = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()!!

    override fun onCreate() {
        super.onCreate()

        sAppContext = applicationContext

        Security.removeProvider("BC")
        Security.insertProviderAt(BouncyCastleProvider(), 1)

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }

    companion object {
        lateinit var sAppContext: Context
            private set
    }
}
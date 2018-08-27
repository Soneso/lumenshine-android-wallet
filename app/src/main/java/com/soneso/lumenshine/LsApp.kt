package com.soneso.lumenshine

import android.content.Context
import android.support.multidex.MultiDexApplication
import com.soneso.lumenshine.di.AppModule
import com.soneso.lumenshine.di.DaggerAppComponent
import org.bouncycastle.jce.provider.BouncyCastleProvider
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
    }

    companion object {
        lateinit var sAppContext: Context
            private set
    }
}
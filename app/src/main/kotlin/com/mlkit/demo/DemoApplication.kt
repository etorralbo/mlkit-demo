package com.mlkit.demo

import android.app.Application
import com.mlkit.demo.di.demoModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@DemoApplication)
            modules(demoModule)
        }
    }
}

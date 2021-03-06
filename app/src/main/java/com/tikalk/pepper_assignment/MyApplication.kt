package com.tikalk.pepper_assignment

import android.app.Application

/**
 * Created by motibartov on 14/01/2018.
 */

class MyApplication : Application() {

    lateinit var component: AppComponent
    override fun onCreate() {
        super.onCreate()
        component = DaggerAppComponent.builder().
                appModule(AppModule(this)).
                build()
    }
}
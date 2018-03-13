package com.bartovapps.wetherapp

import com.bartovapps.wetherapp.api.ApiModule
import com.bartovapps.wetherapp.forcast.ForecastFragment
import com.bartovapps.wetherapp.forcast.ForecastModule
import dagger.Component
import javax.inject.Singleton

/**
 * Created by motibartov on 14/01/2018.
 */

@Component(modules = arrayOf(AppModule::class, ApiModule::class, ForecastModule::class))
@Singleton
interface AppComponent {

    fun injectForecast(target: ForecastFragment)
}
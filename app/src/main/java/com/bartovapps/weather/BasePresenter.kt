package com.bartovapps.weather

/**
 * Created by motibartov on 14/01/2018.
 */
interface BasePresenter {
    fun subscribe(view: BaseView)
    fun unsubscribe()
}
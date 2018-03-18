package com.bartovapps.weather.forcast

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.text.format.DateUtils
import android.util.Log.i
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.bartovapps.weather.MyApplication

import com.bartovapps.weather.R
import com.bartovapps.weather.api.ApiModule
import com.bartovapps.weather.api.ApiService
import com.bartovapps.weather.model.forecast.Forecast
import com.bartovapps.weather.model.daily_forecast.DailyForecast
import com.bartovapps.weather.settings.PreferencesHelper
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_main_weather.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class ForecastFragment : Fragment(), ForecastContract.View {

    @Inject
    lateinit var presenter: ForecastPresenter

    @Inject
    lateinit var dailyAdapter: DailyForcastAdapter

    @Inject
    lateinit var weeklyAdapter: WeeklyForecastAdapter

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject lateinit var preferencesHelper: PreferencesHelper

    @Inject lateinit var sdf : SimpleDateFormat

    @Inject lateinit var viewModelFactory: ForecastViewModelFactory

    lateinit var viewModel: ForecastViewModel

    var forceUpdate : Boolean = true
    companion object {
        val TAG = "ForecastFragment"
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        (activity?.application as MyApplication).component.injectForecast(this)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ForecastViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_weather, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        i(TAG, "onViewCreated: ")
        spLocation.setSelection(getSelectionPosition(preferencesHelper.getLocation()))
        spLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                i(TAG, "onNothingSelected: nothing selected")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                val selectedItem: String = spLocation.selectedItem as String
                val period = preferencesHelper.getPeriod()
                i(TAG, "onItemSelected: Item selected: ${spLocation.selectedItem}")

                presenter.loadForecastForLocation(ApiService.cities[selectedItem], period, forecastDirty(selectedItem))
            }
        }

        rvDailyForecast.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        rvDailyForecast.adapter = dailyAdapter

        rvWeeklyForecast.layoutManager = LinearLayoutManager(activity)
        rvWeeklyForecast.adapter = weeklyAdapter
    }

    override fun onResume() {
        super.onResume()
        presenter.subscribe(this)
    }

    override fun onPause() {
        super.onPause()
        presenter.unsubscribe()
    }

    override fun showDailyForecast(dailyForecast: List<DailyForecast>?) {
//        i(TAG, "Loaded forecast: $dailyForecast")
        val today = dailyForecast?.get(0)

        if(today != null){
            val date = today.dt * 1000
            tvTodayDate.text = DateUtils.getRelativeTimeSpanString(date, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL)
        }

        Glide.with(activity).load(ApiModule.BASE_ICON_URL + today?.weather?.get(0)?.icon + ".png").into(ivForecastIcon)
        tvDailyDescription.text = today?.weather?.get(0)?.description
//        tvTemperature.text = getString(R.string.forecast_temp, today?.temp?.min, today?.temp?.max )
        tvTemperature.text = getString(R.string.temperature, today?.temp?.day)
        weeklyAdapter.updateForecast(dailyForecast)

    }


    override fun showForecast(forecast: List<Forecast>?) {
//        i(TAG, "showDailyForecast: ${forecast.toString()}")
        dailyAdapter.updateForecast(forecast)
    }


    private fun forecastDirty(selectedLocation: String) : Boolean{
        val location = preferencesHelper.getLocation()
        val lastUpdated = preferencesHelper.getLastUpdated()
        val today = sdf.format(Date(System.currentTimeMillis()))
        preferencesHelper.setLastUpdated(today)
        preferencesHelper.saveLocation(selectedLocation)
        val dirty = lastUpdated == null || lastUpdated !=  today || location != selectedLocation
        i(TAG, "lastUpdated: $lastUpdated, today: $today, selectedLocation: $selectedLocation, savedLocation: $location is dirty: $dirty")
        return dirty
    }


    private fun getSelectionPosition(selection: String?) : Int{
        val locations: List<String> = activity?.resources?.getStringArray(R.array.locations)?.asList()
                ?: return 0

        return locations?.indexOf(selection)
    }
}

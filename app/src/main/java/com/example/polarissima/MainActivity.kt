package com.example.polarissima

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.polarissima.databinding.ActivityMainBinding
import com.example.polarissima.retrofit.WeatherAPI
import com.example.polarissima.retrofit.WeatherItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val API = ""

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var weatherAPI: WeatherAPI
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var cityTextView: TextView
    private lateinit var temperatureTextView: TextView
    private lateinit var weatherDescriptionTextView: TextView
    private lateinit var pressureTextView: TextView
    private lateinit var humidityTextView: TextView
    private lateinit var windSpeedTextView: TextView
    private lateinit var hourlyWeatherRecyclerView: RecyclerView
    private lateinit var hWeatherAdapter: HWeatherAdapter

    private val citySelectLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val latitude = result.data?.getDoubleExtra("latitude", 0.0)
                val longitude = result.data?.getDoubleExtra("longitude", 0.0)
                val cityName = result.data?.getStringExtra("cityName")

                if (latitude != null && longitude != null) {
                    sharedPreferences.edit()
                        .putString("latitude", latitude.toString())
                        .putString("longitude", longitude.toString())
                        .apply()

                    getWeatherData(latitude, longitude)
                }

                if (cityName != null) {
                    cityTextView.text = cityName.uppercase()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cityTextView = binding.textCity
        temperatureTextView = binding.textTemperature
        weatherDescriptionTextView = binding.textWeatherDesc
        pressureTextView = binding.textPressure
        humidityTextView = binding.textHumidity
        windSpeedTextView = binding.textWindSpeed
        hourlyWeatherRecyclerView = binding.recyclerHourlyWeather
        setupHourlyWeatherRV()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val hasSelectedCity = sharedPreferences.getBoolean("hasSelectedCity", false)

        if (!hasSelectedCity) {
            startCitySelectAct()
            return
        }

        val latitude = sharedPreferences.getString("latitude", null)?.toDouble()
        val longitude = sharedPreferences.getString("longitude", null)?.toDouble()

        if (latitude == null || longitude == null) {
            startCitySelectAct()
        } else {
            getWeatherData(latitude, longitude)
        }

        val refreshButton = findViewById<Button>(R.id.btn_refresh)
        refreshButton.setOnClickListener {
            updateWeatherData()
            Toast.makeText(this, "Data updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCitySelectAct() {
        val citySelectionIntent = Intent(this, CitySelectActivity::class.java)
        citySelectLauncher.launch(citySelectionIntent)
    }

    private fun setupHourlyWeatherRV() {
        hWeatherAdapter = HWeatherAdapter(emptyList())
        hourlyWeatherRecyclerView.adapter = hWeatherAdapter
        hourlyWeatherRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun getWeatherData(latitude: Double, longitude: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherAPI = retrofit.create(WeatherAPI::class.java)

        lifecycleScope.launch {
            try {
                val weatherData = weatherAPI.getWeatherData(latitude, longitude, API)
                withContext(Dispatchers.Main) {
                    val kelvinTemperature = weatherData.list[0].main.temp.toDouble()
                    val celsiusTemperature = kelvinTemperature - 273.15
                    val roundedCelsiusTemp = celsiusTemperature.toInt()

                    val kelvinFeelsLikeTemperature = weatherData.list[0].main.feels_like
                    val celsiusFLT = kelvinFeelsLikeTemperature - 273.15
                    val roundedCelsiusFLT = celsiusFLT.toInt()

                    val tempText = "$roundedCelsiusTemp°C - $roundedCelsiusFLT°C"
                    temperatureTextView.text = tempText

                    val weatherMain = weatherData.list[0].weather[0].main
                    weatherDescriptionTextView.text = weatherMain.uppercase()

                    val pressureHPa = weatherData.list[0].main.pressure
                    val pressureMmHg = (pressureHPa * 0.750062).toInt()
                    pressureTextView.text = "$pressureMmHg mmHg"

                    val humidity = weatherData.list[0].main.humidity
                    humidityTextView.text = "$humidity%"

                    val windSpeed = weatherData.list[0].wind.speed
                    windSpeedTextView.text = "$windSpeed m/s"

                    val hourlyWeatherList = weatherData.list.subList(1, minOf(weatherData.list.size, 10))
                    updateHourlyWeatherRV(hourlyWeatherList)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    temperatureTextView.text = "Error"
                    weatherDescriptionTextView.text = "Error"
                    pressureTextView.text = "Error"
                    humidityTextView.text = "Error"
                    windSpeedTextView.text = "Error"
                }
                e.printStackTrace()
            }
        }
    }

    private fun updateWeatherData() {
        val latitude = sharedPreferences.getString("latitude", null)?.toDouble()
        val longitude = sharedPreferences.getString("longitude", null)?.toDouble()

        if (latitude != null && longitude != null) {
            getWeatherData(latitude, longitude)
        }
    }

    private fun updateHourlyWeatherRV(hourlyWeatherList: List<WeatherItem>) {
        if (::hWeatherAdapter.isInitialized) {
            val formattedWeatherList = hourlyWeatherList.map { weatherItem ->
                val kelvinTemp = weatherItem.main.temp.toDouble()
                val celsiusTemp = kelvinTemp - 273.15
                val formatCelsiusTemp = "%.0f".format(celsiusTemp)
                weatherItem.copy(main = weatherItem.main.copy(temp = formatCelsiusTemp))
            }
            hWeatherAdapter.setData(formattedWeatherList)
        }
    }
}

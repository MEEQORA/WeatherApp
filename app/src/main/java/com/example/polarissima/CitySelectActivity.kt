package com.example.polarissima

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.polarissima.retrofit.GeocodingAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CitySelectActivity : AppCompatActivity() {
    private lateinit var cityEditText: EditText
    private lateinit var geocodingAPI: GeocodingAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_select)

        cityEditText = findViewById(R.id.edit_text_city)
        val saveButton: Button = findViewById(R.id.button_save)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/geo/1.0/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        geocodingAPI = retrofit.create(GeocodingAPI::class.java)

        saveButton.setOnClickListener {
            val selectedCity = cityEditText.text.toString().trim()
            if (selectedCity.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val response = geocodingAPI.getGeocodingData(selectedCity, 1, API)
                        if (response.isNotEmpty()) {
                            val latitude = response[0].lat
                            val longitude = response[0].lon

                            val resultIntent = Intent().apply {
                                putExtra("latitude", latitude)
                                putExtra("longitude", longitude)
                                putExtra("cityName", selectedCity.uppercase())
                            }
                            setResult(Activity.RESULT_OK, resultIntent)
                            finish()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}

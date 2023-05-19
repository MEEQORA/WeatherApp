package com.example.polarissima

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.polarissima.retrofit.WeatherItem
import java.text.SimpleDateFormat

class HWeatherAdapter(private var hourlyWeatherList: List<WeatherItem>) :
    RecyclerView.Adapter<HWeatherAdapter.HourlyWeatherViewHolder>() {

    fun setData(data: List<WeatherItem>) {
        hourlyWeatherList = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyWeatherViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.hourly_weather, parent, false)
        return HourlyWeatherViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HourlyWeatherViewHolder, position: Int) {
        if (hourlyWeatherList.isNotEmpty()) {
            val hourlyWeather = hourlyWeatherList[position]
            holder.bind(hourlyWeather)
        }
    }

    override fun getItemCount(): Int {
        return hourlyWeatherList.size
    }

    inner class HourlyWeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeTextView: TextView = itemView.findViewById(R.id.text_hourly_time)
        private val tempTextView: TextView = itemView.findViewById(R.id.text_hourly_temp)
        private val weatherIconImg: ImageView = itemView.findViewById(R.id.icon_hourly_weather)
        private val timeFormat = SimpleDateFormat("HH:mm")

        fun bind(hourlyWeather: WeatherItem) {
            val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(hourlyWeather.dt_txt)
            timeTextView.text = timeFormat.format(dateTime)
            tempTextView.text = "${hourlyWeather.main.temp}°C"

            val weatherIconResId = getWeatherIcon(hourlyWeather.weather[0].main)
            weatherIconImg.setImageResource(weatherIconResId)
        }

        private fun getWeatherIcon(weatherDescription: String): Int {
            return when (weatherDescription.lowercase()) {
                "rain" -> R.drawable.rain
                "clear" -> R.drawable.ic_clear_sky
                "clouds" -> R.drawable.cloud_image
                else -> R.drawable.sunny_icon
            }
        }
    }
}

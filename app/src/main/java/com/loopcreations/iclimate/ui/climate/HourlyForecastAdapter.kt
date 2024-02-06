package com.loopcreations.iclimate.ui.climate

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.loopcreations.iclimate.R
import com.loopcreations.iclimate.climateDataModel.HourlyForecastModel
import com.loopcreations.iclimate.databinding.HourlyForecastRowBinding

class HourlyForecastAdapter(private val context: Context, private val hourlyForecastList: ArrayList<HourlyForecastModel>): RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder>() {

    class ViewHolder(hourlyForecastView: View): RecyclerView.ViewHolder(hourlyForecastView){
        private val binding = HourlyForecastRowBinding.bind(hourlyForecastView)
        val hour = binding.timeTxt
        val conditionIV = binding.icon
        val temp = binding.tempTxt
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.hourly_forecast_row,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hourlyPosition = hourlyForecastList[position]
        getClimateCondition(holder.conditionIV, hourlyPosition.code.toInt(), hourlyPosition.isDay)
        holder.temp.text = "${hourlyPosition.temp} \u00B0"
        holder.hour.text = hourlyPosition.date
    }

    override fun getItemCount(): Int {
        return hourlyForecastList.size
    }

    private fun getClimateCondition(icon: ImageView, code: Int, isDay: Boolean) {
        when (code) {
            0 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, if(isDay) R.drawable.baseline_wb_sunny_24 else R.drawable.baseline_mode_night_24))
            }
            1 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, if(isDay) R.drawable.mainly_clear_day else R.drawable.mainly_clear_night))
            }
            2 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, if(isDay) R.drawable.partly_cloudy_day else R.drawable.partly_cloudy_night))
            }
            3 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.baseline_cloud_24))
            }
            45, 48 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.baseline_foggy_24))
            }
            51 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, if(isDay) R.drawable.drizzle_light_day else R.drawable.drizzle_light_night))
            }
            53,55 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.drizzle))
            }
            56 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, if (isDay) R.drawable.freezing_drizzle_day else R.drawable.freezing_drizzle_night))
            }
            57 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.freezing_drizzle_icon))
            }
            61 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.rain))
            }
            63 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.moderate_rain))
            }
            65 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.heavy_rain))
            }
            66,67 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.freezing_rain))
            }
            71 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.light_snow))
            }
            73 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.moderate_snow))
            }
            75 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.heavy_snow))
            }
            77 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.snow_grains))
            }
            80 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, if (isDay) R.drawable.raining_day else R.drawable.raining_night))
            }
            81 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, if (isDay) R.drawable.moderate_shower_day else R.drawable.moderate_shower_night))
            }
            82 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.heavy_shower))
            }
            85 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.light_snow_shower))
            }
            86 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.snow_shower))
            }
            95 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.thunderstorm))
            }
            96 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.thunderstorm_hail))
            }
            99 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.thunderstorm_heavy_hail))
            }
            else -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.baseline_wb_sunny_24))
            }
        }
    }
}
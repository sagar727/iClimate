package com.loopcreations.iclimate.ui.climate

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.loopcreations.iclimate.R
import com.loopcreations.iclimate.climateDataModel.ForecastModel
import com.loopcreations.iclimate.databinding.ForecastRowBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ForecastAdapter(
    private val context: Context,
    private val forecastList: ArrayList<ForecastModel>,
) : RecyclerView.Adapter<ForecastAdapter.ViewHolder>() {

    class ViewHolder(forecastView: View) : RecyclerView.ViewHolder(forecastView) {
        private val binding = ForecastRowBinding.bind(forecastView)
        val date = binding.dateTxt
        val icon = binding.forecastImage
        val minTemp = binding.minTemp
        val maxTemp = binding.maxTemp
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.forecast_row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return forecastList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val forecastPosition = forecastList[position]
        when (position) {
            0 -> holder.date.text = "TODAY"
            1 -> holder.date.text = "TOMORROW"
            else -> holder.date.text = getDayName(forecastPosition.date)
        }
        holder.minTemp.text = "Min ${forecastPosition.minTemp} \u00B0"
        holder.maxTemp.text = "Max ${forecastPosition.maxTemp} \u00B0"
        getClimateCondition(holder.icon, forecastPosition.code)
    }

    private fun getClimateCondition(icon: ImageView, code: Int) {
        when (code) {
            0 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.baseline_wb_sunny_24
                    )
                )
            }

            1 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.mainly_clear_day
                    )
                )
            }

            2 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.partly_cloudy_day
                    )
                )
            }

            3 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.baseline_cloud_24
                    )
                )
            }

            45, 48 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.baseline_foggy_24
                    )
                )
            }

            51 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.drizzle_light_day
                    )
                )
            }

            53, 55 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.drizzle))
            }

            56 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.freezing_drizzle_day
                    )
                )
            }

            57 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.freezing_drizzle_icon
                    )
                )
            }

            61 -> {
                icon.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.rain))
            }

            63 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.moderate_rain
                    )
                )
            }

            65 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.heavy_rain
                    )
                )
            }

            66, 67 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.freezing_rain
                    )
                )
            }

            71 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.light_snow
                    )
                )
            }

            73 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.moderate_snow
                    )
                )
            }

            75 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.heavy_snow
                    )
                )
            }

            77 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.snow_grains
                    )
                )
            }

            80 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.raining_day
                    )
                )
            }

            81 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.moderate_shower_day
                    )
                )
            }

            82 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.heavy_shower
                    )
                )
            }

            85 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.light_snow_shower
                    )
                )
            }

            86 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.snow_shower
                    )
                )
            }

            95 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.thunderstorm
                    )
                )
            }

            96 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.thunderstorm_hail
                    )
                )
            }

            99 -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.thunderstorm_heavy_hail
                    )
                )
            }

            else -> {
                icon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.baseline_wb_sunny_24
                    )
                )
            }
        }
    }


    private fun getDayName(date: String): String {
        val dt = LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
        return dt.dayOfWeek.name
    }
}
package com.loopcreations.iclimate.ui.climate

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.loopcreations.iclimate.BuildConfig
import com.loopcreations.iclimate.R
import com.loopcreations.iclimate.climateDataModel.ForecastModel
import com.loopcreations.iclimate.climateDataModel.HourlyForecastModel
import com.loopcreations.iclimate.databinding.FragmentClimateBinding
import com.loopcreations.iclimate.network.NetworkManager
import com.loopcreations.iclimate.network.RetrofitProvider
import com.loopcreations.iclimate.repository.ClimateRepository
import com.loopcreations.iclimate.room.CityEntity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.snackbar.BaseTransientBottomBar.BaseCallback.DismissEvent
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar

class ClimateFragment : Fragment() {

    private var _binding: FragmentClimateBinding? = null
    private lateinit var climateViewModel: ClimateViewModel
    private lateinit var repository: ClimateRepository
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var locationManager: LocationManager
    private lateinit var progressBar: ProgressBar
    private lateinit var networkManager: NetworkManager
    private lateinit var forecastAdapter: ForecastAdapter
    private lateinit var hourlyForecastAdapter: HourlyForecastAdapter
    private lateinit var placesClient: PlacesClient

    private val requestPermission = activityResultLauncher()
    private val requestLocationService = locationStatusLauncher()

    private val binding get() = _binding!!
    private var temp = "celsius"
    private var wind = "kmh"
    private var precipitation = "mm"
    private var isDefaultCityAvailable = false
    private var tempUnit = false
    private var windUnit = false
    private var precipitationUnit = false
    private var forecastList: ArrayList<ForecastModel> = ArrayList()
    private var hourlyForecastList: ArrayList<HourlyForecastModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        networkManager = NetworkManager(requireContext())
    }

    override fun onStart() {
        super.onStart()
        if(!networkManager.isCallbackRegistered){
            networkManager.registerNetworkCallback()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        climateViewModel = ViewModelProvider(this).get(ClimateViewModel::class.java)
        _binding = FragmentClimateBinding.inflate(inflater, container, false)
        val root: View = binding.root

        onOffline()

        val apiKey = BuildConfig.PLACES_API_KEY

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), apiKey)
        }
        placesClient = Places.createClient(requireContext())

        repository = context?.let { ClimateRepository(it.applicationContext) }!!
        progressBar = binding.loadingPBar

        val forecastListView = binding.forecastListView
        forecastListView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val hourlyForecastListView = binding.hourlyForecastListView
        hourlyForecastListView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        forecastAdapter = ForecastAdapter(requireContext(), forecastList)
        forecastListView.adapter = forecastAdapter
        hourlyForecastAdapter = HourlyForecastAdapter(requireContext(), hourlyForecastList)
        hourlyForecastListView.adapter = hourlyForecastAdapter

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        tempUnit = sharedPreferences.getBoolean("temperature_unit", false)
        temp = if (tempUnit) "fahrenheit" else "celsius"
        windUnit = sharedPreferences.getBoolean("wind_unit", false)
        wind = if (windUnit) "mph" else "kmh"
        precipitationUnit = sharedPreferences.getBoolean("precipitation_unit", false)
        precipitation = if (precipitationUnit) "inch" else "mm"
        val sharedPref = requireActivity().getSharedPreferences("LAT_LONG", Context.MODE_PRIVATE)
        val latitude = sharedPref.getString("latitude", null)?.toDouble()
        val longitude = sharedPref.getString("longitude", null)?.toDouble()
        val locName = sharedPref.getString("locName", null)

        networkManager.networkStatus.observe(viewLifecycleOwner) { networkStatus ->
            Log.i("network_status", networkStatus.toString())

            if (networkStatus != null) {
                if (!networkStatus) {
                    onOffline()
                } else {
                    onOnline()
                    fetchOnLoad(latitude, longitude, locName, sharedPref)
                }
            }
        }

        binding.locButton.setOnClickListener {
            getLocation()
        }

        binding.retryBtn.setOnClickListener {
            if (networkManager.checkNetwork()) {
                fetchOnLoad(latitude, longitude, locName, sharedPref)
            }
        }

        climateDataObserver()
        return root
    }

    private fun onOnline() {
        binding.tempContainer.visibility = View.VISIBLE
        binding.feelsTempTxt.visibility = View.VISIBLE
        binding.currentTempContainer.visibility = View.VISIBLE
        binding.minMaxContainer.visibility = View.VISIBLE
        binding.locButton.visibility = View.VISIBLE
        binding.forecastListView.visibility = View.VISIBLE
        binding.hourlyForecastContainer.visibility = View.VISIBLE
        binding.forecastContainer.visibility = View.VISIBLE
        binding.precepCard.visibility = View.VISIBLE
        binding.humidityCard.visibility = View.VISIBLE
        binding.noNetworkIV.visibility = View.GONE
        binding.retryBtn.visibility = View.GONE
    }

    private fun onOffline() {
        binding.tempContainer.visibility = View.GONE
        binding.feelsTempTxt.visibility = View.GONE
        binding.currentTempContainer.visibility = View.GONE
        binding.minMaxContainer.visibility = View.GONE
        binding.loadingPBar.visibility = View.GONE
        binding.locButton.visibility = View.GONE
        binding.forecastListView.visibility = View.GONE
        binding.hourlyForecastContainer.visibility = View.GONE
        binding.forecastContainer.visibility = View.GONE
        binding.precepCard.visibility = View.GONE
        binding.humidityCard.visibility = View.GONE
        binding.noNetworkIV.visibility = View.VISIBLE
        binding.retryBtn.visibility = View.VISIBLE
    }

    private fun fetchOnLoad(
        latitude: Double?,
        longitude: Double?,
        locName: String?,
        sharedPref: SharedPreferences,
    ) {
        if (latitude != null && longitude != null) {
            climateApiCall(latitude, longitude)
            binding.locTxt.text = locName
            val editor = sharedPref.edit()
            editor.clear()
            editor.apply()
        } else {
            lifecycleScope.launch {
                val defaultCityData = climateViewModel.getDefaultCity(repository)
                if (defaultCityData != null) {
                    climateApiCall(defaultCityData.lat, defaultCityData.lng)
                    binding.locTxt.text = defaultCityData.locationName
                } else {
                    isDefaultCityAvailable = false
                    getLocation()
                }
            }
        }
    }


    override fun onStop() {
        super.onStop()
        if(networkManager.isCallbackRegistered){
            networkManager.unregisterNetworkCallback()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getLocation() {
        locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val hasGPS =
            requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
        if (hasGPS) {
            if (checkPermission()) {
                val locationRequest = com.google.android.gms.location.LocationRequest.create()
                locationRequest.interval = 3000
                locationRequest.priority = Priority.PRIORITY_HIGH_ACCURACY

                val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                val client = LocationServices.getSettingsClient(requireContext())
                val task: Task<LocationSettingsResponse> =
                    client.checkLocationSettings(builder.build())

                task.addOnSuccessListener { response ->
                    val states = response.locationSettingsStates
                    if (states!!.isLocationPresent) {
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            5000,
                            5f
                        ) { location ->
                            getLocationName(location.latitude, location.longitude)
                            climateApiCall(location.latitude, location.longitude)
                        }
                    }
                }

                task.addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Please enable location.", Toast.LENGTH_LONG)
                        .show()
                    val status = (e as ResolvableApiException).statusCode
                    if (status == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        try {
                            requestLocationService.launch(
                                IntentSenderRequest.Builder(e.resolution).build()
                            )
                        } catch (error: SendIntentException) {
                            Log.e("error", error.toString())
                        }
                    }
                }
            } else {
                requestPermission.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun checkPermission(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                )
    }

    private fun activityResultLauncher() =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var num = 0
            permissions.forEach { actionMap ->
                when (actionMap.key) {
                    Manifest.permission.ACCESS_COARSE_LOCATION -> {
                        if (actionMap.value) {
                            num += 1
                        }
                    }

                    Manifest.permission.ACCESS_FINE_LOCATION -> {
                        if (actionMap.value) {
                            num += 1
                        }
                    }
                }
            }
            if (num == 2) {
                getLocation()
            }
        }

    private fun locationStatusLauncher() =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                if (checkPermission()) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        5000,
                        5f
                    ) { location ->
                        getLocationName(location.latitude, location.longitude)
                        climateApiCall(location.latitude, location.longitude)
                    }
                }

            } else if (result.resultCode == RESULT_CANCELED) {
                showSnackBar("Please enable gps to get climate details for your current location")
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5000,
                    5f
                ) { location ->
                    getLocationName(location.latitude, location.longitude)
                    climateApiCall(location.latitude, location.longitude)
                }
            }
        }

    private fun climateApiCall(latitude: Double, longitude: Double) {
        progressBar.visibility = View.VISIBLE
        climateViewModel.getWeather(
            repository,
            latitude,
            longitude,
            temp,
            wind,
            precipitation
        )
    }

    private fun climateDataObserver() {
        climateViewModel.climateLiveData.observe(viewLifecycleOwner) { climate ->
            if (climate != null) {
                progressBar.visibility = View.GONE
                val isDay = climate.current.isDay == 1
                val minTemp =
                    climate.daily.dailyTempMin[0].toString() + if (tempUnit) " \u2109" else " \u2103"
                val maxTemp =
                    climate.daily.dailyTempMax[0].toString() + if (tempUnit) " \u2109" else " \u2103"
                val currTemp =
                    climate.current.currentTemp.toString() + if (tempUnit) " \u2109" else " \u2103"
                val feelsTemp =
                    "Feels Like " + climate.current.feelsTemp.toString() + if (tempUnit) " \u2109" else " \u2103"

                val date = climate.daily.time
                val code = climate.daily.dailyWCode
                val minT = climate.daily.dailyTempMin
                val maxT = climate.daily.dailyTempMax
                val precipitation = climate.current.precipitation.toString() + " " + precipitation
                val pressure = climate.current.pressure.toString() + " hPa"
                val humidity = climate.current.humidity.toString() + " %"
                val snow =
                    climate.current.snowfall.toString() + if (precipitationUnit) " cm" else " inch"
                val gust = climate.current.gusts.toString() + " " + wind
                val speed = climate.current.wind.toString() + " " + wind

                var i = 0
                forecastList.clear()
                while (i < date.size) {
                    forecastList.add(ForecastModel(date[i], code[i], minT[i], maxT[i]))
                    forecastAdapter.notifyDataSetChanged()
                    i++
                }

                var j = 0
                hourlyForecastList.clear()
                val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                while (j < 24) {
                    val hourlyCode = climate.hourly.hourlyWCode[j + currentHour]
                    val hourlyTemp = climate.hourly.hourlyTemp[j + currentHour]
                    val time = climate.hourly.time[j + currentHour].split("T")
                    val timeInNum = time[1].split(":")[0].toInt()
                    if (timeInNum == 0) {
                        val t = "12 AM"
                        if (currentHour == timeInNum) {
                            hourlyForecastList.add(
                                HourlyForecastModel(
                                    "Now",
                                    hourlyCode,
                                    hourlyTemp
                                )
                            )
                        } else {
                            hourlyForecastList.add(HourlyForecastModel(t, hourlyCode, hourlyTemp))
                        }
                    } else if (timeInNum == 12) {
                        val t = "12 PM"
                        if (currentHour == timeInNum) {
                            hourlyForecastList.add(
                                HourlyForecastModel(
                                    "Now",
                                    hourlyCode,
                                    hourlyTemp
                                )
                            )
                        } else {
                            hourlyForecastList.add(HourlyForecastModel(t, hourlyCode, hourlyTemp))
                        }
                    } else if (timeInNum > 12) {
                        val t = "${timeInNum - 12} PM"
                        if (currentHour == timeInNum) {
                            hourlyForecastList.add(
                                HourlyForecastModel(
                                    "Now",
                                    hourlyCode,
                                    hourlyTemp
                                )
                            )
                        } else {
                            hourlyForecastList.add(HourlyForecastModel(t, hourlyCode, hourlyTemp))
                        }
                    } else {
                        val t = "$timeInNum AM"
                        if (currentHour == timeInNum) {
                            hourlyForecastList.add(
                                HourlyForecastModel(
                                    "Now",
                                    hourlyCode,
                                    hourlyTemp
                                )
                            )
                        } else {
                            hourlyForecastList.add(HourlyForecastModel(t, hourlyCode, hourlyTemp))
                        }
                    }
                    hourlyForecastAdapter.notifyDataSetChanged()
                    j++
                }

                binding.tempContainer.setPercentWithAnimation(
                    getProgressPercentage(
                        climate.daily.dailyTempMin[0],
                        climate.daily.dailyTempMax[0],
                        climate.current.currentTemp
                    )
                )
                binding.tempContainer.setProgressBarColor(Color.rgb(33, 150, 243))
                binding.tempContainer.setProgressBarWidth(20)
                binding.tempContainer.setProgressPlaceHolderWidth(30)
                binding.tempContainer.setProgressPlaceHolderWidth(20)

                binding.minTempTxt.text = minTemp
                binding.maxTempTxt.text = maxTemp
                binding.currentTempTxt.text = currTemp
                binding.feelsTempTxt.text = feelsTemp
                binding.currentConditionTxt.text =
                    getClimateCondition(climate.current.currentWCode, isDay)
                binding.precepTxt.text = precipitation
                binding.pressureTxt.text = pressure
                binding.humidityTxt.text = humidity
                binding.snowTxt.text = snow
                binding.gustTxt.text = gust
                binding.speedTxt.text = speed
            } else {
                Toast.makeText(requireContext(), "No data", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getProgressPercentage(min: Double, max: Double, current: Double): Int {
        val difference = max - min
        val currentDifference = current - min
        return (100 * currentDifference / difference).toInt()
    }

    private fun getClimateCondition(code: Int, isDay: Boolean): String {
        var climateCondition = ""

        when (code) {
            0 -> {
                climateCondition = "Clear Sky"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        if (isDay) R.drawable.baseline_wb_sunny_24 else R.drawable.baseline_mode_night_24
                    )
                )
            }

            1 -> {
                climateCondition = "Mainly clear"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        if (isDay) R.drawable.mainly_clear_day else R.drawable.mainly_clear_night
                    )
                )
            }

            2 -> {
                climateCondition = "Partly cloudy"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        if (isDay) R.drawable.partly_cloudy_day else R.drawable.partly_cloudy_night
                    )
                )
            }

            3 -> {
                climateCondition = "Overcast"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.baseline_cloud_24
                    )
                )
            }

            45 -> {
                climateCondition = "Fog"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.baseline_foggy_24
                    )
                )
            }

            48 -> {
                climateCondition = "Depositing rime fog"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.baseline_foggy_24
                    )
                )
            }

            51 -> {
                climateCondition = "Drizzle: Light intensity"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        if (isDay) R.drawable.drizzle_light_day else R.drawable.drizzle_light_night
                    )
                )
            }

            53 -> {
                climateCondition = "Drizzle: Moderate intensity"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.drizzle
                    )
                )
            }

            55 -> {
                climateCondition = "Drizzle: Dense intensity"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.drizzle
                    )
                )
            }

            56 -> {
                climateCondition = "Freezing Drizzle: Light intensity"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        if (isDay) R.drawable.freezing_drizzle_day else R.drawable.freezing_drizzle_night
                    )
                )
            }

            57 -> {
                climateCondition = "Freezing Drizzle: Dense intensity"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.freezing_drizzle_icon
                    )
                )
            }

            61 -> {
                climateCondition = "Rain: Slight intensity"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.rain
                    )
                )
            }

            63 -> {
                climateCondition = "Rain: Moderate intensity"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.moderate_rain
                    )
                )
            }

            65 -> {
                climateCondition = "Rain: Heavy intensity"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.heavy_rain
                    )
                )
            }

            66 -> {
                climateCondition = "Freezing Rain: Light intensity"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.freezing_rain
                    )
                )
            }

            67 -> {
                climateCondition = "Freezing Rain: Heavy intensity"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.freezing_rain
                    )
                )
            }

            71 -> {
                climateCondition = "Snow fall: Slight intensity"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.light_snow
                    )
                )
            }

            73 -> {
                climateCondition = "Snow fall: Moderate intensity"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.moderate_snow
                    )
                )
            }

            75 -> {
                climateCondition = "Snow fall: Heavy intensity"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.heavy_snow
                    )
                )
            }

            77 -> {
                climateCondition = "Snow grains"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.snow_grains
                    )
                )
            }

            80 -> {
                climateCondition = "Rain showers: Slight intensity"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        if (isDay) R.drawable.raining_day else R.drawable.raining_night
                    )
                )
            }

            81 -> {
                climateCondition = "Rain showers: Moderate intensity"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        if (isDay) R.drawable.moderate_shower_day else R.drawable.moderate_shower_night
                    )
                )
            }

            82 -> {
                climateCondition = "Rain showers: Violent intensity"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.heavy_shower
                    )
                )
            }

            85 -> {
                climateCondition = "Snow showers: Slight intensity"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.light_snow_shower
                    )
                )
            }

            86 -> {
                climateCondition = "Snow showers: Heavy intensity"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.snow_shower
                    )
                )
            }

            95 -> {
                climateCondition = "Thunderstorm: Slight or moderate"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.thunderstorm
                    )
                )
            }

            96 -> {
                climateCondition = "Thunderstorm with slight hail"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.thunderstorm_hail
                    )
                )
            }

            99 -> {
                climateCondition = "Thunderstorm with heavy hail"
                binding.conditionImage.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.thunderstorm_heavy_hail
                    )
                )
            }

            else -> {
                climateCondition = ""
            }
        }
        return climateCondition
    }

    @SuppressLint("MissingPermission")
    private fun getLocationName(latitude: Double, longitude: Double) {
        val placeFields: List<Place.Field> = listOf(Place.Field.ID)
        val findRequest: FindCurrentPlaceRequest =
            FindCurrentPlaceRequest.builder(placeFields).build()
        val findReqResponse = placesClient.findCurrentPlace(findRequest)
        findReqResponse.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val responseData = task.result
                if (responseData.placeLikelihoods.size > 0) {
                    val currentPlaceId = responseData.placeLikelihoods[0].place.id
                    if (currentPlaceId != null) {
                        val placeFields1: List<Place.Field> = listOf(Place.Field.ADDRESS_COMPONENTS)
                        val fetchRequest =
                            FetchPlaceRequest.newInstance(currentPlaceId, placeFields1)
                        placesClient.fetchPlace(fetchRequest).addOnSuccessListener { result ->
                            val addressComponent = result.place.addressComponents?.asList()
                            var location = ""
                            if (addressComponent != null) {
                                for (component in addressComponent) {
                                    val types = component.types
                                    if (types.size > 0) {
                                        for (type in types) {
                                            when (type) {
                                                "administrative_area_level_3" -> {
                                                    location = component.name
                                                }

                                                "locality" -> {
                                                    location = component.name
                                                }
                                            }

                                            if (type == "administrative_area_level_1") {
                                                location = location + ", " + component.name
                                            }
                                            if (type == "country") {
                                                location = location + ", " + component.name
                                            }
                                        }
                                    }
                                }
                                binding.locTxt.text = location
                                if (!isDefaultCityAvailable) {
                                    if (location != "") {
                                        val cityEntity =
                                            CityEntity(0, location, latitude, longitude, true)
                                        climateViewModel.addCity(repository, cityEntity)
                                        isDefaultCityAvailable = true
                                    }
                                }
                            }
                        }
                    }
                } else {
                    showSnackBar("There was error while getting current location name.")
                }
            } else {
                showSnackBar("There was error while getting current location name.")
            }
        }
    }

    private fun showSnackBar(msg: String) {
        view?.let {
            Snackbar.make(
                requireContext(),
                it,
                msg,
                Snackbar.LENGTH_LONG
            )
                .setBackgroundTint(Color.BLACK)
                .setTextColor(Color.WHITE)
                .setAction("OK") {
                    DismissEvent()
                }
                .show()
        }
    }
}
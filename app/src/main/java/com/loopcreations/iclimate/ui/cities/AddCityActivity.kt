package com.loopcreations.iclimate.ui.cities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.loopcreations.iclimate.BuildConfig
import com.loopcreations.iclimate.MainActivity
import com.loopcreations.iclimate.R
import com.loopcreations.iclimate.databinding.ActivityAddCityBinding
import com.loopcreations.iclimate.repository.ClimateRepository
import com.loopcreations.iclimate.room.CityEntity
import com.loopcreations.iclimate.ui.climate.ClimateViewModel
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.coroutines.launch

class AddCityActivity : AppCompatActivity(), CityAdapter.OnItemClickListener {

    private lateinit var binding: ActivityAddCityBinding
    private lateinit var climateViewModel: ClimateViewModel
    private lateinit var repository: ClimateRepository
    private var cityList: ArrayList<CityEntity> = ArrayList()
    private lateinit var adapter: CityAdapter
    private lateinit var recyclerView: RecyclerView
    private var lat: Double = 0.0
    private var lng: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        climateViewModel = ViewModelProvider(this).get(ClimateViewModel::class.java)
        repository = ClimateRepository(this)
        binding.addCityBtn.visibility = View.GONE
        binding.addCityBtn.isEnabled = false
        binding.locTv.visibility = View.GONE

        recyclerView = binding.cityListRV
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = CityAdapter(cityList, this)
        recyclerView.adapter = adapter

        climateViewModel.getCities(repository)

        cityListObserver()

        val swipeGesture = object : SwipeGesture(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        val cityData = cityList[viewHolder.bindingAdapterPosition]
                        if (cityList.size > 1) {
                            if (cityData.isDefault) {
                                lifecycleScope.launch {
                                    climateViewModel.deleteCity(repository, cityData.locationName)
                                    val firstCity = climateViewModel.getFirstCity(repository)
                                    climateViewModel.updateCity(
                                        repository,
                                        firstCity.locationName,
                                        true
                                    )
                                    adapter.deleteItem(viewHolder.bindingAdapterPosition)
                                    cityList[0].isDefault = true
                                    adapter.notifyDataSetChanged()
                                }
                            } else {
                                lifecycleScope.launch {
                                    climateViewModel.deleteCity(repository, cityData.locationName)
                                    adapter.deleteItem(viewHolder.bindingAdapterPosition)
                                    adapter.notifyDataSetChanged()
                                }
                            }
                        } else {
                            lifecycleScope.launch {
                                climateViewModel.deleteCity(repository, cityData.locationName)
                                adapter.deleteItem(viewHolder.bindingAdapterPosition)
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }

                    ItemTouchHelper.RIGHT -> {
                        lifecycleScope.launch {
                            for (city in cityList) {
                                if (city.isDefault) {
                                    city.isDefault = false
                                    climateViewModel.updateCity(
                                        repository,
                                        city.locationName,
                                        false
                                    )
                                }
                            }
                            val cityData = cityList[viewHolder.bindingAdapterPosition]
                            climateViewModel.updateCity(repository, cityData.locationName, true)
                            cityData.isDefault = true
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        }

        val touchHelper = ItemTouchHelper(swipeGesture)
        touchHelper.attachToRecyclerView(recyclerView)

        val apiKey = BuildConfig.PLACES_API_KEY

        if (!Places.isInitialized()) {
            Places.initialize(this, apiKey)
        }

        val autoCompleteFragment =
            supportFragmentManager.findFragmentById(R.id.addCityFragment) as AutocompleteSupportFragment
        autoCompleteFragment.view?.setBackgroundColor(Color.WHITE)
        autoCompleteFragment.setPlaceFields(
            listOf(Place.Field.LAT_LNG, Place.Field.ADDRESS_COMPONENTS)
        )

        autoCompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(p0: Status) {

            }

            override fun onPlaceSelected(loc: Place) {
                lat = loc.latLng?.latitude ?: 0.0
                lng = loc.latLng?.longitude ?: 0.0
                val addressComponent = loc.addressComponents?.asList()
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
                    binding.locTv.text = location
                    lifecycleScope.launch {
                        val count = climateViewModel.findCityCountByName(repository, location)
                        binding.locTv.visibility = View.VISIBLE
                        if (count == 0) {
                            binding.addCityBtn.visibility = View.VISIBLE
                            binding.addCityBtn.isEnabled = true
                        } else {
                            binding.addCityBtn.visibility = View.VISIBLE
                            binding.addCityBtn.isEnabled = false
                        }
                    }
                }
            }
        })

        binding.addCityBtn.setOnClickListener {
            if (binding.locTv.text.trim().toString() != "") {
                lifecycleScope.launch {
                    val count = climateViewModel.getCityCount(repository)
                    val location = binding.locTv.text.toString()
                    val isDefault: Boolean = count == 0
                    val cityEntity = CityEntity(0, location, lat, lng, isDefault)
                    climateViewModel.addCity(repository, cityEntity)
                    cityList.add(cityEntity)
                    adapter.notifyDataSetChanged()
                    binding.locTv.text = ""
                    binding.addCityBtn.visibility = View.GONE
                    binding.locTv.visibility = View.GONE
                }
            }
        }
    }

    override fun onItemClick(data: CityEntity) {
        val lat = data.lat.toString()
        val lng = data.lng.toString()
        val loc = data.locationName
        val sharedPref = getSharedPreferences("LAT_LONG", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("latitude", lat)
        editor.putString("longitude", lng)
        editor.putString("locName",loc)
        editor.apply()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun cityListObserver() {
        cityList.clear()
        climateViewModel.cities.observe(this) { cities ->
            for (city in cities) {
                cityList.add(city)
                adapter.notifyDataSetChanged()
            }
        }
    }
}
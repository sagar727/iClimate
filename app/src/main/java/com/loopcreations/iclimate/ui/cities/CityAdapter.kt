package com.loopcreations.iclimate.ui.cities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.loopcreations.iclimate.R
import com.loopcreations.iclimate.databinding.CityListRowBinding
import com.loopcreations.iclimate.room.CityEntity

class CityAdapter(private val cityList: ArrayList<CityEntity>, private val listener: OnItemClickListener): RecyclerView.Adapter<CityAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(data: CityEntity)
    }

    inner class ViewHolder(cityView: View): RecyclerView.ViewHolder(cityView), View.OnClickListener{
        private val binding = CityListRowBinding.bind(cityView)
        val icon = binding.tickImage
        val city = binding.cityText
        val default = binding.defaultText

        init {
            cityView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            if(bindingAdapterPosition != RecyclerView.NO_POSITION){
                val city = cityList[bindingAdapterPosition]
                listener.onItemClick(city)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.city_list_row,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cityPosition = cityList[position]
        holder.city.text = cityPosition.locationName
        if(cityPosition.isDefault){
            holder.default.visibility = View.VISIBLE
            holder.icon.visibility = View.VISIBLE
        }else{
            holder.default.visibility = View.INVISIBLE
            holder.icon.visibility = View.INVISIBLE
        }
        holder.city
    }

    override fun getItemCount(): Int {
        return cityList.size
    }

    fun deleteItem(i:Int){
        cityList.removeAt(i)
        notifyItemRemoved(i)
    }
}
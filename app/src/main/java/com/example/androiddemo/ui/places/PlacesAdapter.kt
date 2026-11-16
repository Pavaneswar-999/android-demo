package com.example.androiddemo.ui.places

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androiddemo.data.local.Place
import com.example.androiddemo.databinding.ItemPlaceBinding
import java.util.Locale

class PlacesAdapter(
    private val onSelectClick: (Place) -> Unit,
    private val onDeleteClick: (Place) -> Unit
) : ListAdapter<Place, PlacesAdapter.PlaceViewHolder>(DiffCallback) {

    class PlaceViewHolder(
        private val binding: ItemPlaceBinding,
        private val onSelectClick: (Place) -> Unit,
        private val onDeleteClick: (Place) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Place) {
            val displayName = if (item.isCurrentLocation) "${item.name} (Current Location)" else item.name
            binding.tvPlaceName.text = displayName
            binding.tvPlaceDetails.text = String.format(
                Locale.US,
                "Lat: %.4f, Lon: %.4f",
                item.latitude,
                item.longitude
            )

            binding.btnSelect.setOnClickListener { onSelectClick(item) }
            binding.btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val binding = ItemPlaceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaceViewHolder(binding, onSelectClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Place>() {
            override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean {
                return oldItem == newItem
            }
        }
    }
}

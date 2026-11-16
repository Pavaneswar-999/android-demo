package com.example.androiddemo.ui.forecast

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androiddemo.R
import com.example.androiddemo.data.model.DailyRiskItem
import com.example.androiddemo.data.model.RiskLevel
import com.example.androiddemo.databinding.ItemForecastDayBinding

class ForecastAdapter : ListAdapter<DailyRiskItem, ForecastAdapter.ForecastViewHolder>(DiffCallback) {

    class ForecastViewHolder(private val binding: ItemForecastDayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DailyRiskItem) {
            binding.tvDate.text = item.date
            binding.tvDischarge.text = String.format(
                java.util.Locale.US,
                "Discharge: %.1f m³/s (Mean: %.1f m³/s)",
                item.discharge,
                item.meanDischarge
            )

            val context = binding.root.context
            val (bgColor, textColor, riskLabel, contentDesc) = when (item.riskLevel) {
                RiskLevel.NORMAL -> Tuple4(
                    R.color.risk_normal_bg,
                    R.color.risk_normal_color,
                    "NORMAL",
                    "Normal flood risk"
                )
                RiskLevel.WATCH -> Tuple4(
                    R.color.risk_watch_bg,
                    R.color.risk_watch_color,
                    "WATCH",
                    "Watch flood risk"
                )
                RiskLevel.ALERT -> Tuple4(
                    R.color.risk_alert_bg,
                    R.color.risk_alert_color,
                    "ALERT",
                    "Alert flood risk: High discharge level"
                )
            }

            binding.tvRiskBadge.text = riskLabel
            binding.tvRiskBadge.setBackgroundColor(ContextCompat.getColor(context, bgColor))
            binding.tvRiskBadge.setTextColor(ContextCompat.getColor(context, textColor))
            binding.tvRiskBadge.contentDescription = contentDesc
        }
    }

    private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val binding = ItemForecastDayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<DailyRiskItem>() {
            override fun areItemsTheSame(oldItem: DailyRiskItem, newItem: DailyRiskItem): Boolean {
                return oldItem.date == newItem.date
            }

            override fun areContentsTheSame(oldItem: DailyRiskItem, newItem: DailyRiskItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}

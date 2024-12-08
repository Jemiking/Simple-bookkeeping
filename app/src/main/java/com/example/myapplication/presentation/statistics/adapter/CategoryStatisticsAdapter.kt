package com.example.myapplication.presentation.statistics.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemCategoryStatisticsBinding
import com.example.myapplication.domain.model.Category
import com.example.myapplication.presentation.util.formatCurrency

class CategoryStatisticsAdapter : ListAdapter<CategoryStatistics, CategoryStatisticsAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryStatisticsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemCategoryStatisticsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CategoryStatistics) {
            binding.apply {
                // 设置分类图标
                imageCategory.setImageResource(item.category.iconResId)

                // 设置分类名称
                textCategory.text = item.category.name

                // 设置金额
                textAmount.text = item.amount.formatCurrency()

                // 设置百分比
                textPercentage.text = "%.1f%%".format(item.percentage)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<CategoryStatistics>() {
        override fun areItemsTheSame(oldItem: CategoryStatistics, newItem: CategoryStatistics): Boolean {
            return oldItem.category.id == newItem.category.id
        }

        override fun areContentsTheSame(oldItem: CategoryStatistics, newItem: CategoryStatistics): Boolean {
            return oldItem == newItem
        }
    }
}

data class CategoryStatistics(
    val category: Category,
    val amount: Double,
    val percentage: Double
) 
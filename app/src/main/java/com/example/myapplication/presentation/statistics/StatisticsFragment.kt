package com.example.myapplication.presentation.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentStatisticsBinding
import com.example.myapplication.domain.model.TransactionType
import com.example.myapplication.presentation.statistics.adapter.CategoryStatisticsAdapter
import com.example.myapplication.presentation.statistics.dialog.FilterDialog
import com.example.myapplication.presentation.statistics.dialog.FilterOptions
import com.example.myapplication.presentation.util.ShareUtils
import com.example.myapplication.presentation.util.formatCurrency
import com.example.myapplication.presentation.view.PieChartData
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class StatisticsFragment : Fragment(), FilterDialog.FilterDialogListener {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatisticsViewModel by viewModels()
    private val categoryAdapter = CategoryStatisticsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeState()
    }

    private fun setupUI() {
        setupMonthChips()
        setupChartTypeToggle()
        setupToolbar()
        setupRecyclerView()
        setupErrorView()
    }

    private fun setupMonthChips() {
        // 生成最近6个月的月份选项
        val currentMonth = YearMonth.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy年MM月")

        for (i in 0..5) {
            val month = currentMonth.minusMonths(i.toLong())
            val chip = Chip(requireContext()).apply {
                text = month.format(formatter)
                isCheckable = true
                isChecked = i == 0
                setOnClickListener {
                    viewModel.onEvent(StatisticsEvent.SelectMonth(month))
                }
            }
            binding.monthChips.addView(chip, 0)
        }
    }

    private fun setupChartTypeToggle() {
        binding.chartTypeToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.button_pie_chart -> {
                        binding.pieChart.isVisible = true
                        binding.lineChart.isVisible = false
                        viewModel.onEvent(StatisticsEvent.ToggleChartType)
                    }
                    R.id.button_line_chart -> {
                        binding.pieChart.isVisible = false
                        binding.lineChart.isVisible = true
                        viewModel.onEvent(StatisticsEvent.ToggleChartType)
                    }
                }
            }
        }

        // 默认选中饼图
        binding.buttonPieChart.isChecked = true
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_filter -> {
                    showFilterDialog()
                    true
                }
                R.id.action_share -> {
                    shareStatistics()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerCategories.adapter = categoryAdapter
    }

    private fun setupErrorView() {
        binding.includeError.buttonRetry.setOnClickListener {
            viewModel.onEvent(StatisticsEvent.Refresh)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    updateUI(state)
                }
            }
        }
    }

    private fun updateUI(state: StatisticsState) {
        // 更新加载状态
        binding.includeLoading.root.isVisible = state.isLoading
        binding.includeError.root.isVisible = state.error != null
        binding.contentLayout.isVisible = !state.isLoading && state.error == null

        // 更新错误信息
        state.error?.let { error ->
            binding.includeError.textError.text = error
            showErrorSnackbar(error)
        }

        // 更新收支金额
        binding.textIncome.text = state.totalIncome.formatCurrency()
        binding.textExpense.text = state.totalExpense.formatCurrency()

        // 更新图表数据
        if (binding.lineChart.isVisible) {
            binding.lineChart.setData(state.chartData)
        } else {
            binding.pieChart.setData(state.categoryStatistics.map { (category, amount) ->
                PieChartData(category.name, amount.toFloat())
            })
        }

        // 更新分类统计列表
        categoryAdapter.submitList(state.categoryStatistics.map { (category, amount) ->
            CategoryStatistics(
                category = category,
                amount = amount,
                percentage = amount / (if (state.selectedType == TransactionType.INCOME) state.totalIncome else state.totalExpense) * 100
            )
        })
    }

    private fun showFilterDialog() {
        FilterDialog().apply {
            setListener(this@StatisticsFragment)
        }.show(childFragmentManager, "filter_dialog")
    }

    private fun shareStatistics() {
        val title = getString(R.string.share_statistics_title)
        ShareUtils.shareStatistics(requireContext(), binding.contentLayout, title)
    }

    private fun showErrorSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction(R.string.retry) {
                viewModel.onEvent(StatisticsEvent.Refresh)
            }
            .show()
    }

    override fun onFilterApplied(filter: FilterOptions) {
        viewModel.onEvent(StatisticsEvent.ApplyFilter(filter))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
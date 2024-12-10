package com.example.myapplication.presentation.statistics.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import com.example.myapplication.R
import com.example.myapplication.databinding.DialogFilterBinding
import com.example.myapplication.domain.model.TransactionType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import java.time.LocalDate

class FilterDialog : BottomSheetDialogFragment() {

    private var _binding: DialogFilterBinding? = null
    private val binding get() = _binding!!

    private var listener: FilterDialogListener? = null
    private var currentFilter = FilterOptions()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        setupTypeChips()
        setupAmountInputs()
        setupButtons()
    }

    private fun setupTypeChips() {
        binding.chipGroupType.apply {
            TransactionType.values().forEach { type ->
                addView(createTypeChip(type))
            }
            check(when (currentFilter.type) {
                TransactionType.INCOME -> R.id.chip_income
                TransactionType.EXPENSE -> R.id.chip_expense
                else -> View.NO_ID
            })
            setOnCheckedStateChangeListener { group, checkedIds ->
                currentFilter = currentFilter.copy(
                    type = when (checkedIds.firstOrNull()) {
                        R.id.chip_income -> TransactionType.INCOME
                        R.id.chip_expense -> TransactionType.EXPENSE
                        else -> null
                    }
                )
            }
        }
    }

    private fun createTypeChip(type: TransactionType): Chip {
        return Chip(requireContext()).apply {
            id = when (type) {
                TransactionType.INCOME -> R.id.chip_income
                TransactionType.EXPENSE -> R.id.chip_expense
                else -> View.generateViewId()
            }
            text = when (type) {
                TransactionType.INCOME -> getString(R.string.income)
                TransactionType.EXPENSE -> getString(R.string.expense)
                else -> type.name
            }
            isCheckable = true
        }
    }

    private fun setupAmountInputs() {
        binding.editMinAmount.apply {
            setText(currentFilter.minAmount?.toString() ?: "")
            doAfterTextChanged { text ->
                currentFilter = currentFilter.copy(
                    minAmount = text?.toString()?.toDoubleOrNull()
                )
            }
        }

        binding.editMaxAmount.apply {
            setText(currentFilter.maxAmount?.toString() ?: "")
            doAfterTextChanged { text ->
                currentFilter = currentFilter.copy(
                    maxAmount = text?.toString()?.toDoubleOrNull()
                )
            }
        }
    }

    private fun setupButtons() {
        binding.buttonReset.setOnClickListener {
            currentFilter = FilterOptions()
            updateUI()
        }

        binding.buttonApply.setOnClickListener {
            listener?.onFilterApplied(currentFilter)
            dismiss()
        }
    }

    private fun updateUI() {
        binding.apply {
            // 更新类型选择
            chipGroupType.check(when (currentFilter.type) {
                TransactionType.INCOME -> R.id.chip_income
                TransactionType.EXPENSE -> R.id.chip_expense
                else -> View.NO_ID
            })

            // 更新金额输入
            editMinAmount.setText(currentFilter.minAmount?.toString() ?: "")
            editMaxAmount.setText(currentFilter.maxAmount?.toString() ?: "")
        }
    }

    fun setListener(listener: FilterDialogListener) {
        this.listener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface FilterDialogListener {
        fun onFilterApplied(filter: FilterOptions)
    }
}

data class FilterOptions(
    val type: TransactionType? = null,
    val categoryIds: List<String> = emptyList(),
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null
) 
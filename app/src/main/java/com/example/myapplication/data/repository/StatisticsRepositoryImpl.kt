package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.TransactionDao
import com.example.myapplication.domain.model.*
import com.example.myapplication.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.*
import java.time.YearMonth
import javax.inject.Inject

class StatisticsRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : StatisticsRepository {

    override fun getMonthlyStatistics(yearMonth: YearMonth): Flow<MonthlyStatistics> {
        return combine(
            transactionDao.getMonthlyTotalsByType(yearMonth),
            transactionDao.getMonthlyCategoryStatistics(yearMonth),
            transactionDao.getMonthlyDailyStatistics(yearMonth)
        ) { totals, categories, daily ->
            MonthlyStatistics(
                yearMonth = yearMonth,
                totalIncome = totals.find { it.type == "INCOME" }?.total ?: 0.0,
                totalExpense = totals.find { it.type == "EXPENSE" }?.total ?: 0.0,
                categoryStatistics = categories.map { category ->
                    CategoryStatistics(
                        categoryId = category.categoryId,
                        categoryName = category.categoryName,
                        categoryType = category.categoryType,
                        categoryColor = category.categoryColor,
                        amount = category.amount,
                        percentage = category.percentage,
                        count = category.count
                    )
                },
                dailyStatistics = daily.map { day ->
                    DailyStatistics(
                        date = day.date,
                        income = day.income,
                        expense = day.expense
                    )
                }
            )
        }
    }

    override fun getYearlyStatistics(year: Int): Flow<YearlyStatistics> {
        return flow {
            val monthlyStats = (1..12).map { month ->
                getMonthlyStatistics(YearMonth.of(year, month))
            }.merge()
                .toList()
                .sortedBy { it.yearMonth }

            emit(YearlyStatistics(year = year, monthlyStatistics = monthlyStats))
        }
    }

    override fun getCategoryTrend(
        categoryId: Long,
        startYearMonth: YearMonth,
        endYearMonth: YearMonth
    ): Flow<CategoryTrend> {
        return transactionDao.getCategoryTrend(categoryId, startYearMonth, endYearMonth)
            .map { trend ->
                CategoryTrend(
                    categoryId = trend.categoryId,
                    categoryName = trend.categoryName,
                    categoryType = trend.categoryType,
                    categoryColor = trend.categoryColor,
                    monthlyAmounts = trend.amounts.map { amount ->
                        MonthlyAmount(
                            yearMonth = amount.yearMonth,
                            amount = amount.amount
                        )
                    }
                )
            }
    }

    override fun getMultiCategoryTrend(
        categoryIds: List<Long>,
        startYearMonth: YearMonth,
        endYearMonth: YearMonth
    ): Flow<List<CategoryTrend>> {
        return categoryIds.map { categoryId ->
            getCategoryTrend(categoryId, startYearMonth, endYearMonth)
        }.merge().toList().asFlow()
    }
} 
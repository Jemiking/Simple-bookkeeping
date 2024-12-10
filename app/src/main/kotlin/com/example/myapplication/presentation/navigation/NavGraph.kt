package com.example.myapplication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.presentation.budget.BudgetDetailScreen
import com.example.myapplication.presentation.budget.BudgetListScreen
import com.example.myapplication.presentation.category.CategoryDetailScreen
import com.example.myapplication.presentation.category.CategoryListScreen
import com.example.myapplication.presentation.settings.SettingsScreen
import com.example.myapplication.presentation.statistics.CategoryTrendScreen
import com.example.myapplication.presentation.statistics.MonthlyStatisticsScreen
import com.example.myapplication.presentation.statistics.YearlyStatisticsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.CategoryList.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 分类列表
        composable(route = Screen.CategoryList.route) {
            CategoryListScreen(
                onNavigateToAddCategory = {
                    navController.navigate(Screen.CategoryDetail.route)
                },
                onNavigateToCategoryDetail = { categoryId ->
                    navController.navigate(Screen.CategoryDetail.createRoute(categoryId))
                }
            )
        }

        // 分类详情
        composable(
            route = Screen.CategoryDetail.route + "?categoryId={categoryId}",
            arguments = listOf(
                navArgument("categoryId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { entry ->
            val categoryId = entry.arguments?.getLong("categoryId")
            CategoryDetailScreen(
                categoryId = if (categoryId == -1L) null else categoryId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 预算列表
        composable(route = Screen.BudgetList.route) {
            BudgetListScreen(
                onNavigateToAddBudget = {
                    navController.navigate(Screen.BudgetDetail.route)
                },
                onNavigateToBudgetDetail = { budgetId ->
                    navController.navigate(Screen.BudgetDetail.createRoute(budgetId))
                }
            )
        }

        // 预算详情
        composable(
            route = Screen.BudgetDetail.route + "?budgetId={budgetId}",
            arguments = listOf(
                navArgument("budgetId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { entry ->
            val budgetId = entry.arguments?.getLong("budgetId")
            BudgetDetailScreen(
                budgetId = if (budgetId == -1L) null else budgetId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 月度统计
        composable(route = Screen.MonthlyStatistics.route) {
            MonthlyStatisticsScreen()
        }

        // 年度统计
        composable(route = Screen.YearlyStatistics.route) {
            YearlyStatisticsScreen()
        }

        // 分类趋势
        composable(route = Screen.CategoryTrend.route) {
            CategoryTrendScreen()
        }

        // 设置
        composable(route = Screen.Settings.route) {
            SettingsScreen()
        }
    }
}

sealed class Screen(val route: String) {
    object CategoryList : Screen("category_list")
    object CategoryDetail : Screen("category_detail") {
        fun createRoute(categoryId: Long) = "$route?categoryId=$categoryId"
    }
    object BudgetList : Screen("budget_list")
    object BudgetDetail : Screen("budget_detail") {
        fun createRoute(budgetId: Long) = "$route?budgetId=$budgetId"
    }
    object MonthlyStatistics : Screen("monthly_statistics")
    object YearlyStatistics : Screen("yearly_statistics")
    object CategoryTrend : Screen("category_trend")
    object Settings : Screen("settings")
} 
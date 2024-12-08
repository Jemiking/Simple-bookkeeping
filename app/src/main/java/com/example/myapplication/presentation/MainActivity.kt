package com.example.myapplication.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.presentation.budget.BudgetScreen
import com.example.myapplication.presentation.category.CategoryScreen
import com.example.myapplication.presentation.main.MainScreen
import com.example.myapplication.presentation.navigation.BottomNavBar
import com.example.myapplication.presentation.navigation.NavigationRail
import com.example.myapplication.presentation.navigation.Screen
import com.example.myapplication.presentation.settings.SettingsScreen
import com.example.myapplication.presentation.statistics.StatisticsScreen
import com.example.myapplication.presentation.transaction.AddTransactionScreen
import com.example.myapplication.presentation.transaction.detail.TransactionDetailScreen
import com.example.myapplication.presentation.transaction.search.SearchTransactionScreen
import com.example.myapplication.presentation.account.transfer.AccountTransferScreen
import com.example.myapplication.presentation.account.adjust.AccountAdjustScreen
import com.example.myapplication.presentation.ui.theme.MyApplicationTheme
import com.example.myapplication.util.rememberWindowSize
import com.example.myapplication.util.rememberWindowSizeLayout
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // 获取窗口大小信息
                val windowSize = rememberWindowSize()
                val windowSizeLayout = rememberWindowSizeLayout(windowSize)
                
                // 导航控制器
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                // 是否显示导航栏
                val showNavigationBar by remember(currentDestination) {
                    derivedStateOf {
                        when (currentDestination?.route?.substringBefore("/")) {
                            Screen.AddTransaction.route -> false
                            Screen.Category.route -> false
                            "transaction_detail" -> false
                            Screen.SearchTransaction.route -> false
                            Screen.AccountTransfer.route -> false
                            Screen.AccountAdjust.route -> false
                            else -> true
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // 侧边导航栏
                        if (windowSizeLayout.showNavigationRail && showNavigationBar) {
                            NavigationRail(
                                navController = navController,
                                currentDestination = currentDestination
                            )
                        }

                        // 主内容区���
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = Screen.Home.route
                            ) {
                                composable(
                                    route = Screen.Home.route,
                                    enterTransition = {
                                        fadeIn(animationSpec = tween(300))
                                    },
                                    exitTransition = {
                                        fadeOut(animationSpec = tween(300))
                                    }
                                ) {
                                    MainScreen(
                                        onNavigateToAddTransaction = {
                                            navController.navigate(Screen.AddTransaction.route)
                                        },
                                        onNavigateToCategory = {
                                            navController.navigate(Screen.Category.route)
                                        },
                                        onNavigateToTransactionDetail = { transactionId ->
                                            navController.navigate(Screen.TransactionDetail.createRoute(transactionId))
                                        },
                                        onNavigateToSearch = {
                                            navController.navigate(Screen.SearchTransaction.route)
                                        },
                                        onNavigateToTransfer = {
                                            navController.navigate(Screen.AccountTransfer.route)
                                        },
                                        onNavigateToAdjust = {
                                            navController.navigate(Screen.AccountAdjust.route)
                                        }
                                    )
                                }
                                
                                composable(
                                    route = Screen.Statistics.route,
                                    enterTransition = {
                                        slideIntoContainer(
                                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                            animationSpec = tween(300)
                                        )
                                    },
                                    exitTransition = {
                                        slideOutOfContainer(
                                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                            animationSpec = tween(300)
                                        )
                                    }
                                ) {
                                    StatisticsScreen()
                                }
                                
                                composable(
                                    route = Screen.AddTransaction.route,
                                    enterTransition = {
                                        slideIntoContainer(
                                            towards = AnimatedContentTransitionScope.SlideDirection.Up,
                                            animationSpec = tween(300)
                                        )
                                    },
                                    exitTransition = {
                                        slideOutOfContainer(
                                            towards = AnimatedContentTransitionScope.SlideDirection.Down,
                                            animationSpec = tween(300)
                                        )
                                    }
                                ) {
                                    AddTransactionScreen(
                                        onNavigateBack = {
                                            navController.popBackStack()
                                        }
                                    )
                                }
                                
                                composable(
                                    route = Screen.Budget.route,
                                    enterTransition = {
                                        slideIntoContainer(
                                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                            animationSpec = tween(300)
                                        )
                                    },
                                    exitTransition = {
                                        slideOutOfContainer(
                                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                            animationSpec = tween(300)
                                        )
                                    }
                                ) {
                                    BudgetScreen()
                                }
                                
                                composable(
                                    route = Screen.Settings.route,
                                    enterTransition = {
                                        slideIntoContainer(
                                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                            animationSpec = tween(300)
                                        )
                                    },
                                    exitTransition = {
                                        slideOutOfContainer(
                                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                            animationSpec = tween(300)
                                        )
                                    }
                                ) {
                                    SettingsScreen(
                                        onNavigateBack = {
                                            navController.popBackStack()
                                        }
                                    )
                                }
                                
                                composable(
                                    route = Screen.Category.route,
                                    enterTransition = {
                                        slideIntoContainer(
                                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                            animationSpec = tween(300)
                                        )
                                    },
                                    exitTransition = {
                                        slideOutOfContainer(
                                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                            animationSpec = tween(300)
                                        )
                                    }
                                ) {
                                    CategoryScreen(
                                        onNavigateBack = {
                                            navController.popBackStack()
                                        }
                                    )
                                }

                                composable(
                                    route = Screen.TransactionDetail.route,
                                    arguments = listOf(
                                        navArgument("transactionId") {
                                            type = NavType.LongType
                                        }
                                    ),
                                    enterTransition = {
                                        slideIntoContainer(
                                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                            animationSpec = tween(300)
                                        )
                                    },
                                    exitTransition = {
                                        slideOutOfContainer(
                                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                            animationSpec = tween(300)
                                        )
                                    }
                                ) {
                                    TransactionDetailScreen(
                                        onNavigateBack = {
                                            navController.popBackStack()
                                        }
                                    )
                                }

                                composable(
                                    route = Screen.SearchTransaction.route,
                                    enterTransition = {
                                        slideIntoContainer(
                                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                            animationSpec = tween(300)
                                        )
                                    },
                                    exitTransition = {
                                        slideOutOfContainer(
                                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                            animationSpec = tween(300)
                                        )
                                    }
                                ) {
                                    SearchTransactionScreen(
                                        onNavigateBack = {
                                            navController.popBackStack()
                                        },
                                        onNavigateToTransactionDetail = { transactionId ->
                                            navController.navigate(Screen.TransactionDetail.createRoute(transactionId))
                                        }
                                    )
                                }

                                composable(
                                    route = Screen.AccountTransfer.route,
                                    enterTransition = {
                                        slideIntoContainer(
                                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                            animationSpec = tween(300)
                                        )
                                    },
                                    exitTransition = {
                                        slideOutOfContainer(
                                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                            animationSpec = tween(300)
                                        )
                                    }
                                ) {
                                    AccountTransferScreen(
                                        onNavigateBack = {
                                            navController.popBackStack()
                                        }
                                    )
                                }

                                composable(
                                    route = Screen.AccountAdjust.route,
                                    enterTransition = {
                                        slideIntoContainer(
                                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                            animationSpec = tween(300)
                                        )
                                    },
                                    exitTransition = {
                                        slideOutOfContainer(
                                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                            animationSpec = tween(300)
                                        )
                                    }
                                ) {
                                    AccountAdjustScreen(
                                        onNavigateBack = {
                                            navController.popBackStack()
                                        }
                                    )
                                }
                            }

                            // 底部导航栏
                            if (windowSizeLayout.showBottomBar && showNavigationBar) {
                                BottomNavBar(
                                    navController = navController,
                                    currentDestination = currentDestination,
                                    isVisible = true,
                                    modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 
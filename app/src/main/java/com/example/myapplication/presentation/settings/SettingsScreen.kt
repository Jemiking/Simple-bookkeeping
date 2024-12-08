package com.example.myapplication.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.presentation.ui.theme.ThemeSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 主题设置
            ListItem(
                headlineContent = { Text("主题设置") },
                supportingContent = { 
                    Text(
                        if (ThemeSettings.current.value.darkTheme) "深色模式" else "浅色模式"
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = if (ThemeSettings.current.value.darkTheme) {
                            Icons.Default.DarkMode
                        } else {
                            Icons.Default.LightMode
                        },
                        contentDescription = null
                    )
                },
                modifier = Modifier.clickable { showThemeDialog = true }
            )

            // 动态取色开关
            ListItem(
                headlineContent = { Text("动态取色") },
                supportingContent = { Text("使用系统主题色") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null
                    )
                },
                trailingContent = {
                    Switch(
                        checked = ThemeSettings.current.value.dynamicColor,
                        onCheckedChange = { enabled ->
                            ThemeSettings.current.value = ThemeSettings.current.value.copy(
                                dynamicColor = enabled
                            )
                        }
                    )
                }
            )
        }

        // 主题选择对话框
        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = { Text("选择主题") },
                text = {
                    Column {
                        RadioButton(
                            selected = !ThemeSettings.current.value.darkTheme,
                            onClick = {
                                ThemeSettings.current.value = ThemeSettings.current.value.copy(
                                    darkTheme = false
                                )
                                showThemeDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("浅色模式")
                        }
                        
                        RadioButton(
                            selected = ThemeSettings.current.value.darkTheme,
                            onClick = {
                                ThemeSettings.current.value = ThemeSettings.current.value.copy(
                                    darkTheme = true
                                )
                                showThemeDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("深色模式")
                        }
                        
                        RadioButton(
                            selected = !ThemeSettings.current.value.darkTheme && 
                                     ThemeSettings.current.value.dynamicColor,
                            onClick = {
                                ThemeSettings.current.value = ThemeSettings.current.value.copy(
                                    darkTheme = false,
                                    dynamicColor = true
                                )
                                showThemeDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("跟随系统")
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showThemeDialog = false }
                    ) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

@Composable
private fun RadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        content()
    }
} 
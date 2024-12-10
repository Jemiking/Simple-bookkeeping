package com.example.myapplication.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun IconPickerDialog(
    initialIcon: String,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val icons = listOf(
        Icons.Default.Category to "category",
        Icons.Default.ShoppingCart to "shopping_cart",
        Icons.Default.Restaurant to "restaurant",
        Icons.Default.LocalCafe to "cafe",
        Icons.Default.DirectionsCar to "car",
        Icons.Default.Home to "home",
        Icons.Default.LocalHospital to "hospital",
        Icons.Default.School to "school",
        Icons.Default.LocalMall to "mall",
        Icons.Default.LocalAtm to "atm",
        Icons.Default.LocalGroceryStore to "grocery",
        Icons.Default.LocalLaundryService to "laundry",
        Icons.Default.LocalPharmacy to "pharmacy",
        Icons.Default.LocalShipping to "shipping",
        Icons.Default.LocalTaxi to "taxi",
        Icons.Default.Pets to "pets",
        Icons.Default.Favorite to "favorite",
        Icons.Default.Star to "star",
        Icons.Default.Work to "work",
        Icons.Default.AccountBalance to "bank"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择图标") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                items(icons) { (icon, name) ->
                    IconItem(
                        icon = icon,
                        isSelected = name == initialIcon,
                        onClick = { onIconSelected(name) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun IconItem(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
} 
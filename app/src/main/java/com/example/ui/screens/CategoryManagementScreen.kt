package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CategoryEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    categories: List<CategoryEntity>,
    onAddCategory: (String, String, String, String, String) -> Unit,
    onUpdateCategory: (CategoryEntity, String) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Category Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("cat_manage_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = RoyalBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_category_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .testTag("category_management_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Organize Your Flow", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(
                    "Tap any category to edit its name, icon, color, or SMS keywords.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(categories, key = { it.id }) { cat ->
                val bgParsedColor = parseCategoryColor(cat.colorHex, RoyalBlue.copy(alpha = 0.15f))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { editingCategory = cat }
                        .testTag("category_card_${cat.id}"),
                    shape = RoundedCornerShape(22.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category Icon Badge
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(bgParsedColor.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIconVector(cat.iconName),
                                contentDescription = null,
                                tint = RoyalBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = cat.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                SuggestionChip(
                                    onClick = { editingCategory = cat },
                                    label = { Text(cat.tag, fontSize = 10.sp) },
                                    modifier = Modifier.height(20.dp)
                                )
                            }
                            if (cat.keywords.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Keywords: ${cat.keywords}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }

                        IconButton(
                            onClick = { editingCategory = cat },
                            modifier = Modifier.testTag("edit_category_${cat.id}")
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Category",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (cat.isCustom) {
                            IconButton(
                                onClick = { onDeleteCategory(cat) },
                                modifier = Modifier.testTag("delete_category_${cat.id}")
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Edit Category Dialog
    editingCategory?.let { cat ->
        CategoryEditDialog(
            category = cat,
            onDismiss = { editingCategory = null },
            onSave = { updatedCat, oldName ->
                onUpdateCategory(updatedCat, oldName)
                editingCategory = null
            }
        )
    }

    // Add Category Dialog
    if (showAddDialog) {
        CategoryAddDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, tag, kw, icon, color ->
                onAddCategory(name, tag, kw, icon, color)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CategoryEditDialog(
    category: CategoryEntity,
    onDismiss: () -> Unit,
    onSave: (CategoryEntity, String) -> Unit
) {
    var name by remember { mutableStateOf(category.name) }
    var tag by remember { mutableStateOf(category.tag) }
    var keywords by remember { mutableStateOf(category.keywords) }
    var selectedIcon by remember { mutableStateOf(category.iconName) }
    var selectedColor by remember { mutableStateOf(category.colorHex) }

    val iconOptions = listOf(
        "restaurant", "home", "payments", "directions_car",
        "movie", "shopping_bag", "medical_services", "shopping_cart",
        "work", "star", "person", "category"
    )

    val colorOptions = listOf(
        "#FFDAD6", "#D7E2FF", "#FFDDB4", "#DAE2FF",
        "#FFB955", "#E1E3E4", "#C2F0C2", "#E8DEF8", "#EDEEEF"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Category", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_cat_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = tag,
                    onValueChange = { tag = it },
                    label = { Text("Tag Group") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_cat_tag_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = keywords,
                    onValueChange = { keywords = it },
                    label = { Text("SMS Auto-Match Keywords") },
                    placeholder = { Text("e.g. coffee, starbucks, food") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_cat_keywords_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Choose Icon", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(iconOptions) { iconName ->
                        val isSelected = selectedIcon.equals(iconName, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) RoyalBlue else Color(0xFFF3F4F6))
                                .clickable { selectedIcon = iconName },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIconVector(iconName),
                                contentDescription = iconName,
                                tint = if (isSelected) Color.White else Color.DarkGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Text("Choose Color", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(colorOptions) { hex ->
                        val isSelected = selectedColor.equals(hex, ignoreCase = true)
                        val color = parseCategoryColor(hex, RoyalBlue)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) RoyalBlue else Color.LightGray,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val oldName = category.name
                        val updated = category.copy(
                            name = name.trim(),
                            tag = tag.trim(),
                            keywords = keywords.trim(),
                            iconName = selectedIcon,
                            colorHex = selectedColor
                        )
                        onSave(updated, oldName)
                    }
                },
                modifier = Modifier.testTag("save_category_button")
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CategoryAddDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("Essential") }
    var keywords by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("category") }
    var selectedColor by remember { mutableStateOf("#DAE2FF") }

    val iconOptions = listOf(
        "restaurant", "home", "payments", "directions_car",
        "movie", "shopping_bag", "medical_services", "shopping_cart",
        "work", "star", "person", "category"
    )

    val colorOptions = listOf(
        "#FFDAD6", "#D7E2FF", "#FFDDB4", "#DAE2FF",
        "#FFB955", "#E1E3E4", "#C2F0C2", "#E8DEF8", "#EDEEEF"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Category", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    placeholder = { Text("e.g. Gym & Fitness") },
                    modifier = Modifier.fillMaxWidth().testTag("add_cat_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = tag,
                    onValueChange = { tag = it },
                    label = { Text("Tag Group") },
                    placeholder = { Text("e.g. Health / Lifestyle") },
                    modifier = Modifier.fillMaxWidth().testTag("add_cat_tag_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = keywords,
                    onValueChange = { keywords = it },
                    label = { Text("SMS Auto-Match Keywords") },
                    placeholder = { Text("e.g. gym, fitness, workout") },
                    modifier = Modifier.fillMaxWidth().testTag("add_cat_keywords_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Choose Icon", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(iconOptions) { iconName ->
                        val isSelected = selectedIcon.equals(iconName, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) RoyalBlue else Color(0xFFF3F4F6))
                                .clickable { selectedIcon = iconName },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIconVector(iconName),
                                contentDescription = iconName,
                                tint = if (isSelected) Color.White else Color.DarkGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Text("Choose Color", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(colorOptions) { hex ->
                        val isSelected = selectedColor.equals(hex, ignoreCase = true)
                        val color = parseCategoryColor(hex, RoyalBlue)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) RoyalBlue else Color.LightGray,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), tag.trim(), keywords.trim(), selectedIcon, selectedColor)
                    }
                },
                modifier = Modifier.testTag("confirm_add_category_button")
            ) {
                Text("Add Category")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun parseCategoryColor(hex: String, fallback: Color = RoyalBlue): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorInt = android.graphics.Color.parseColor("#$cleanHex")
        Color(colorInt)
    } catch (e: Exception) {
        fallback
    }
}

fun getCategoryIconVector(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "restaurant", "food" -> Icons.Default.Restaurant
        "home", "house", "rent" -> Icons.Default.Home
        "payments", "salary", "money", "bills" -> Icons.Default.Payments
        "directions_car", "transport", "car" -> Icons.Default.DirectionsCar
        "movie", "entertainment" -> Icons.Default.Movie
        "shopping_bag", "shopping" -> Icons.Default.ShoppingBag
        "medical_services", "health", "hospital" -> Icons.Default.MedicalServices
        "shopping_cart", "groceries" -> Icons.Default.ShoppingCart
        "work", "business" -> Icons.Default.Work
        "star" -> Icons.Default.Star
        "person" -> Icons.Default.Person
        else -> Icons.Default.Category
    }
}

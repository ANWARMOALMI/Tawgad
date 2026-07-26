package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object ProductImageUtils {

    /**
     * Returns a realistic, high quality illustrative image URL for a given product or category
     */
    fun getProductImageUrl(name: String, category: String): String {
        val lowerName = name.lowercase()
        val lowerCategory = category.lowercase()

        return when {
            // Specific medications
            lowerName.contains("بنادول") || lowerName.contains("panadol") || lowerName.contains("باراسيتامول") ->
                "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=200&auto=format&fit=crop&q=80"
            lowerName.contains("أموكسيسيلين") || lowerName.contains("amox") || lowerName.contains("مضاد") ->
                "https://images.unsplash.com/photo-1471864190281-a93a3070b6de?w=200&auto=format&fit=crop&q=80"
            lowerName.contains("أوميبرازول") || lowerName.contains("معدة") || lowerName.contains("كبسول") ->
                "https://images.unsplash.com/photo-1576602976047-174e57a47881?w=200&auto=format&fit=crop&q=80"
            lowerName.contains("فيتامين") || lowerName.contains("شراب") || lowerName.contains("أطفال") ->
                "https://images.unsplash.com/photo-1550572017-edf792890003?w=200&auto=format&fit=crop&q=80"
            lowerCategory.contains("صيدل") || lowerCategory.contains("دواء") || lowerCategory.contains("أدوية") ->
                "https://images.unsplash.com/photo-1585435557343-3b092031a831?w=200&auto=format&fit=crop&q=80"

            // Auto parts
            lowerName.contains("فلتر") || lowerName.contains("زيت") || lowerName.contains("تويوتا") ->
                "https://images.unsplash.com/photo-1486262715619-67b85e0b08d3?w=200&auto=format&fit=crop&q=80"
            lowerName.contains("فحمات") || lowerName.contains("فرامل") || lowerName.contains("شمعات") ->
                "https://images.unsplash.com/photo-1619642751034-765dfdf7c58e?w=200&auto=format&fit=crop&q=80"
            lowerCategory.contains("سيارات") || lowerCategory.contains("غيار") ->
                "https://images.unsplash.com/photo-1530046339160-ce3e530c7d2f?w=200&auto=format&fit=crop&q=80"

            // Electronics & Solar
            lowerName.contains("طاقة") || lowerName.contains("إنفرتر") || lowerName.contains("بطارية") ->
                "https://images.unsplash.com/photo-1509391365360-2e959784a276?w=200&auto=format&fit=crop&q=80"
            lowerCategory.contains("إلكترونيات") ->
                "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=200&auto=format&fit=crop&q=80"

            else ->
                "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?w=200&auto=format&fit=crop&q=80"
        }
    }

    fun getCategoryIcon(category: String): ImageVector {
        return when {
            category.contains("صيدل") || category.contains("دواء") || category.contains("أدوية") -> Icons.Default.Medication
            category.contains("سيارات") || category.contains("غيار") -> Icons.Default.TwoWheeler
            category.contains("إلكترونيات") -> Icons.Default.Devices
            else -> Icons.Default.Build
        }
    }

    fun getCategoryColor(category: String): Color {
        return when {
            category.contains("صيدل") || category.contains("دواء") || category.contains("أدوية") -> Color(0xFF4F46E5)
            category.contains("سيارات") || category.contains("غيار") -> Color(0xFFEA580C)
            category.contains("إلكترونيات") -> Color(0xFF0284C7)
            else -> Color(0xFF64748B)
        }
    }
}

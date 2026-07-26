package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ProductEntity
import com.example.data.ProductSearchResult
import com.example.network.SupabaseProductDto
import com.example.util.Currency
import com.example.util.CurrencyFormatter
import com.example.util.CurrencyRates
import com.example.util.ProductImageUtils

@Composable
fun SearchAutocompleteOverlay(
    query: String,
    localSuggestions: List<ProductSearchResult> = emptyList(),
    supabaseSuggestions: List<SupabaseProductDto> = emptyList(),
    selectedCurrency: Currency,
    rates: CurrencyRates,
    onSelectLocalProduct: (ProductEntity) -> Unit,
    onSelectSupabaseProduct: (SupabaseProductDto) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = localSuggestions.size + supabaseSuggestions.size
    if (query.isBlank() || totalCount == 0) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("search_autocomplete_overlay"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // Header bar for autocomplete
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "اقتراحات البحث",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "اقتراحات تلقائية ($totalCount)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "إغلاق ✕",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { onDismiss() }
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
            ) {
                // Local DB Suggestions
                items(localSuggestions.take(5)) { result ->
                    val product = result.product
                    val imageUrl = ProductImageUtils.getProductImageUrl(product.name, product.category)
                    val catColor = ProductImageUtils.getCategoryColor(product.category)
                    val formattedPrice = CurrencyFormatter.formatPrice(result.minPriceYer ?: 0.0, selectedCurrency, rates)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectLocalProduct(product)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Product Illustrative Thumbnail Box
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(catColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = product.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Text Info
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = catColor.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = product.category,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = catColor,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }

                                if (product.activeIngredient.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = product.activeIngredient,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Price & Arrow
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formattedPrice,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Icon(
                                imageVector = Icons.Default.NorthWest,
                                contentDescription = "اختيار",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                }

                // Supabase DTO Suggestions
                items(supabaseSuggestions.take(5)) { dto ->
                    val imageUrl = ProductImageUtils.getProductImageUrl(dto.name, dto.category)
                    val catColor = ProductImageUtils.getCategoryColor(dto.category)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectSupabaseProduct(dto)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Thumbnail
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(catColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = dto.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Info
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = dto.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = catColor.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = dto.category,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = catColor,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }

                                if (dto.activeIngredient.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = dto.activeIngredient,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Default.NorthWest,
                            contentDescription = "اختيار",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                }
            }
        }
    }
}

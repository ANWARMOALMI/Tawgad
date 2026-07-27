package com.example.util

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

data class ImportedInventoryItem(
    val productName: String,
    val category: String,
    val description: String = "",
    val activeIngredient: String = "",
    val unit: String = "قطعة",
    val storeName: String = "",
    val storeCategory: String = "",
    val city: String = "",
    val address: String = "",
    val phone: String = "",
    val priceYer: Double = 0.0,
    val stockQuantity: Int = 10,
    val stockStatus: String = "متوفر"
)

data class ImportResult(
    val totalProcessed: Int,
    val productsImported: Int,
    val storesImported: Int,
    val inventoryRecordsCreated: Int,
    val errors: List<String>
)

object CsvExcelImporter {

    const val STORE_EXCEL_TEMPLATE = """اسم المنتج,الفئة,المكون الفعال / رقم القطعة,السعر (ريال يمني),الكمية,الوحدة,حالة التوفر
بنادول أدفانس 500 ملغ,صيدليات وأدوية,Paracetamol,1200,50,علبة,متوفر
أوجمنتين 1 غرام,صيدليات وأدوية,Amoxicillin,6500,20,علبة,متوفر
فولتارين 50 ملغ,صيدليات وأدوية,Diclofenac,2800,4,شريط,كمية محدودة
فلتر زيت تويوتا كامري,قطع غيار سيارات,15601-YZZT1,4500,30,قطعة,متوفر
فحمات فرامل أمامي,قطع غيار سيارات,58101-C1A00,12500,15,طقم,متوفر
شمعات احتراق نيسان,قطع غيار سيارات,22401-ED815,8000,0,قطعة,غير متوفر
"""

    const val SAMPLE_CSV_CONTENT = """اسم المنتج,الفئة,المكون الفعال,اسم الصيدلية/المحل,المدينة,العنوان,الهاتف,السعر (ريال يمني),حالة التوفر,الكمية
بنادول أدفانس 500 ملغ,صيدليات وأدوية,Paracetamol,صيدلية الأمل الحديثة,صنعاء,شارع الزبيري - بجانب المستشفى الجمهوري,771234567,1200,متوفر,50
أوجمنتين 1 غرام طعم,صيدليات وأدوية,Amoxicillin + Clavulanic,صيدلية العافية,صنعاء,شارع حدة - دوار الرويشان,772345678,6500,متوفر,30
فولتارين إيجل 50 ملغ,صيدليات وأدوية,Diclofenac Potassium,صيدلية الحكمة,عدن,كريتر - شارع أروى,733456789,2800,كمية محدودة,5
فلتر زيت تويوتا كامري 2018-2023,قطع غيار سيارات,15601-YZZT1,مركز التيسير لقطع غيار تويوتا,صنعاء,شارع 60 - الحصبة,770112233,4500,متوفر,40
فحمات فرامل أمامي هونداي سوناتا,قطع غيار سيارات,58101-C1A00,المحارفي لقطع غيار الكوري,صنعاء,شارع خولان,773445566,12500,متوفر,12
شمعات احتراق نيسان بتمول,قطع غيار سيارات,22401-ED815,القمة لقطع غيار نيسان,تعز,شارع جمال,774556677,8000,متوفر,25
مثقاب كهربائي بوش 13 ملغ 650 واط,أدوات ومعدات,BOSH GSB 13 RE,العالمية للأدوات الصناعية,صنعاء,شارع علي عبدالمغني,775667788,38000,متوفر,8
شاحن سريع أنكر 65 واط USB-C,إلكترونيات وتكنولوجيا,Anker GaNPrime,التقنية الرقمية,عدن,الشيخ عثمان,736778899,18500,متوفر,18
"""

    fun parseCsvStream(inputStream: InputStream): List<ImportedInventoryItem> {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val text = reader.use { it.readText() }
        return parseTextContent(text)
    }

    fun parseTextContent(textContent: String): List<ImportedInventoryItem> {
        val items = mutableListOf<ImportedInventoryItem>()
        val lines = textContent.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) return items

        var nameCol = 0
        var catCol = 1
        var activeCol = 2
        var storeCol = -1
        var cityCol = -1
        var addressCol = -1
        var phoneCol = -1
        var priceCol = -1
        var qtyCol = -1
        var unitCol = -1
        var statusCol = -1

        var startIndex = 0

        // Header detection
        val firstLine = lines[0]
        val headerTokens = parseCsvLine(firstLine)

        if (headerTokens.any { it.contains("اسم") || it.contains("منتج") || it.contains("Product") || it.contains("سعر") || it.contains("Price") }) {
            startIndex = 1
            headerTokens.forEachIndexed { idx, token ->
                val clean = token.trim().lowercase()
                when {
                    clean.contains("كمية") || clean.contains("العدد") || clean.contains("qty") || clean.contains("quantity") || clean.contains("stock") -> qtyCol = idx
                    clean.contains("سعر") || clean.contains("price") -> priceCol = idx
                    clean.contains("اسم") || clean.contains("منتج") || clean.contains("product") || clean.contains("name") -> nameCol = idx
                    clean.contains("فئة") || clean.contains("تصنيف") || clean.contains("category") -> catCol = idx
                    clean.contains("مادة") || clean.contains("فعال") || clean.contains("قطعة") || clean.contains("code") -> activeCol = idx
                    clean.contains("وحدة") || clean.contains("unit") -> unitCol = idx
                    clean.contains("حالة") || clean.contains("توفر") || clean.contains("status") -> statusCol = idx
                    clean.contains("صيدل") || clean.contains("محل") || clean.contains("متجر") || clean.contains("store") -> storeCol = idx
                    clean.contains("مدين") || clean.contains("city") -> cityCol = idx
                    clean.contains("عنوان") || clean.contains("address") -> addressCol = idx
                    clean.contains("هاتف") || clean.contains("تلفون") || clean.contains("phone") -> phoneCol = idx
                }
            }
        }

        for (i in startIndex until lines.size) {
            val line = lines[i]
            val tokens = parseCsvLine(line)
            if (tokens.isEmpty()) continue

            try {
                val productName = tokens.getOrNull(if (nameCol >= 0) nameCol else 0)?.trim().takeIf { !it.isNullOrBlank() } ?: continue

                val category = tokens.getOrNull(if (catCol >= 0) catCol else 1)?.trim().takeIf { !it.isNullOrBlank() } ?: "عام"

                val activeIngredient = if (activeCol >= 0) tokens.getOrNull(activeCol)?.trim() ?: "" else tokens.getOrNull(2)?.trim() ?: ""

                // Price detection fallback
                var priceYer = 0.0
                if (priceCol >= 0 && priceCol < tokens.size) {
                    val raw = tokens[priceCol].replace("[^0-9.]".toRegex(), "")
                    priceYer = raw.toDoubleOrNull() ?: 0.0
                } else {
                    // Search tokens for numeric price
                    for (t in tokens) {
                        val num = t.replace("[^0-9.]".toRegex(), "").toDoubleOrNull()
                        if (num != null && num > 10) {
                            priceYer = num
                            break
                        }
                    }
                }

                // Quantity detection fallback
                var stockQty = 10
                if (qtyCol >= 0 && qtyCol < tokens.size) {
                    val rawQty = tokens[qtyCol].replace("[^0-9]".toRegex(), "")
                    stockQty = rawQty.toIntOrNull() ?: 10
                } else if (tokens.size >= 5) {
                    // Try token index 4 or search for integer
                    val candidate = tokens.getOrNull(4)?.replace("[^0-9]".toRegex(), "")?.toIntOrNull()
                    if (candidate != null && candidate < 10000) {
                        stockQty = candidate
                    }
                }

                val unit = if (unitCol >= 0 && unitCol < tokens.size) tokens[unitCol].trim().ifBlank { "علبة" } else "علبة"

                var stockStatus = if (statusCol >= 0 && statusCol < tokens.size) tokens[statusCol].trim().ifBlank { null } else null

                if (stockStatus == null) {
                    stockStatus = when {
                        stockQty == 0 -> "غير متوفر"
                        stockQty <= 5 -> "كمية محدودة"
                        else -> "متوفر"
                    }
                }

                val storeName = if (storeCol >= 0) tokens.getOrNull(storeCol)?.trim() ?: "" else tokens.getOrNull(3)?.trim() ?: ""
                val city = if (cityCol >= 0) tokens.getOrNull(cityCol)?.trim() ?: "" else tokens.getOrNull(4)?.trim() ?: "صنعاء"
                val address = if (addressCol >= 0) tokens.getOrNull(addressCol)?.trim() ?: "" else tokens.getOrNull(5)?.trim() ?: ""
                val phone = if (phoneCol >= 0) tokens.getOrNull(phoneCol)?.trim() ?: "" else tokens.getOrNull(6)?.trim() ?: ""

                items.add(
                    ImportedInventoryItem(
                        productName = productName,
                        category = category,
                        description = "منتج مستورد عبر ملف Excel",
                        activeIngredient = activeIngredient,
                        unit = unit,
                        storeName = storeName,
                        storeCategory = if (category.contains("صيدل")) "صيدلية" else "قطع غيار ومحلات",
                        city = city,
                        address = address,
                        phone = phone,
                        priceYer = priceYer,
                        stockQuantity = stockQty,
                        stockStatus = stockStatus
                    )
                )
            } catch (e: Exception) {
                // Ignore broken line
            }
        }

        return items
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        // Detect delimiter (, or ; or \t or |)
        val delimiter = when {
            line.contains("\t") -> '\t'
            line.contains(";") -> ';'
            line.contains("|") -> '|'
            else -> ','
        }

        for (c in line) {
            when (c) {
                '"' -> inQuotes = !inQuotes
                delimiter -> {
                    if (inQuotes) {
                        sb.append(c)
                    } else {
                        result.add(sb.toString())
                        sb.clear()
                    }
                }
                else -> sb.append(c)
            }
        }
        result.add(sb.toString())
        return result
    }
}


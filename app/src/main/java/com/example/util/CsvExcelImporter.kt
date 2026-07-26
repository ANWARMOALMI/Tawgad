package com.example.util

import com.example.data.ProductEntity
import com.example.data.StoreEntity
import com.example.data.StoreInventoryEntity
import java.io.InputStream
import java.io.BufferedReader
import java.io.InputStreamReader

data class ImportedInventoryItem(
    val productName: String,
    val category: String,
    val description: String,
    val activeIngredient: String,
    val unit: String,
    val storeName: String,
    val storeCategory: String,
    val city: String,
    val address: String,
    val phone: String,
    val priceYer: Double,
    val stockStatus: String
)

data class ImportResult(
    val totalProcessed: Int,
    val productsImported: Int,
    val storesImported: Int,
    val inventoryRecordsCreated: Int,
    val errors: List<String>
)

object CsvExcelImporter {

    const val SAMPLE_CSV_CONTENT = """اسم المنتج,الفئة,المكون الفعال,اسم الصيدلية/المحل,المدينة,العنوان,الهاتف,السعر (ريال يمني),حالة التوفر
بنادول أدفانس 500 ملغ,صيدليات وأدوية,Paracetamol,صيدلية الأمل الحديثة,صنعاء,شارع الزبيري - بجانب المستشفى الجمهوري,771234567,1200,متوفر
أوجمنتين 1 غرام طعم,صيدليات وأدوية,Amoxicillin + Clavulanic,صيدلية العافية,صنعاء,شارع حدة - دوار الرويشان,772345678,6500,متوفر
فولتارين إيجل 50 ملغ,صيدليات وأدوية,Diclofenac Potassium,صيدلية الحكمة,عدن,كريتر - شارع أروى,733456789,2800,كمية محدودة
فلتر زيت تويوتا كامري 2018-2023,قطع غيار سيارات,15601-YZZT1,مركز التيسير لقطع غيار تويوتا,صنعاء,شارع 60 - الحصبة,770112233,4500,متوفر
فحمات فرامل أمامي هونداي سوناتا,قطع غيار سيارات,58101-C1A00,المحارفي لقطع غيار الكوري,صنعاء,شارع خولان,773445566,12500,متوفر
شمعات احتراق نيسان بتمول,قطع غيار سيارات,22401-ED815,القمة لقطع غيار نيسان,تعز,شارع جمال,774556677,8000,متوفر
مثقاب كهربائي بوش 13 ملغ 650 واط,أدوات ومعدات,BOSH GSB 13 RE,العالمية للأدوات الصناعية,صنعاء,شارع علي عبدالمغني,775667788,38000,متوفر
شاحن سريع أنكر 65 واط USB-C,إلكترونيات وتكنولوجيا,Anker GaNPrime,التقنية الرقمية,عدن,الشيخ عثمان,736778899,18500,متوفر
"""

    fun parseCsvStream(inputStream: InputStream): List<ImportedInventoryItem> {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val items = mutableListOf<ImportedInventoryItem>()
        var lineIndex = 0

        reader.useLines { lines ->
            lines.forEach { rawLine ->
                lineIndex++
                val line = rawLine.trim()
                if (line.isNotEmpty()) {
                    // Skip header if lineIndex == 1 and contains column keywords
                    if (lineIndex == 1 && (line.contains("اسم") || line.contains("Product") || line.contains("name"))) {
                        return@forEach
                    }

                    val tokens = parseCsvLine(line)
                    if (tokens.size >= 6) {
                        try {
                            val productName = tokens.getOrNull(0)?.trim() ?: "منتج جديد"
                            val category = tokens.getOrNull(1)?.trim().takeIf { !it.isNull_or_empty() } ?: "عام"
                            val activeIngredient = tokens.getOrNull(2)?.trim() ?: ""
                            val storeName = tokens.getOrNull(3)?.trim().takeIf { !it.isNull_or_empty() } ?: "محل عام"
                            val city = tokens.getOrNull(4)?.trim().takeIf { !it.isNull_or_empty() } ?: "صنعاء"
                            val address = tokens.getOrNull(5)?.trim() ?: "العنوان الرئيسي"
                            val phone = tokens.getOrNull(6)?.trim() ?: "770000000"
                            val priceRaw = tokens.getOrNull(7)?.trim()?.replace("[^0-9.]".toRegex(), "") ?: "0"
                            val priceYer = priceRaw.toDoubleOrNull() ?: 1000.0
                            val stockStatus = tokens.getOrNull(8)?.trim().takeIf { !it.isNull_or_empty() } ?: "متوفر"

                            items.add(
                                ImportedInventoryItem(
                                    productName = productName,
                                    category = category,
                                    description = "منتج مستورد عبر لوحة التحكم",
                                    activeIngredient = activeIngredient,
                                    unit = "قطعة",
                                    storeName = storeName,
                                    storeCategory = if (category.contains("صيدل")) "صيدلية" else "قطع غيار ومحلات",
                                    city = city,
                                    address = address,
                                    phone = phone,
                                    priceYer = priceYer,
                                    stockStatus = stockStatus
                                )
                            )
                        } catch (e: Exception) {
                            // ignore line error
                        }
                    }
                }
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
            line.contains(";") -> ';'
            line.contains("\t") -> '\t'
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

    private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
}

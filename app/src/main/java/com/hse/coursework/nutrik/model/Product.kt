package com.hse.coursework.nutrik.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable


@Entity(tableName = "products")
@Serializable
data class Product(
    @PrimaryKey
    val id: String = "def",
    val code: String = "def",
    val category: String = "",
    val name: String = "def",
    val unit: String = "",
    val proteins: Double = -1.0,
    val fats: Double = -1.0,
    val carbs: Double = -1.0,
    val energyValue: Double = -1.0,
    val composition: List<String> = ArrayList<String>(),
    val manufacturer: String= "",
    val brand: String = "",
    val weight: Double = 0.0,
    val description: String = "",
    val imageLinks: List<String> = ArrayList<String>(),
    val allergens: Set<String> = hashSetOf<String>(),
)


// DTO для Firestore
@Serializable
data class ProductDTO(
    var id: String = "def",
    val code: String = "",
    val category: String = "",
    val name: String = "",
    val unit: String = "",
    val proteins: Double = -1.0,
    val fats: Double = -1.0,
    val carbs: Double = -1.0,
    val energyValue: Double = -1.0,
    val composition: List<String> = emptyList(),
    val manufacturer: String = "",
    val brand: String = "",
    val weight: Double = 0.0,
    val description: String = "",
    val imageLinks: List<String> = emptyList(),
    val allergens: List<String> = emptyList()
)

// Конвертеры
fun ProductDTO.toDomain(id : String): Product {
    return Product(
        id = id,
        code = code,
        category = category,
        name = name,
        unit = unit,
        proteins = proteins,
        fats = fats,
        carbs = carbs,
        energyValue = energyValue,
        composition = composition,
        manufacturer = manufacturer,
        brand = brand,
        weight = weight,
        description = description,
        imageLinks = imageLinks,
        allergens = allergens.toSet()
    )
}

fun Product.toDTO(): ProductDTO {
    return ProductDTO(
        id = id,
        code = code,
        category = category,
        name = name,
        unit = unit,
        proteins = proteins,
        fats = fats,
        carbs = carbs,
        energyValue = energyValue,
        composition = composition,
        manufacturer = manufacturer,
        brand = brand,
        weight = weight,
        description = description,
        imageLinks = imageLinks,
        allergens = allergens.toList()
    )
}

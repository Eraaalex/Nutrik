package com.hse.coursework.nutrik.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hse.coursework.nutrik.utils.ConverterUtil
import kotlinx.serialization.Serializable


@Entity(tableName = "products")
@Serializable
data class ProductEntity(
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
    val sugar: Double = -1.0,
    val salt: Double = -1.0,
    val composition: List<String> = ArrayList<String>(),
    val manufacturer: String = "",
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
    val category: String? = "",
    val name: String? = "",
    val unit: String? = "",
    val proteins: Double? = -1.0,
    val fats: Double? = -1.0,
    val carbs: Double? = -1.0,
    val energyValue: Double? = -1.0,
    val sugar: Double = -1.0,
    val salt: Double = -1.0,
    val composition: List<String>? = null,
    val manufacturer: String? = null,
    val brand: String? = null,
    val weight: Double? = 0.0,
    val description: String? = null,
    val imageLinks: List<String>? = null,
    val allergens: List<String>? = null
)

// Конвертеры
fun ProductDTO.toDomain(id: String): ProductEntity {
    return ProductEntity(
        id = id,
        code = code,
        category = category ?: "",
        name = name ?: "",
        unit = unit ?: "",
        proteins = proteins ?: 0.0,
        fats = fats ?: 0.0,
        carbs = carbs ?: 0.0,
        energyValue = energyValue ?: 0.0,
        sugar = sugar ?: 0.0,
        salt = salt ?: 0.0,
        composition = composition ?: emptyList(),
        manufacturer = manufacturer ?: "",
        brand = brand ?: "",
        weight = weight ?: 0.0,
        description = description ?: "",
        imageLinks = imageLinks ?: emptyList(),
        allergens = allergens?.toSet() ?: emptySet()
    )
}

fun ProductEntity.toDTO(): ProductDTO {
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

fun Product.toEntity(): ProductEntity {
    return ProductEntity(
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
        allergens = allergens.map { it.name }.toSet()
    )
}

fun ProductEntity.toUI(): Product {
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
        allergens = ConverterUtil.fromStringToRestrictionSet(allergens)
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
        allergens = allergens.map { it.name }.toList()
    )
}

// Для работы с Product в UI
data class Product(
    val id: String = "def",
    val code: String = "def",
    val category: String = "",
    val name: String = "def",
    val unit: String = "",
    val proteins: Double = -1.0,
    val fats: Double = -1.0,
    val carbs: Double = -1.0,
    val energyValue: Double = -1.0,
    val sugar: Double = -1.0,
    val salt: Double = -1.0,
    val composition: List<String> = ArrayList<String>(),
    val manufacturer: String = "",
    val brand: String = "",
    val weight: Double = 0.0,
    val description: String = "",
    val imageLinks: List<String> = ArrayList<String>(),
    val allergens: Set<Restriction> = hashSetOf<Restriction>(),
)
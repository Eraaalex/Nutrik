package com.hse.coursework.nutrik.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Consumption(
    val productId: String = "",
    val productName: String = "",
    val userId: String = "",
    val date: LocalDate = LocalDate.now(),
    val weight: Double = 0.0
)

data class ConsumptionDTO(
    val productId: String = "",
    val productName: String = "",
    val userId: String = "",
    val date: String = "",
    val weight: Double = 0.0
)

fun ConsumptionDTO.toConsumption(): Consumption {
    return Consumption(
        productId = productId,
        userId = userId,
        productName = productName,
        date = LocalDate.parse(date.ifBlank { "2025-05-13" }, DateTimeFormatter.ISO_LOCAL_DATE),
        weight = weight
    )
}

fun Consumption.toDTO(): ConsumptionDTO {
    return ConsumptionDTO(
        productId = productId,
        userId = userId,
        productName = productName,
        date = date.toString(),
        weight = weight
    )
}

@Entity(tableName = "consumptions",
    primaryKeys = ["userId","productId","date"])
data class ConsumptionEntity(
    val productId: String,
    val productName: String,
    val userId: String,
    val date: String,
    val weight: Double
)

fun Consumption.toEntity() = ConsumptionEntity(
    productId = productId,
    date = date.toString(),
    weight = weight,
    userId = userId,
    productName = productName
)

fun ConsumptionEntity.toConsumption() = Consumption(
    productId = productId,
    date = LocalDate.parse(date.ifBlank { "2025-05-13" }, DateTimeFormatter.ISO_LOCAL_DATE),
    weight = weight,
    userId = userId,
    productName = productName
)
package com.hse.coursework.nutrik.model.dto

import com.hse.coursework.nutrik.model.Restriction

data class FirestoreUserDTO(
    val email: String = "",
    val restrictions: List<String> = emptyList(),
    val age: Int = 0,
    val gender: String = "UNSPECIFIED"
)

fun FirestoreUserDTO.toDomain(): User {
    return User(
        email = email,
        restrictions = restrictions.map { Restriction.fromString(it) },
        age = age,
        gender = Gender.valueOf(gender.uppercase())
    )
}

fun User.toDTO(): FirestoreUserDTO {
    return FirestoreUserDTO(
        email = email,
        restrictions = restrictions.map { it.name.lowercase() },
        age = age,
        gender = gender.name
    )
}

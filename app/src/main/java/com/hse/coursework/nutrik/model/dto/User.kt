package com.hse.coursework.nutrik.model.dto

import com.hse.coursework.nutrik.model.Restriction


data class User(
    val email: String = "",
    val restrictions: List<Restriction> = emptyList(),
    val age: Int = 0,
    val gender: Gender = Gender.UNSPECIFIED
)

enum class Gender {
    MALE, FEMALE, OTHER, UNSPECIFIED
}

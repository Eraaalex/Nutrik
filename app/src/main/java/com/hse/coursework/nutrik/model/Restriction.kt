package com.hse.coursework.nutrik.model


enum class Restriction(val key: String, val russianName: String) {
    NONE("none", "нет ограничений"),


    // Аллергии
    NUT("nut", "Орехи"),
    SEAFOOD("seafood", "Морепродукты"),
    LACTOSE("lactose", "Молоко"),
    EGG("egg", "Яйца"),

    // Соль / сахар
    SALT("salt", "Продукты с высоким содержанием соли"),
    SUGAR("sugar", "Продукты с высоким содержанием сахара"),

    // Мясо
    VEGETARIAN("meat", "Мясные продукты"),
    VEGAN("vegan", "Мясные и животные продукты"),

    HIGH_CARBOHYDRATE("high carbohydrate", "Высокоуглеводные продукты");

    companion object {
        fun fromString(value: String): Restriction {
            return values().find { it.key.equals(value, ignoreCase = true) } ?: NONE
        }
    }

    override fun toString(): String {
        return russianName
    }
}
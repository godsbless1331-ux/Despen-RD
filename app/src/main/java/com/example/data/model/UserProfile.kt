package com.example.data.model

enum class ProfileKind(val displayName: String, val emoji: String) {
    ADULT("Adulto", "👤"),
    CHILD("Hijo/a", "🧒"),
    BABY("Bebé", "👶"),
    PET("Mascota", "🐾")
}

data class UserProfile(
    val id: String,
    val name: String,
    val emoji: String = "🏠",
    val kind: ProfileKind = ProfileKind.ADULT,
    val diapersPerDay: Int = 6,
    val diaperSize: String = "Talla 3",
    val birthDate: String = ""
)

val DEFAULT_PROFILES = listOf(
    UserProfile("p_main", "Hogar Principal", "🏠", ProfileKind.ADULT),
    UserProfile("p_baby", "Bebé de la casa", "👶", ProfileKind.BABY, diapersPerDay = 6, diaperSize = "Talla 3"),
    UserProfile("p_business", "Negocio / Colmado", "🏪", ProfileKind.ADULT),
    UserProfile("p_vacation", "Casa de Campo", "🌴", ProfileKind.ADULT)
)

package com.example.esportstracker.ui.components

import java.util.Locale

fun gameLabel(game: String?): String = when {
    game.isNullOrBlank() -> ""
    game.contains("league", ignoreCase = true) || game.equals("lol", ignoreCase = true) ->
        "Liên Minh Huyền Thoại"
    game.contains("valorant", ignoreCase = true) -> "Valorant"
    else -> game
}

fun countryLabel(code: String?): String? {
    if (code.isNullOrBlank()) return null
    if (code.length != 2) return code
    return try {
        Locale.Builder().setRegion(code.uppercase()).build()
            .getDisplayCountry(Locale.forLanguageTag("vi"))
            .ifBlank { code }
    } catch (e: Exception) {
        code
    }
}

fun roleLabel(role: String?): String? = when (role?.lowercase()) {
    null, "" -> null
    "top" -> "Đường trên"
    "jun", "jungle" -> "Đi rừng"
    "mid" -> "Đường giữa"
    "adc" -> "Xạ thủ"
    "sup", "support" -> "Hỗ trợ"
    else -> role
}
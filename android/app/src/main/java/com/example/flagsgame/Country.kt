package com.example.flagsgame

data class Country(
    val code: String,
    val flag: String,
    val names: List<String>,
    var norms: List<String> = listOf(),
    var displayName: String = "",
    var userGuess: String? = null,
    var correct: Boolean = false
)

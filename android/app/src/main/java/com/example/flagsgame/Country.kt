package com.example.flagsgame

data class Country(
    val code: String,
    val flag: String,
    val names: List<String>,
    var norms: List<String> = listOf(),
    var capitals: List<String> = listOf(),
    var capNorms: List<String> = listOf(),
    var capitalDisplay: String = "",
    var displayName: String = "",
    var userGuess: String? = null,
    var correct: Boolean = false
)

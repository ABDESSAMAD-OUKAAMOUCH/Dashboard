package com.example.dashboard

data class Category(
    val id: String = "",            // ← أضف هذا
    val name: String = "",
    val imageBase64: String = "",
    val items: List<Item> = emptyList()
)


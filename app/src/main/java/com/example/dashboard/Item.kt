package com.example.dashboard

data class Item(
    var id: String = "",
    var itemName: String = "",
    var price: Double = 0.0,
    var imageBase64: String = ""
) {
    constructor() : this("", "", 0.0, "")
}

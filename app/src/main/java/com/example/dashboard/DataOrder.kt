package com.example.dashboard

import java.security.Timestamp
import java.util.Date

data class UserOrder(
    var userId:String="",
    val orderId: String = "",
    val itemName: String = "",
    val price: Double = 0.0,
    val imageBase64: String = "",
    val timestamp: Timestamp? = null,
    val status: String = "",
    val userName: String = "",
    val userLocation: String = "",
    val quantity:Int=1,
    val phone:String="",
    val lat:Double=0.0,
    val lng:Double=0.0
)

package com.example.dashboard

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dashboard.UserOrder
import com.example.dashboard.R
import com.google.firebase.firestore.FirebaseFirestore
import java.security.Timestamp

class Orders : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders) // يمكنك تغيير الاسم لاحقًا لو غيرت التصميم



        recyclerView=findViewById(R.id.ordersRecyclerView)
        val ordersList = mutableListOf<UserOrder>()
        val adapter = UserOrderAdapter(this,ordersList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        val db = FirebaseFirestore.getInstance()
        val sharedPreferences = getSharedPreferences("RestaurantPrefs", MODE_PRIVATE)
        val adminId = sharedPreferences.getString("adminId", null)
        val restaurantId = sharedPreferences.getString("restaurantId", null)

        if (!adminId.isNullOrEmpty() && !restaurantId.isNullOrEmpty()) {
            db.collection("admins").document(adminId)
                .collection("restaurants").document(restaurantId)
                .collection("orders").get()
                .addOnSuccessListener { orderSnapshot ->

                    val tempOrders = mutableListOf<UserOrder>()
                    val userIdsSet = mutableSetOf<String>()
                    val ordersDataMap = mutableMapOf<String, MutableList<Map<String, Any>>>()

                    // اجمع الطلبات حسب userId
                    for (orderDoc in orderSnapshot) {
                        val data = orderDoc.data
                        val userId = data["userId"] as? String ?: continue

                        userIdsSet.add(userId)

                        val orderWithId = data.toMutableMap()
                        orderWithId["orderId"] = orderDoc.id

                        if (!ordersDataMap.containsKey(userId)) {
                            ordersDataMap[userId] = mutableListOf()
                        }
                        ordersDataMap[userId]?.add(orderWithId)
                    }

                    // الآن اجلب معلومات كل مستخدم
                    for (userId in userIdsSet) {
                        db.collection("users").document(userId).get()
                            .addOnSuccessListener { userDoc ->
                                val userName = userDoc.getString("fullName") ?: "Unknown"
                                val locationMap = userDoc.get("location") as? Map<*, *>
                                val userLocation = locationMap?.get("city")?.toString() ?: "No city"
                                val latString = locationMap?.get("lat")?.toString()
                                val lngString = locationMap?.get("lng")?.toString()
                                val lat = latString?.toDoubleOrNull()
                                val lng = lngString?.toDoubleOrNull()




                                ordersDataMap[userId]?.forEach { data ->
                                    val order = lat?.let {
                                        lng?.let { it1 ->
                                            UserOrder(
                                                orderId = data["orderId"] as String,
                                                itemName = data["name"] as? String ?: "",
                                                price = (data["price"] as? Number)?.toDouble() ?: 0.0,
                                                imageBase64 = data["imageBase64"] as? String ?: "",
                                                timestamp = data["timestamp"] as? Timestamp,
                                                status = data["status"] as? String ?: "",
                                                userName = userName,
                                                userLocation = userLocation,
                                                quantity=(data["quantity"] as? Number)?.toInt() ?: 1,
                                                phone = data["phone"] as? String ?: "",
                                                lat = it.toDouble(),
                                                lng = it1.toDouble(),
                                                userId = userId // مرّر الـ userId هنا

                                            )
                                        }
                                    }
                                    if (order != null) {
                                        tempOrders.add(order)
                                    }
                                    if (order != null) {
                                        Log.d("DEBUG", "Updating user order: userId=$userId, orderId=${order.orderId}")
                                    }

                                }


                                // تحديث القائمة بعد جلب بيانات المستخدم
                                ordersList.clear()
                                ordersList.addAll(tempOrders)
                                adapter.notifyDataSetChanged()
                            }
                    }
                }
        }









//        recyclerView = findViewById(R.id.recyclerView)
//        progressBar = findViewById(R.id.progressBar3)
//        val backButton = findViewById<ImageView>(R.id.back)
//
//        recyclerView.layoutManager = LinearLayoutManager(this)
//
//        val restaurantId = getSharedPreferences("RestaurantPrefs", Context.MODE_PRIVATE)
//            .getString("restaurantId", null)
//
//        if (restaurantId != null) {
//            cartAdapter = OrderAdapter(this, cartItems, restaurantId)
//            recyclerView.adapter = cartAdapter
//            fetchCartItems()
//        } else {
//            Toast.makeText(this, "لم يتم العثور على معرّف المطعم", Toast.LENGTH_SHORT).show()
//        }
//
//        backButton.setOnClickListener {
//            finish() // للخروج من الـ Activity
//        }
//    }
//
//    private fun fetchCartItems() {
//        FirebaseFirestore.getInstance()
//            .collection("restaurants")
//            .get()
//            .addOnSuccessListener { restaurantDocs ->
//                cartItems.clear()
//                val totalRestaurants = restaurantDocs.size()
//                var processedRestaurants = 0
//
//                for (restaurantDoc in restaurantDocs) {
//                    val restaurantId = restaurantDoc.id
//
//                    restaurantDoc.reference
//                        .collection("orders")
//                        .get()
//                        .addOnSuccessListener { orderDocs ->
//                            for (doc in orderDocs) {
//                                val item = doc.toObject(DataOrder::class.java)
//                                item.id = doc.id
//                                item.restaurantId = restaurantId
//                                cartItems.add(item)
//                            }
//
//                            processedRestaurants++
//                            if (processedRestaurants == totalRestaurants) {
//                                cartAdapter.notifyDataSetChanged()
//                                progressBar.visibility = View.GONE
//                            }
//                        }
//                        .addOnFailureListener {
//                            processedRestaurants++
//                            if (processedRestaurants == totalRestaurants) {
//                                cartAdapter.notifyDataSetChanged()
//                                progressBar.visibility = View.GONE
//                            }
//                        }
//                }
//
//                if (totalRestaurants == 0) {
//                    cartAdapter.notifyDataSetChanged()
//                    progressBar.visibility = View.GONE
//                }
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "فشل تحميل المطاعم", Toast.LENGTH_SHORT).show()
//                progressBar.visibility = View.GONE
//            }
    }
}

package com.example.dashboard

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dashboard.databinding.ActivityAllItemsBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

class AllItems : AppCompatActivity() {
    private lateinit var binding: ActivityAllItemsBinding
//    private lateinit var adapter: ItemsAdapter
    private val itemList = mutableListOf<Item>()
    private lateinit var itemAdapter: ItemAdapter
    var adminId:String?=null
    private lateinit var itemRecycler: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryId = intent.getStringExtra("categoryId")
        val categoryName = intent.getStringExtra("categoryName")
        val categoryImage = intent.getStringExtra("categoryImage")
        val sharedPreferences = getSharedPreferences("RestaurantPrefs", MODE_PRIVATE)
        adminId = sharedPreferences.getString("adminId", null).toString()
        val restaurantId = sharedPreferences.getString("restaurantId", null)
        itemRecycler = findViewById(R.id.recyclerView)
        itemRecycler.layoutManager = GridLayoutManager(this, 2)

        if (restaurantId != null) {
            if (categoryId != null) {
                loadItemsForCategory(adminId!!, restaurantId, categoryId)
            }
        }


    }
    private fun loadItemsForCategory(adminId: String, restaurantId: String, categoryId: String) {
        FirebaseFirestore.getInstance()
            .collection("admins")
            .document(adminId)
            .collection("restaurants")
            .document(restaurantId)
            .collection("categories")
            .document(categoryId)
            .collection("items")
            .get()
            .addOnSuccessListener { itemDocs ->
                val items = mutableListOf<Item>()
                for (doc in itemDocs) {
                    val item = doc.toObject(Item::class.java)
                    item.id = doc.id  // هنا نربط الـ ID بالعنصر
                    items.add(item)
                }
                itemAdapter = ItemAdapter(this, items, restaurantId, categoryId)
                itemRecycler.adapter = itemAdapter
//        binding.progressBar4.visibility = View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(this, "فشل تحميل العناصر", Toast.LENGTH_SHORT).show()
            }

    }


}
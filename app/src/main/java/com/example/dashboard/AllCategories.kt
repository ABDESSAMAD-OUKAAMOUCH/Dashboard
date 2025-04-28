package com.example.dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dashboard.databinding.ActivityAllCategoriesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AllCategories : AppCompatActivity() {
    lateinit var binding: ActivityAllCategoriesBinding
    private lateinit var categoryAdapter: CategoryAdapter
    lateinit var restaurantId:String
    private lateinit var categoryRecycler: RecyclerView
    var adminId:String?=null
    private var allCategories: List<Category> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAllCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val db = FirebaseFirestore.getInstance()
// استرجاع adminId و restaurantId من SharedPreferences
        val sharedPreferences = getSharedPreferences("RestaurantPrefs", MODE_PRIVATE)
        adminId = sharedPreferences.getString("adminId", null).toString()
        val restaurantId = sharedPreferences.getString("restaurantId", null)
        Log.d("test","adminid $adminId")
//        Log.d("test","adminid $restaurantId")


        categoryRecycler = findViewById(R.id.recyclerView)

        // RecyclerView للفئات
        categoryRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        // RecyclerView للأصناف
        if (restaurantId != null) {
            fetchCategoriesWithItems(adminId!!,restaurantId)
        }

    }
    private fun fetchCategoriesWithItems(adminId: String, restaurantId: String) {
        FirebaseFirestore.getInstance()
            .collection("admins")
            .document(adminId)
            .collection("restaurants")
            .document(restaurantId)
            .collection("categories")
            .get()
            .addOnSuccessListener { categoryDocs ->
                val categories = mutableListOf<Category>()

                for (doc in categoryDocs) {
                    val category = doc.toObject(Category::class.java).copy(id = doc.id)
                    categories.add(category)
                }

                allCategories = categories

                categoryAdapter = CategoryAdapter(this, categories) { selectedCategory ->
                    // عند الضغط على فئة
//                    loadItemsForCategory(adminId, restaurantId, selectedCategory.id)
                }
                categoryRecycler.adapter = categoryAdapter

                if (categories.isNotEmpty()) {
                    // تحميل عناصر أول فئة تلقائيًا
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "فشل تحميل التصنيفات", Toast.LENGTH_SHORT).show()
            }
    }

}
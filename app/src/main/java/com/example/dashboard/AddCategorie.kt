package com.example.dashboard

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dashboard.databinding.ActivityAddCategorieBinding
import com.example.dashboard.databinding.ActivityHomeBinding
import com.google.firebase.firestore.FirebaseFirestore

class AddCategorie : AppCompatActivity() {
    lateinit var binding: ActivityAddCategorieBinding
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddCategorieBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                binding.categoryImage.setImageURI(it) // لتغيير الصورة المعروضة
            }
        }
        binding.categoryImage.setOnClickListener {
            pickImage.launch("image/*") // يفتح المعرض لاختيار صورة
        }
        binding.btnAddCategory.setOnClickListener {
            val categoryName = binding.editTextCategoryName.text.toString()

            if (::imageUri.isInitialized && categoryName.isNotEmpty()) {
                val base64Image = convertImageToBase64(imageUri)

                val categoryData = hashMapOf(
                    "name" to categoryName,
                    "imageBase64" to base64Image
                )

                // استرجاع restaurantId من SharedPreferences
                val sharedPreferences = getSharedPreferences("RestaurantPrefs", MODE_PRIVATE)
                val restaurantId = sharedPreferences.getString("restaurantId", null)

                if (restaurantId != null) {
                    FirebaseFirestore.getInstance()
                        .collection("restaurants")
                        .document(restaurantId)
                        .collection("categories")
                        .add(categoryData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "تم إضافة الفئة بنجاح", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "فشل في إضافة الفئة", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "لم يتم العثور على معرّف المطعم", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(this, "يرجى اختيار صورة وملء الاسم", Toast.LENGTH_SHORT).show()
            }


        }
    }
    fun convertImageToBase64(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
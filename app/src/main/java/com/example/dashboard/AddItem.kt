package com.example.dashboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.util.Base64

class AddItem : AppCompatActivity() {

    private lateinit var categorySpinner: Spinner
    private lateinit var itemNameEditText: EditText
    private lateinit var itemPriceEditText: EditText
    private lateinit var itemDescriptionEditText: EditText
    private lateinit var uploadImageView: ImageView
    private lateinit var addItemButton: Button

    private var base64Image: String = ""
    private val categoryList = mutableListOf<String>()
    private val categoryIdList = mutableListOf<String>()

    private val IMAGE_PICK_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        categorySpinner = findViewById(R.id.categories)
        itemNameEditText = findViewById(R.id.etItemName)
        itemPriceEditText = findViewById(R.id.etItemPrice)
        itemDescriptionEditText = findViewById(R.id.etDescription)
        uploadImageView = findViewById(R.id.itemImage)
        addItemButton = findViewById(R.id.btnAddItem)

        loadCategories()

        uploadImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        addItemButton.setOnClickListener {
            addItemToFirestore()
        }
    }

    private fun loadCategories() {
        val sharedPreferences = getSharedPreferences("RestaurantPrefs", Context.MODE_PRIVATE)
        val restaurantId = sharedPreferences.getString("restaurantId", null)

        if (restaurantId == null) {
            Toast.makeText(this, "Restaurant ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("restaurants")
            .document(restaurantId)
            .collection("categories")
            .get()
            .addOnSuccessListener { snapshot ->
                categoryList.clear()
                categoryIdList.clear()

                for (doc in snapshot) {
                    val categoryName = doc.getString("name") ?: continue
                    categoryList.add(categoryName)
                    categoryIdList.add(doc.id)
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load categories", Toast.LENGTH_SHORT).show()
            }
    }


    private fun addItemToFirestore() {
        val itemName = itemNameEditText.text.toString()
        val price = itemPriceEditText.text.toString().toDoubleOrNull() ?: 0.0
        val description = itemDescriptionEditText.text.toString()

        val selectedCategoryIndex = categorySpinner.selectedItemPosition
        if (selectedCategoryIndex < 0 || selectedCategoryIndex >= categoryIdList.size) {
            Toast.makeText(this, "Invalid category selected", Toast.LENGTH_SHORT).show()
            return
        }

        val categoryId = categoryIdList[selectedCategoryIndex]

        val sharedPreferences = getSharedPreferences("RestaurantPrefs", Context.MODE_PRIVATE)
        val restaurantId = sharedPreferences.getString("restaurantId", null)

        val itemData = hashMapOf(
            "itemName" to itemName,
            "price" to price,
            "description" to description,
            "imageBase64" to base64Image
        )

        if (restaurantId != null) {
            FirebaseFirestore.getInstance()
                .collection("restaurants")
                .document(restaurantId)
                .collection("categories")
                .document(categoryId)
                .collection("Items")
                .add(itemData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show()
                }
        }else{
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            uploadImageView.setImageURI(imageUri)

            val inputStream = contentResolver.openInputStream(imageUri!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            base64Image = encodeImageToBase64(bitmap)
        }
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream) // استخدم PNG
        val imageBytes = outputStream.toByteArray()
        return android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT)
    }

}

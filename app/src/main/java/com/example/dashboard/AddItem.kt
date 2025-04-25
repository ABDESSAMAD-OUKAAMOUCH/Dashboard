package com.example.dashboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.InputStream
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
        val restaurantId = sharedPreferences.getString("restaurantId", null) ?: run {
            Toast.makeText(this, "Restaurant ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        val adminId = sharedPreferences.getString("adminId", null) ?: run {
            Toast.makeText(this, "Admin ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("admins")
            .document(adminId)
            .collection("restaurants")
            .document(restaurantId)
            .collection("categories")
            .get()
            .addOnSuccessListener { snapshot ->
                categoryList.clear()
                categoryIdList.clear()

                for (doc in snapshot.documents) {
                    val categoryName = doc.getString("name") ?: continue
                    categoryList.add(categoryName)
                    categoryIdList.add(doc.id)
                }

                if (categoryList.isEmpty()) {
                    Toast.makeText(this, "No categories found", Toast.LENGTH_SHORT).show()
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load categories: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("LoadCategories", "Error loading categories", e)
            }
    }

    private fun addItemToFirestore() {
        val itemName = itemNameEditText.text.toString().trim()
        if (itemName.isEmpty()) {
            Toast.makeText(this, "Please enter item name", Toast.LENGTH_SHORT).show()
            return
        }

        val price = itemPriceEditText.text.toString().toDoubleOrNull() ?: run {
            Toast.makeText(this, "Invalid price", Toast.LENGTH_SHORT).show()
            return
        }

        val description = itemDescriptionEditText.text.toString().trim()
        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT).show()
            return
        }

        if (base64Image.isEmpty()) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedCategoryIndex = categorySpinner.selectedItemPosition
        if (selectedCategoryIndex < 0 || selectedCategoryIndex >= categoryIdList.size) {
            Toast.makeText(this, "Please select a valid category", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPreferences = getSharedPreferences("RestaurantPrefs", Context.MODE_PRIVATE)
        val restaurantId = sharedPreferences.getString("restaurantId", null) ?: run {
            Toast.makeText(this, "Restaurant ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        val adminId = sharedPreferences.getString("adminId", null) ?: run {
            Toast.makeText(this, "Admin ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        val categoryId = categoryIdList[selectedCategoryIndex]

        val itemData = hashMapOf(
            "itemName" to itemName,
            "price" to price,
            "description" to description,
            "imageBase64" to base64Image,
            "timestamp" to FieldValue.serverTimestamp()
        )

        FirebaseFirestore.getInstance()
            .collection("admins")
            .document(adminId)
            .collection("restaurants")
            .document(restaurantId)
            .collection("categories")
            .document(categoryId)
            .collection("items") // يجب أن تكون الأحرف صغيرة لتجنب مشاكل التوافق
            .add(itemData)
            .addOnSuccessListener {
                Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show()
                clearForm()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add item: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                Log.e("AddItem", "Error adding item", e)
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data ?: run {
                Toast.makeText(this, "Invalid image selected", Toast.LENGTH_SHORT).show()
                return
            }

            var inputStream: InputStream? = null
            try {
                inputStream = contentResolver.openInputStream(imageUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                uploadImageView.setImageURI(imageUri)
                base64Image = encodeImageToBase64(bitmap)
            } catch (e: Exception) {
                Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show()
                Log.e("ImageProcessing", "Error processing image", e)
            } finally {
                inputStream?.close()
            }
        }
    }

    private fun clearForm() {
        itemNameEditText.text.clear()
        itemPriceEditText.text.clear()
        itemDescriptionEditText.text.clear()
        base64Image = ""
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream) // استخدم PNG
        val imageBytes = outputStream.toByteArray()
        return android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT)
    }

}

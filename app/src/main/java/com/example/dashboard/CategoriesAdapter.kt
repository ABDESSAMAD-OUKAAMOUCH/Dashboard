package com.example.dashboard

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class CategoryAdapter(
    private val context: Context,
    private val categories: MutableList<Category>, // مهم: خليتها MutableList عشان نقدر نحذف منها
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val nameText: TextView = itemView.findViewById(R.id.categoryName)
        val container: CardView = itemView.findViewById(R.id.container)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.all_categories_layout, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.nameText.text = category.name

        // عرض الصورة
        val imageBytes = android.util.Base64.decode(category.imageBase64, android.util.Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        holder.imageView.setImageBitmap(bitmap)

        // الانتقال إلى صفحة الأصناف عند الضغط
        holder.itemView.setOnClickListener {
            val intent = Intent(context, AllItems::class.java)
            intent.putExtra("categoryId", category.id)
            intent.putExtra("categoryName", category.name)
            intent.putExtra("categoryImage", category.imageBase64)
            context.startActivity(intent)
        }
        // حذف الفئة عند الضغط على زر الحذف مع تأكيد
        holder.deleteButton.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Categorie")
                .setMessage("Are you sure you want to delete this categorie?")
                .setPositiveButton("Yes") { _, _ ->
                    deleteCategory(category, position)
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    override fun getItemCount(): Int = categories.size

    private fun deleteCategory(category: Category, position: Int) {
        val db = FirebaseFirestore.getInstance()
        val sharedPreferences = context.getSharedPreferences("RestaurantPrefs", Context.MODE_PRIVATE)
        val adminId = sharedPreferences.getString("adminId", null)
        val restaurantId = sharedPreferences.getString("restaurantId", null)

        if (adminId != null && restaurantId != null) {
            db.collection("admins")
                .document(adminId)
                .collection("restaurants")
                .document(restaurantId)
                .collection("categories")
                .document(category.id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Categorie Deleted successfully", Toast.LENGTH_SHORT).show()
                    categories.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, categories.size)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to delete: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "معرف الأدمن أو المطعم غير موجود", Toast.LENGTH_SHORT).show()
        }
    }


}
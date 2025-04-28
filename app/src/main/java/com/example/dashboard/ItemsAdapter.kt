package com.example.dashboard

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.Base64
import com.google.firebase.firestore.FirebaseFirestore

class ItemAdapter(
    private val context: Context,
    private val itemList: List<Item>,
    private val restaurantId: String,
    private val categoryId: String
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.itemimage)
        val itemName: TextView = itemView.findViewById(R.id.itemName)
        val itemPrice: TextView = itemView.findViewById(R.id.itemPrice)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_layout, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.itemName.text = item.itemName
        holder.itemPrice.text = "${item.price}$"

        val imageBytes = android.util.Base64.decode(item.imageBase64, android.util.Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        holder.itemImage.setImageBitmap(bitmap)

        holder.deleteButton.setOnClickListener {
            // عرض مربع تأكيد قبل الحذف
            val builder = androidx.appcompat.app.AlertDialog.Builder(context)
            builder.setTitle("Delete Item")
            builder.setMessage("Are you sure you want to delete this item?")

            builder.setPositiveButton("Yes") { dialog, _ ->
                // إذا وافق المستخدم -> حذف المنتج من Firestore
                val db = FirebaseFirestore.getInstance()
                val sharedPreferences = context.getSharedPreferences("RestaurantPrefs", Context.MODE_PRIVATE)
                val adminId = sharedPreferences.getString("adminId", null)

                if (adminId != null) {
                    db.collection("admins")
                        .document(adminId)
                        .collection("restaurants")
                        .document(restaurantId)
                        .collection("categories")
                        .document(categoryId)
                        .collection("items")
                        .document(item.id)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Item deleted successfully", Toast.LENGTH_SHORT).show()
                            (itemList as ArrayList<Item>).removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, itemList.size)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to delete : ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "adminId غير موجود", Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()
            }

            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

            builder.show()
        }
    }


    override fun getItemCount(): Int = itemList.size
}

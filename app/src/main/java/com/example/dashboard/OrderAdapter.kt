package com.example.dashboard

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale


class UserOrderAdapter(var context: Context,private val orders: List<UserOrder>) :
    RecyclerView.Adapter<UserOrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageItem: ImageView = view.findViewById(R.id.imageItem)
        val itemName: TextView = view.findViewById(R.id.itemName)
        val itemPrice: TextView = view.findViewById(R.id.itemPrice)
        val spinnerStatus: Spinner = view.findViewById(R.id.spinnerStatus)
        val userName: TextView = view.findViewById(R.id.userName)
        val userLocation: TextView = view.findViewById(R.id.userLocation)
        val phone: TextView = view.findViewById(R.id.phone)
        val quantity: TextView = view.findViewById(R.id.quantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_cart, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]

        holder.itemName.text = order.itemName
        holder.itemPrice.text = "Price: ${order.price}"
        holder.userName.text = "User: ${order.userName}"
        holder.phone.text = "Phone: ${order.phone}"
        holder.userLocation.text = "Location: ${order.userLocation}"
        holder.quantity.text = "quantity: ${order.quantity}"
        holder.userLocation.setOnClickListener {
            val lat = order.lat
            val lng = order.lng
            try {
                val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${URLEncoder.encode("موقع المستخدم", "UTF-8")})")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")

                // إذا ما فيه خرائط قوقل، خليه مفتوح لأي تطبيق خرائط
                if (intent.resolveActivity(context.packageManager) == null) {
                    intent.setPackage(null)
                }

                context.startActivity(intent)  // استخدم context هنا

            } catch (e: Exception) {
                Toast.makeText(context, "حدث خطأ أثناء فتح الخرائط", Toast.LENGTH_SHORT).show()
                Log.e("MapsError", "Error opening maps", e)
            }
        }

        // عرض الصورة
        val imageBytes = Base64.decode(order.imageBase64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        holder.imageItem.setImageBitmap(bitmap)

        // إعداد الـ Spinner مع الحالات الممكنة
        val statuses = listOf("Pending", "Accepted", "Rejected", "Delivered")
        val spinnerAdapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_item, statuses)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.spinnerStatus.adapter = spinnerAdapter

        // تحديد الحالة الحالية بناءً على بيانات الطلب
        val currentIndex = statuses.indexOf(order.status)
        if (currentIndex != -1) {
            holder.spinnerStatus.setSelection(currentIndex)
            // تغيير اللون حسب الحالة الحالية
            when (order.status) {
                "Delivered" -> holder.spinnerStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.green))
                "Pending" -> holder.spinnerStatus.setBackgroundColor(Color.TRANSPARENT)
                "Accepted" -> holder.spinnerStatus.setBackgroundColor(Color.TRANSPARENT)
                "Rejected" -> holder.spinnerStatus.setBackgroundColor(Color.TRANSPARENT)
            }

        }

        // التعامل مع تغيير الحالة
        holder.spinnerStatus.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            var isFirstTime = true

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if (isFirstTime) {
                    isFirstTime = false
                    return
                }

                val newStatus = statuses[pos]


                val db = FirebaseFirestore.getInstance()
                // الحصول على adminId و restaurantId من SharedPreferences
                val sharedPreferences = holder.itemView.context.getSharedPreferences("RestaurantPrefs", Context.MODE_PRIVATE)
                val adminId = sharedPreferences.getString("adminId", null)
                val restaurantId = sharedPreferences.getString("restaurantId", null)

                if (adminId != null && restaurantId != null) {
                    db.collection("admins").document(adminId)
                        .collection("restaurants").document(restaurantId)
                        .collection("orders").document(order.orderId)
                        .update("status", newStatus)
                        .addOnSuccessListener {
                            // تحديث الحالة في طلبات المستخدم باستخدام userId من الطلب
                            val userId = order.userId  // تأكد أن order يحتوي على userId

                            if (userId != null) {
                                Log.d("DEBUG", "Updating user order: userId=$userId, orderId=${order.orderId}")

                                db.collection("users").document(userId)
                                    .collection("orders").document(order.orderId)
                                    .update("status", newStatus)
                                    .addOnSuccessListener {
                                        Toast.makeText(holder.itemView.context, "Status updated in both Admin and User orders", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(holder.itemView.context, "Failed to update status in User orders", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            // تغيير اللون حسب الحالة الحالية
                            when (newStatus) {
                                "Delivered" -> holder.spinnerStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.green))
                                "Pending" -> holder.spinnerStatus.setBackgroundColor(Color.TRANSPARENT)
                                "Accepted" -> holder.spinnerStatus.setBackgroundColor(Color.TRANSPARENT)
                                "Rejected" -> holder.spinnerStatus.setBackgroundColor(Color.TRANSPARENT)
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(holder.itemView.context, "Failed to update status in Admin orders", Toast.LENGTH_SHORT).show()
                        }
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
    }

    override fun getItemCount(): Int = orders.size
}

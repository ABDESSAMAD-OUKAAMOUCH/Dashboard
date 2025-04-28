package com.example.dashboard

import com.example.dashboard.databinding.ActivityProfileBinding
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val db = FirebaseFirestore.getInstance()
    var adminId =""
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences = getSharedPreferences("RestaurantPrefs", MODE_PRIVATE)
        adminId = sharedPreferences.getString("adminId", null).toString()
        disableFields()
        fetchAdminData()

        binding.editProfile.setOnClickListener {
            if (!isEditMode) {
                enableEditMode()
            } else {
                saveProfileChanges()
            }
        }
    }

    private fun fetchAdminData() {
        adminId?.let { id ->
            Log.d("AdminProfile", "adminId: $adminId")

            db.collection("admins").document(id)

                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        binding.name.setText(document.getString("name"))
                        binding.email.setText(document.getString("email"))
                        binding.phone.setText(document.getString("phone"))
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "فشل في جلب البيانات", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfileChanges() {
        disableEditMode()

        val updates = hashMapOf<String, Any>(
            "name" to binding.name.text.toString(),
            "email" to binding.email.text.toString(),
            "phone" to binding.phone.text.toString()
        )

        adminId?.let { id ->
            db.collection("admins").document(id)
                .update(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "تم تحديث البيانات بنجاح", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "فشل التحديث", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun enableEditMode() {
        isEditMode = true
        binding.editProfile.text = "Save"
        binding.editProfile.setBackgroundColor(ContextCompat.getColor(this, R.color.Raspberry))
        binding.name.isEnabled = true
        binding.phone.isEnabled = true
    }

    private fun disableEditMode() {
        isEditMode = false
        binding.editProfile.text = "Edit Profile"
        binding.editProfile.setBackgroundColor(ContextCompat.getColor(this, R.color.gray1))
        disableFields()
    }

    private fun disableFields() {
        binding.name.isEnabled = false
        binding.email.isEnabled = false
        binding.phone.isEnabled = false
    }
}

//        binding.logout.setOnClickListener {
//            FirebaseAuth.getInstance().signOut()
//
//            val intent = Intent(this, SignIn::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(intent)
//        }


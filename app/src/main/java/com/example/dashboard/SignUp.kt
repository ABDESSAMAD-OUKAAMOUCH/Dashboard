package com.example.dashboard

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.dashboard.databinding.ActivitySignUpBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.Base64
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.UUID


class SignUp : AppCompatActivity() {
    lateinit var binding: ActivitySignUpBinding
    var mAut: FirebaseAuth? = null
    var passwordShowing = false
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var imageUri: Uri
    private val firestore = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAut = FirebaseAuth.getInstance()
        ////
        FirebaseApp.initializeApp(this)
//        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
//            uri?.let {
//                imageUri = it
//                binding.categoryImage.setImageURI(uri)
//            }
//        }

        ////
        binding.signIn.setOnClickListener {
            var intent = Intent(this, SignIn::class.java)
            startActivity(intent)
        }
        binding.passwordIcon.setOnClickListener {
            passwordShowing = !passwordShowing
            passwordIcon(passwordShowing)
        }
        binding.signUp.setOnClickListener {
            val name = binding.name.text.toString()
            val phone = binding.phone.text.toString()
            val email = binding.emailAddress.text.toString()
            val password = binding.password.text.toString()
            val restaurantName = binding.lastname.text.toString()
            val url = binding.url.text.toString()
            val urlImage = binding.imageUploadSection.text.toString()

            if (name.isNotEmpty() && phone.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()
                && restaurantName.isNotEmpty() && url.isNotEmpty() && urlImage.isNotEmpty()
            ) {
                val mProgressDialog = ProgressDialog(this)
                mProgressDialog.setMessage("Loading...")
                mProgressDialog.setCanceledOnTouchOutside(false)
                mProgressDialog.show()

                mAut?.createUserWithEmailAndPassword(email, password)?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        val user = mAut?.currentUser

                        user?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
                            if (verifyTask.isSuccessful) {
                                val adminData = hashMapOf(
                                    "name" to name,
                                    "phone" to phone,
                                    "email" to email
                                )

                                FirebaseFirestore.getInstance()
                                    .collection("admins")
                                    .add(adminData)
                                    .addOnSuccessListener { adminRef ->
                                        val adminId = adminRef.id

                                        val restaurantData = hashMapOf(
                                            "restaurantName" to restaurantName,
                                            "restaurantUrl" to url,
                                            "imageBase64" to urlImage,
                                            "placeholder" to true
                                        )

                                        adminRef.collection("restaurants")
                                            .add(restaurantData)
                                            .addOnSuccessListener { restaurantRef ->
                                                val restaurantId = restaurantRef.id

                                                val sharedPreferences = getSharedPreferences("RestaurantPrefs", MODE_PRIVATE)
                                                sharedPreferences.edit()
                                                    .putString("adminId", adminId)  // 👈 حفظ adminId
                                                    .putString("restaurantId", restaurantId)
                                                    .apply()

                                                val placeholderOrder = hashMapOf("note" to "عنصر مؤقت - يمكن حذفه لاحقًا")
                                                restaurantRef.collection("orders")
                                                    .document("placeholder")
                                                    .set(placeholderOrder)
                                                    .addOnSuccessListener {
                                                        mProgressDialog.dismiss()
                                                        Toast.makeText(this, "Account created & verified\nCheck your email", Toast.LENGTH_SHORT).show()
                                                        startActivity(Intent(this, SignIn::class.java))
                                                    }
                                                    .addOnFailureListener {
                                                        mProgressDialog.dismiss()
                                                        Toast.makeText(this, "Restaurant added, failed to create order doc", Toast.LENGTH_SHORT).show()
                                                    }
                                            }
                                            .addOnFailureListener {
                                                mProgressDialog.dismiss()
                                                Toast.makeText(this, "Failed to add restaurant", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                    .addOnFailureListener {
                                        mProgressDialog.dismiss()
                                        Toast.makeText(this, "Failed to add admin", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                mProgressDialog.dismiss()
                                Toast.makeText(this, verifyTask.exception?.message ?: "Verification email failed", Toast.LENGTH_SHORT).show()
                            }
                        }

                    } else {
                        mProgressDialog.dismiss()
                        Toast.makeText(this, it.exception?.message ?: "Sign-up failed", Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun passwordIcon(isShow: Boolean) {
        if (isShow) {
            binding.password.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
            binding.passwordIcon.setImageResource(R.drawable.open)
        } else {
            binding.password.transformationMethod =
                PasswordTransformationMethod.getInstance()
            binding.passwordIcon.setImageResource(R.drawable.closed)
        }
        binding.password.setSelection(binding.password.text.length)
    }
//    fun convertImageToBase64(uri: Uri): String {
//        val inputStream = contentResolver.openInputStream(uri)
//        val bytes = inputStream?.readBytes()
//        return android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
//    }
//    fun convertImageToBase64(uri: Uri): String {
//        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
//        val stream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream) // قلل الجودة لتصغير الحجم
//        val byteArray = stream.toByteArray()
//        return android.util.Base64.encodeToString(byteArray, android.util.Base64.NO_WRAP)
//    }
}
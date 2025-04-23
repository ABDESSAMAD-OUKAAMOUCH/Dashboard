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
            val restaurantName = binding.firstname.text.toString()
            val url = binding.url.text.toString()
            val urlImage = binding.imageUploadSection.text.toString()
            if (binding.imageUploadSection.text.isNotEmpty() && restaurantName.isNotEmpty() && url.isNotEmpty() &&
                binding.emailAddress.text.isNotEmpty() && binding.firstname.text.isNotEmpty() && binding.lastname.text.isNotEmpty() &&
                binding.phone.text.isNotEmpty() && binding.password.text.isNotEmpty()
            ) {

                val restaurantData = hashMapOf(
                    "restaurantName" to restaurantName,
                    "restaurantUrl" to url,
                    "imageBase64" to urlImage,
                    "placeholder" to true
                )

                FirebaseFirestore.getInstance()
                    .collection("restaurants")
                    .add(restaurantData)
                    .addOnSuccessListener { documentReference ->
                        val restaurantId = documentReference.id

                        // ✅ تخزين restaurantId في SharedPreferences
                        val sharedPreferences = getSharedPreferences("RestaurantPrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("restaurantId", restaurantId)
                        editor.apply()

                        // ✅ إنشاء Collection "orders" مع وثيقة مؤقتة
                        val placeholderOrder = hashMapOf(
                            "note" to "عنصر مؤقت - يمكن حذفه لاحقًا"
                        )
                        documentReference.collection("orders")
                            .document("placeholder")
                            .set(placeholderOrder)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Restaurant Uploaded", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Restaurant uploaded but failed to create orders.", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Upload failed!", Toast.LENGTH_SHORT).show()
                    }

                val mProgressDialog = ProgressDialog(this)
                mProgressDialog.setMessage("Loading...")
                mProgressDialog.setCanceledOnTouchOutside(false)
                mProgressDialog.show()
                mAut?.createUserWithEmailAndPassword(
                    binding.emailAddress.text.toString(),
                    binding.password.text.toString()
                )?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        var user = mAut?.currentUser
                        user?.sendEmailVerification()?.addOnCompleteListener {
                            if (it.isSuccessful) {
                                mProgressDialog.dismiss()
                                Toast.makeText(
                                    this,
                                    "account created,\n check your email for verify your account",
                                    Toast.LENGTH_SHORT
                                ).show()
                                var intent = Intent(this, SignIn::class.java)
                                startActivity(intent)
                            } else {
                                mProgressDialog.dismiss()
                                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT)
                                    .show();
                            }
                        }
                    } else {
                        mProgressDialog.dismiss()
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this, "Empty", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private fun passwordIcon(isShow: Boolean) {
        if (isShow) {
            binding.password.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.passwordIcon.setImageResource(R.drawable.open)
        } else {
            binding.password.transformationMethod = PasswordTransformationMethod.getInstance()
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
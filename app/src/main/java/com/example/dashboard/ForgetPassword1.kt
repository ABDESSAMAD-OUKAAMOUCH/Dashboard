package com.example.dashboard

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dashboard.databinding.ActivityForgetPassword1Binding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth

class ForgetPassword1 : AppCompatActivity() {
    lateinit var binding: ActivityForgetPassword1Binding
    lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityForgetPassword1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        binding.signUp.setOnClickListener {
            var email = binding.emailAddress.text.toString()
            if(binding.emailAddress.text.isNotEmpty()){
                val mProgressDialog = ProgressDialog(this)
                mProgressDialog.setMessage("Loading...")
                mProgressDialog.setCanceledOnTouchOutside(false)
                mProgressDialog.show()
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener {
                    if (it.isSuccessful){
                        mProgressDialog.dismiss()
                        Toast.makeText(this, "Email sent", Toast.LENGTH_SHORT).show();
//                        var intent= Intent(this,ForgetPassword2::class.java)
                        intent.putExtra("email",binding.emailAddress.text.toString())
                        startActivity(intent)
                        finish()
                    }
                    else{
                        mProgressDialog.dismiss()
                        Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            else{
                binding.emailAddress.error="enter your email"
            }
        }
    }
}
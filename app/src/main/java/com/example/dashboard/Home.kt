package com.example.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dashboard.databinding.ActivityForgetPassword1Binding
import com.example.dashboard.databinding.ActivityHomeBinding

class Home : AppCompatActivity() {
    lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.linearLayout2.setOnClickListener {
            startActivity(Intent(this,AddItem::class.java))
        }
        binding.linearLayout.setOnClickListener {
            startActivity(Intent(this,AddCategorie::class.java))
        }
    }
}
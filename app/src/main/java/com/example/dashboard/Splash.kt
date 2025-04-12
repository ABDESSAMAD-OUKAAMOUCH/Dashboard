package com.example.dashboard

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.lifecycleScope
import com.example.dashboard.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Splash : AppCompatActivity() {
    var mAuth: FirebaseAuth? = null
    lateinit var binding: ActivitySplashBinding
    var alertDialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mAuth = FirebaseAuth.getInstance()
        lifecycleScope.launch {
            delay(4000)
            checkAndProceed()
        }
    }

    fun checkAndProceed() {
        if (checkForInternet(this@Splash)) {
            startActivity(Intent(this@Splash, SignIn::class.java))
            finish()
        } else {
            showNoInternetDialog()
        }
    }

    fun checkForInternet(context: Context): Boolean {

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val network = connectivityManager.activeNetwork ?: return false

            val activeNetwork =
                connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {

                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true

                else -> false
            }
        } else {
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    fun showNoInternetDialog() {
//        binding.gifImageView.visibility=View.GONE
        if (alertDialog != null && alertDialog!!.isShowing) {
            return
        }
        var myDialog = AlertDialog.Builder(this@Splash)
            .setTitle("Disconnected")
            .setMessage("check your internet connection")
            .setCancelable(false)
        var view = layoutInflater.inflate(R.layout.alert_dialog, null, false)
        myDialog.setView(view)
        alertDialog = myDialog.create()
        alertDialog?.show()
//        view.findViewById<Button>(R.id.tryAgain).setOnClickListener {
//            binding.gifImageView.visibility=View.VISIBLE
        if (checkForInternet(this@Splash)) {
            alertDialog?.dismiss()
//                startActivity(Intent(this@Splash, Location1::class.java))
            finish()
        } else {
            alertDialog?.dismiss()
            showNoInternetDialog()
        }
    }

    override fun onStart() {
        super.onStart()
        if (mAuth?.currentUser != null) {
            startActivity(Intent(this, Home::class.java))
            finish()
        }
    }
}


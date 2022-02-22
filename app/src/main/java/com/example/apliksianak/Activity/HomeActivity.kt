package com.example.apliksianak.Activity

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.apliksianak.MainActivity
import com.example.apliksianak.R
import com.example.apliksianak.dataClass.StoreData
import com.example.apliksianak.dataClass.UsersId
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.android.synthetic.main.activity_home.*
import java.text.SimpleDateFormat
import java.util.*


class HomeActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQ_CODE = 1000;
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var mAuth: FirebaseAuth
    private lateinit var qrCode: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        qrCode = findViewById(R.id.qrCode)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getCurrentLocation()

        mAuth = FirebaseAuth.getInstance()
        tvTitleHome.text = "Hello, ${mAuth.currentUser?.displayName.toString()}"
        btnLogout.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun getCurrentDate():String{
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
        return sdf.format(Date())
    }

    private fun generateQr(){

        val data = mAuth.currentUser?.uid.toString()

        if (data.isEmpty()){
            Toast.makeText(this, "Enter some data", Toast.LENGTH_SHORT).show()
        } else {
            val writer = QRCodeWriter()
            try {
                val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                for(x in 0 until width){
                    for (y in 0 until height){
                        bmp.setPixel(x, y, if (bitMatrix[x,y]) Color.BLACK else Color.WHITE)
                    }
                }
                qrCode.setImageBitmap(bmp)
            }catch (e: WriterException){
                e.printStackTrace()
            }
        }
    }

    private fun getCurrentLocation() {
        // checking location permission
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQ_CODE);
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                // getting the last known or current location
                latitude = location.latitude
                longitude = location.longitude
                location.provider
                var waktu = getCurrentDate()
                val ref = FirebaseDatabase.getInstance().getReference("Users")
                val storedata = StoreData(waktu,latitude, longitude,mAuth.currentUser?.displayName.toString(), mAuth.currentUser?.photoUrl.toString(),location.provider)

                ref.child("Childs").child(mAuth.currentUser?.uid.toString()).setValue(storedata).addOnCompleteListener {
                    Log.d("HomeActivity", mAuth.currentUser?.uid.toString())
                    generateQr()
                    Toast.makeText(this, "Succes Add Data",
                        Toast.LENGTH_SHORT).show()
                }

                Log.d("HomeActivity", "getCurrentLocation: $longitude")

            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed on getting current location",
                    Toast.LENGTH_SHORT).show()
            }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQ_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                } else {
                    // permission denied
                    Toast.makeText(this, "You need to grant permission to access location",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
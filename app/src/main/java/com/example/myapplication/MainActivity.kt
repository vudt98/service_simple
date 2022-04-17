package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.myapplication.databinding.ActivityMainBinding




class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    var testService : ServiceTest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val service = ServiceTest::class.java
        val testIntent = Intent(applicationContext,service)

        startService(testIntent)
    }

}
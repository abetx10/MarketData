package com.example.marketdata.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.marketdata.R
import com.example.marketdata.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, MainFragment())
                .commit()
        }
    }
}
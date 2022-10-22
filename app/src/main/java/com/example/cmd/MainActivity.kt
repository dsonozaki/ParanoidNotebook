package com.example.cmd

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.cmd.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val mainBinding: ActivityMainBinding =
      DataBindingUtil.setContentView(this, R.layout.activity_main)
    setSupportActionBar(mainBinding.toolbar2)
    setContentView(mainBinding.root)
  }
}

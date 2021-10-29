package com.cxsplay.printerdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.cxsplay.printerdemo.databinding.ActivityMainBinding
import com.cxsplay.printerdemo.sedprinter.ComSedUsbPrinter

class MainActivity : AppCompatActivity() {

    private lateinit var bind: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = DataBindingUtil.setContentView(this, R.layout.activity_main)
        init()
        ComSedUsbPrinter.connectUSB()
    }

    private fun init() {
        bind.btnPrint.setOnClickListener {
            ComSedUsbPrinter.printTest()
        }
    }

    override fun onDestroy() {
        ComSedUsbPrinter.closeConnection()
        super.onDestroy()
    }
}
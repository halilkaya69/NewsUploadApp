package com.halilkaya.newsuploadapp

import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import java.util.Locale

object AppUtil {

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun getString(context: Context,resId:Int,locale:String):String{
        val config=Configuration(context.resources.configuration)
        config.setLocale(Locale(locale))
        return context.createConfigurationContext(config).resources.getString(resId)
    }


}
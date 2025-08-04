package com.halilkaya.newsuploadapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeViewModel(application: Application):AndroidViewModel(application){

    private val _isDarkMode=MutableStateFlow<Boolean?>(null)
    val isDarkMode=_isDarkMode.asStateFlow()

    fun toggleTheme(){
        _isDarkMode.value=_isDarkMode.value?.let { it }
    }

    fun setDarkMode(isDark:Boolean){
        _isDarkMode.value=isDark
    }

    fun resetToSystemDefault(){
        _isDarkMode.value=null
    }

    fun isUsingSystemDefault():Boolean{
        return _isDarkMode.value==null
    }

}
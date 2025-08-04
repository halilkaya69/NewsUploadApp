package com.halilkaya.newsuploadapp.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class LanguageViewModel(application: Application):AndroidViewModel(application) {

    private val _selectedLanguage=MutableStateFlow<String?>(null) //null:system default olan
    val selectedLanguage=_selectedLanguage.asStateFlow()

    private val sharedPreferences=application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    init {
        loadLanguagePreference()
    }

    private fun loadLanguagePreference(){
        val savedLanguage=sharedPreferences.getString("language",null)
        _selectedLanguage.value=savedLanguage
    }

    private fun getSystemLanguage():String{
        val systemLanguage= Locale.getDefault().language
        return when(systemLanguage){
            "tr" ->"tr"
            "en" -> "en"
            else -> "tr"
        }
    }

    fun setLanguage(languageCode:String){
        _selectedLanguage.value=languageCode

        sharedPreferences.edit()
            .putString("language",languageCode)
            .apply()

    }

    fun resetToSystemDefault(){
        _selectedLanguage.value=null
        sharedPreferences.edit()
            .remove("language")
            .apply()
    }

    fun getCurrentLanguageDisplayName():String{
        return when (_selectedLanguage.value){
            "en" -> "English"
            "tr" -> "Türkçe"
            null -> "System Default (${getSystemLanguage().uppercase()})"
            else -> "System Default"
        }
    }

    fun getCurrentLanguageCode():String{
        return _selectedLanguage.value ?: getSystemLanguage()
    }


}
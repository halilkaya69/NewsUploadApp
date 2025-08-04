package com.halilkaya.newsuploadapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.halilkaya.newsuploadapp.screens.LoginPage
import com.halilkaya.newsuploadapp.ui.theme.NewsUploadAppTheme
import com.halilkaya.newsuploadapp.viewmodel.ArticleViewModel
import com.halilkaya.newsuploadapp.viewmodel.AuthViewModel
import com.halilkaya.newsuploadapp.viewmodel.LanguageViewModel
import com.halilkaya.newsuploadapp.viewmodel.RoleViewModel
import com.halilkaya.newsuploadapp.viewmodel.UserViewModel
import com.halilkaya.newsuploadapp.viewmodel.ThemeViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authViewModel: AuthViewModel by viewModels()
        val userViewModel: UserViewModel by viewModels()
        val roleViewModel: RoleViewModel by viewModels()
        val themeViewModel:ThemeViewModel by viewModels()
        val languageViewModel:LanguageViewModel by viewModels()
        val articleViewModel:ArticleViewModel by viewModels()



        setContent {

            val userThemePreference by themeViewModel.isDarkMode.collectAsState()
            val systemInDarkTheme=isSystemInDarkTheme()
            val isDarkMode=userThemePreference?:systemInDarkTheme

            NewsUploadAppTheme(darkTheme=isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyAppNavigation(
                        authViewModel = authViewModel,
                        userViewModel = userViewModel,
                        roleViewModel = roleViewModel,
                        articleViewModel,
                        themeViewModel = themeViewModel,
                        languageViewModel = languageViewModel
                    )
                }
            }
        }
    }
}

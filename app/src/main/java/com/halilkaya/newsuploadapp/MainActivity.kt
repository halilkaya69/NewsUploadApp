package com.halilkaya.newsuploadapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.halilkaya.newsuploadapp.screens.LoginPage
import com.halilkaya.newsuploadapp.ui.theme.NewsUploadAppTheme
import com.halilkaya.newsuploadapp.viewmodel.AuthViewModel
import com.halilkaya.newsuploadapp.viewmodel.RoleViewModel
import com.halilkaya.newsuploadapp.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authViewModel: AuthViewModel by viewModels()
        val userViewModel: UserViewModel by viewModels()
        val roleViewModel: RoleViewModel by viewModels()

        setContent {
            NewsUploadAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyAppNavigation(
                        authViewModel = authViewModel,
                        userViewModel = userViewModel,
                        roleViewModel = roleViewModel
                    )
                }
            }
        }
    }
}

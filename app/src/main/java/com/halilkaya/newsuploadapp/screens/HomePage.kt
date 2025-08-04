package com.halilkaya.newsuploadapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.halilkaya.newsuploadapp.AppUtil
import com.halilkaya.newsuploadapp.R
import com.halilkaya.newsuploadapp.Routes
import com.halilkaya.newsuploadapp.components.DrawerLayout
import com.halilkaya.newsuploadapp.viewmodel.AuthState
import com.halilkaya.newsuploadapp.viewmodel.AuthViewModel
import com.halilkaya.newsuploadapp.viewmodel.LanguageViewModel
import com.halilkaya.newsuploadapp.viewmodel.UserViewModel

@Composable
fun HomePage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel,userViewModel: UserViewModel,languageViewModel: LanguageViewModel) {
    val authState = authViewModel.authState.observeAsState()
    val currentUserId = authViewModel.currentUserId.observeAsState()

    val context= LocalContext.current
    val currentLocale = languageViewModel.getCurrentLanguageCode()

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Unauthenticated -> navController.navigate(Routes.login)
            else -> Unit
        }
    }

    DrawerLayout(
        navController = navController,
        authViewModel = authViewModel,
        userViewModel = userViewModel,
        languageViewModel=languageViewModel,
        title = AppUtil.getString(context, R.string.home_title,currentLocale)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = AppUtil.getString(context, R.string.welcome,currentLocale),
                fontSize = 28.sp,
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = AppUtil.getString(context, R.string.user_id,currentLocale)+" ${currentUserId.value}",
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodyLarge
            )




        }
    }
}
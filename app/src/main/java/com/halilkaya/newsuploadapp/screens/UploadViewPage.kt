package com.halilkaya.newsuploadapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.halilkaya.newsuploadapp.AppUtil
import com.halilkaya.newsuploadapp.R
import com.halilkaya.newsuploadapp.Routes
import com.halilkaya.newsuploadapp.UserRole
import com.halilkaya.newsuploadapp.components.DrawerLayout
import com.halilkaya.newsuploadapp.viewmodel.ArticleViewModel
import com.halilkaya.newsuploadapp.viewmodel.AuthState
import com.halilkaya.newsuploadapp.viewmodel.AuthViewModel
import com.halilkaya.newsuploadapp.viewmodel.LanguageViewModel
import com.halilkaya.newsuploadapp.viewmodel.RoleViewModel
import com.halilkaya.newsuploadapp.viewmodel.UserViewModel

@Composable
fun UploadViewPage(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    roleViewModel: RoleViewModel,
    articleViewModel: ArticleViewModel,
    languageViewModel: LanguageViewModel
) {


    val user by userViewModel.user.collectAsState()
    val userRole = user.role

    val authState = authViewModel.authState.observeAsState()
    val context= LocalContext.current
    val currentLocale = languageViewModel.getCurrentLanguageCode()

    // ProfilePage her açıldığında fresh user data çek
    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Authenticated) {
            userViewModel.getUserProfile()
        }
    }


    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Unauthenticated -> navController.navigate(Routes.login)
            else -> Unit
        }
    }


    DrawerLayout(
        navController = navController,
        authViewModel = authViewModel,
        userViewModel = userViewModel,languageViewModel,
        title = AppUtil.getString(context, R.string.upload_requests,currentLocale)
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (userRole) {
                UserRole.USER,  UserRole.WRITER  -> {
                    // Sıradan kullanıcı - İzin yok
                    NoPermissionView(languageViewModel)
                }

                UserRole.EDITOR, UserRole.ADMIN -> {

                    // Moderator/Admin - Onay bekleyen makaleler
                    PendingArticlesView(articleViewModel = articleViewModel,languageViewModel)
                }
            }

        }
    }
}
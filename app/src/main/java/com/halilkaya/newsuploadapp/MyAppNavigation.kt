package com.halilkaya.newsuploadapp


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.navigation.compose.rememberNavController
import com.halilkaya.newsuploadapp.screens.HomePage
import com.halilkaya.newsuploadapp.screens.LoginPage
import com.halilkaya.newsuploadapp.screens.ProfilePage
import com.halilkaya.newsuploadapp.screens.RolePage
import com.halilkaya.newsuploadapp.screens.SettingsPage
import com.halilkaya.newsuploadapp.screens.SignupPage
import com.halilkaya.newsuploadapp.screens.UploadPage
import com.halilkaya.newsuploadapp.screens.UploadViewPage
import com.halilkaya.newsuploadapp.viewmodel.ArticleViewModel

import com.halilkaya.newsuploadapp.viewmodel.AuthViewModel
import com.halilkaya.newsuploadapp.viewmodel.LanguageViewModel
import com.halilkaya.newsuploadapp.viewmodel.RoleViewModel
import com.halilkaya.newsuploadapp.viewmodel.ThemeViewModel
import com.halilkaya.newsuploadapp.viewmodel.UserViewModel
import java.util.Locale

@Composable
fun MyAppNavigation(authViewModel: AuthViewModel, userViewModel: UserViewModel,roleViewModel: RoleViewModel,articleViewModel:ArticleViewModel,themeViewModel: ThemeViewModel,languageViewModel: LanguageViewModel) {

    val navController = rememberNavController()

    var currentLocale by remember {
        mutableStateOf(Locale.getDefault().language)
    }

    NavHost(navController = navController, startDestination = Routes.login) {

        composable(Routes.login) {
            LoginPage(Modifier, navController, authViewModel,languageViewModel)
        }
        composable(Routes.home) {
            HomePage(Modifier, navController, authViewModel,userViewModel,languageViewModel)
        }
        composable(Routes.signup) {
            SignupPage(Modifier, navController, authViewModel,languageViewModel)
        }
        composable(Routes.profile) {
            ProfilePage(Modifier, navController, authViewModel, userViewModel,languageViewModel)
        }
        composable(Routes.role) {
            RolePage(Modifier, navController, authViewModel, userViewModel, roleViewModel ,languageViewModel)
        }
        composable(Routes.upload) {
            UploadPage(Modifier, navController, authViewModel, userViewModel, roleViewModel , articleViewModel ,languageViewModel )
        }

        composable(Routes.uploadview) {
            UploadViewPage(Modifier, navController, authViewModel, userViewModel, roleViewModel , articleViewModel ,languageViewModel )
        }
        composable(Routes.settings) {
            SettingsPage(Modifier, navController, authViewModel, userViewModel,themeViewModel,languageViewModel )
        }


    }
}
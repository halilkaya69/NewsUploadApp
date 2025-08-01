package com.halilkaya.newsuploadapp


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

import androidx.navigation.compose.rememberNavController
import com.halilkaya.newsuploadapp.screens.HomePage
import com.halilkaya.newsuploadapp.screens.LoginPage
import com.halilkaya.newsuploadapp.screens.ProfilePage
import com.halilkaya.newsuploadapp.screens.RolePage
import com.halilkaya.newsuploadapp.screens.SignupPage

import com.halilkaya.newsuploadapp.viewmodel.AuthViewModel
import com.halilkaya.newsuploadapp.viewmodel.RoleViewModel
import com.halilkaya.newsuploadapp.viewmodel.UserViewModel

@Composable
fun MyAppNavigation(authViewModel: AuthViewModel, userViewModel: UserViewModel,roleViewModel: RoleViewModel) {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.login) {

        composable(Routes.login) {
            LoginPage(Modifier, navController, authViewModel)
        }
        composable(Routes.home) {
            HomePage(Modifier, navController, authViewModel,userViewModel)
        }
        composable(Routes.signup) {
            SignupPage(Modifier, navController, authViewModel)
        }
        composable(Routes.profile) {
            ProfilePage(Modifier, navController, authViewModel, userViewModel)
        }
        composable(Routes.role) {
            RolePage(Modifier, navController, authViewModel, userViewModel, roleViewModel )
        }

    }
}
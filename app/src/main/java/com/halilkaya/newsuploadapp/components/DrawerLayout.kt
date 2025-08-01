package com.halilkaya.newsuploadapp.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.halilkaya.newsuploadapp.viewmodel.AuthViewModel
import com.halilkaya.newsuploadapp.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerLayout(
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    title: String,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawer(
                navController = navController,
                authViewModel = authViewModel,
                userViewModel=userViewModel,
                        onCloseDrawer = {
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            content(paddingValues)
        }
    }
}

package com.halilkaya.newsuploadapp.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.halilkaya.newsuploadapp.AppUtil
import com.halilkaya.newsuploadapp.R
import com.halilkaya.newsuploadapp.Routes
import com.halilkaya.newsuploadapp.UserRole
import com.halilkaya.newsuploadapp.screens.ArticleUploadView
import com.halilkaya.newsuploadapp.screens.NoPermissionView
import com.halilkaya.newsuploadapp.screens.PendingArticlesView
import com.halilkaya.newsuploadapp.viewmodel.AuthViewModel
import com.halilkaya.newsuploadapp.viewmodel.LanguageViewModel
import com.halilkaya.newsuploadapp.viewmodel.UserViewModel

@Composable
fun NavigationDrawer(
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    languageViewModel: LanguageViewModel,
    onCloseDrawer: () -> Unit
) {
    val context = LocalContext.current
    val currentLocale = languageViewModel.getCurrentLanguageCode()

    val user by userViewModel.user.collectAsState()
    val userRole = user.role

    // Drawer açıldığında user profilini güncelle
    LaunchedEffect(Unit) {
        userViewModel.refreshUserProfile()
    }

    ModalDrawerSheet(
        modifier = Modifier.width(280.dp)
    ) {
        // Drawer Header
        DrawerHeader(userViewModel = userViewModel, context = context, currentLocale = currentLocale)
        
        HorizontalDivider()
        
        // Navigation Items
        DrawerNavigationItem(
            icon = Icons.Default.Home,
            label = AppUtil.getString(context, R.string.home, currentLocale),
            onClick = {
                navController.navigate(Routes.home)
                onCloseDrawer()
            }
        )
        
        DrawerNavigationItem(
            icon = Icons.Default.Person,
            label = "Profil",
            onClick = {
                navController.navigate(Routes.profile)
                onCloseDrawer()
            }
        )

        DrawerNavigationItem(
            icon = Icons.Default.Info,
            label = when (userRole) {
                UserRole.USER,UserRole.WRITER,UserRole.EDITOR -> {
                    AppUtil.getString(context, R.string.role, currentLocale)
                }
            UserRole.ADMIN ->{
                AppUtil.getString(context, R.string.role_requests, currentLocale)
            }
            },

            onClick = {
                navController.navigate(Routes.role)
                onCloseDrawer()
            }
        )



        when (userRole) {
            UserRole.USER -> {
                    //hiçbişi
            }
            UserRole.WRITER,-> { //sadece yazı ekleme
                DrawerNavigationItem(
                    icon = Icons.Default.Send,
                    label = AppUtil.getString(context, R.string.upload, currentLocale),
                    onClick = {
                        navController.navigate(Routes.upload)
                        onCloseDrawer()
                    }
                )
            }
            UserRole.EDITOR, UserRole.ADMIN -> { //hem yazı ekleme hem onaylama

                DrawerNavigationItem(
                    icon = Icons.Default.Send,
                    label = AppUtil.getString(context, R.string.upload, currentLocale),
                    onClick = {
                        navController.navigate(Routes.upload)
                        onCloseDrawer()
                    }
                )
                DrawerNavigationItem(
                    icon = Icons.Default.Checklist,
                    label =  AppUtil.getString(context, R.string.upload_requests, currentLocale),
                    onClick = {
                        navController.navigate(Routes.uploadview)
                        onCloseDrawer()
                    }
                )
            }
        }

        DrawerNavigationItem(
            icon = Icons.Default.Settings,
            label = "Settings",
            onClick = {
                navController.navigate(Routes.settings)
                onCloseDrawer()
            }
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        HorizontalDivider()
        
        // Sign Out
        DrawerNavigationItem(
            icon = Icons.Default.ExitToApp,
            label = AppUtil.getString(context, R.string.sign_out, currentLocale),
            onClick = {
                authViewModel.signout(context)
                onCloseDrawer()
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DrawerHeader(userViewModel:UserViewModel, context: android.content.Context, currentLocale: String) {
    val userProfile by userViewModel.user.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // User Avatar Placeholder
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = AppUtil.getString(context, R.string.user_avatar, currentLocale),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = AppUtil.getString(context, R.string.news_upload_app, currentLocale),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = userProfile.name,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DrawerNavigationItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label
            )
        },
        label = {
            Text(text = label)
        },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}

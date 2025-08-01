package com.halilkaya.newsuploadapp.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.halilkaya.newsuploadapp.Routes
import com.halilkaya.newsuploadapp.viewmodel.AuthViewModel
import com.halilkaya.newsuploadapp.viewmodel.UserViewModel

@Composable
fun NavigationDrawer(
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    onCloseDrawer: () -> Unit
) {
    val context = LocalContext.current
    
    ModalDrawerSheet(
        modifier = Modifier.width(280.dp)
    ) {
        // Drawer Header
        DrawerHeader(userViewModel = userViewModel)
        
        HorizontalDivider()
        
        // Navigation Items
        DrawerNavigationItem(
            icon = Icons.Default.Home,
            label = "Ana Sayfa",
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
            icon = Icons.Default.Person,
            label = "Role",
            onClick = {
                navController.navigate(Routes.role)
                onCloseDrawer()
            }
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        HorizontalDivider()
        
        // Sign Out
        DrawerNavigationItem(
            icon = Icons.Default.ExitToApp,
            label = "Çıkış Yap",
            onClick = {
                authViewModel.signout(context)
                onCloseDrawer()
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DrawerHeader(userViewModel:UserViewModel) {
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
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "News Upload App",
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

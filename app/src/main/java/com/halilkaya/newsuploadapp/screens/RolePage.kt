package com.halilkaya.newsuploadapp.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.halilkaya.newsuploadapp.AppUtil
import com.halilkaya.newsuploadapp.Routes
import com.halilkaya.newsuploadapp.UserRole
import com.halilkaya.newsuploadapp.components.DrawerLayout
import com.halilkaya.newsuploadapp.model.RoleModel
import com.halilkaya.newsuploadapp.viewmodel.AuthState
import com.halilkaya.newsuploadapp.viewmodel.AuthViewModel
import com.halilkaya.newsuploadapp.viewmodel.RoleViewModel
import com.halilkaya.newsuploadapp.viewmodel.UserViewModel

@Composable
fun RolePage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, userViewModel: UserViewModel,roleViewModel: RoleViewModel) {

    val context= LocalContext.current

    val userProfile by userViewModel.user.collectAsStateWithLifecycle()

    val authState = authViewModel.authState.observeAsState()
    val currentUserId = authViewModel.currentUserId.observeAsState()


    var roleInput by remember { mutableStateOf(UserRole.USER.name) }


    // ProfilePage her açıldığında fresh user data çek
    LaunchedEffect(Unit) {
        userViewModel.getUserProfile()
    }

    // userProfile değiştiğinde role isteklerini çek
    LaunchedEffect(userProfile.role, userProfile.email) {
        if(userProfile.email.isNotEmpty()) { // Profile yüklendiyse
            roleInput = userProfile.role.name
            if(userProfile.role == UserRole.ADMIN){
                roleViewModel.getAllRoleRequests()
            } else {
                roleViewModel.getRoleRequest()
            }
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
        userViewModel = userViewModel,
        title = "Rol Yönetimi"
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            if(userProfile.role==UserRole.ADMIN){
                Text(
                    text = "Kullanıcı ID: ${currentUserId.value}",
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Hoş Geldiniz admin!",
                    fontSize = 28.sp,
                    style = MaterialTheme.typography.headlineMedium
                )






                val roleRequests by roleViewModel.allRoleRequests.collectAsState()

                Column {
                    Text(
                        text = "Tüm Rol İstekleri",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if(roleRequests.isEmpty()) {
                        Text("Hiç rol isteği yok.")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(roleRequests) { request ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outline,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(16.dp)
                                ) {
                                    // İstek bilgileri
                                    Text(
                                        text = "İsim: ${request.name}",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Text(
                                        text = "E-posta: ${request.email}",
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Text(
                                        text = "İstenen Rol: ${request.role.name}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Butonlar alt alta
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        TextButton(
                                            onClick = {
                                                // Onayla
                                                roleViewModel.updateUserRole(
                                                    uid = request.email,
                                                    newRole = request.role,
                                                    userViewModel = userViewModel,
                                                    onSuccess = {
                                                        AppUtil.showToast(context, "Rol başarıyla güncellendi.")
                                                        roleViewModel.getAllRoleRequests() // Listeyi yenile
                                                    },
                                                    onFailure = { error ->
                                                        AppUtil.showToast(context, "Güncelleme başarısız: $error")
                                                    }
                                                )
                                            },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = Color.Green
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("✓ Kabul Et", fontWeight = FontWeight.Bold)
                                        }

                                        TextButton(
                                            onClick = {
                                                // Reddet
                                                roleViewModel.rejectRoleRequest(
                                                    uid = request.email,
                                                    onSuccess = {
                                                        AppUtil.showToast(context, "Rol isteği reddedildi.")
                                                        roleViewModel.getAllRoleRequests() // Listeyi yenile
                                                    },
                                                    onFailure = { error ->
                                                        AppUtil.showToast(context, "Reddetme başarısız: $error")
                                                    }
                                                )
                                            },
                                            colors = ButtonDefaults.textButtonColors(
                                                contentColor = Color.Red
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("✗ Reddet", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                }else{
                    val currentRequest = roleViewModel.currentRoleRequest.collectAsState().value
                    if(currentRequest == null) {
                        // Rol isteği gönderebilir
                        Text(
                            text = "Rol Değişikliği İsteği",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Text(
                            text = "Mevcut Rolünüz: ${userProfile.role.name}",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        val roles = UserRole.values().toList()
                        var selectedRole by remember { mutableStateOf(UserRole.USER) }

                        Column {
                            Text(
                                text = "İstediğiniz Rolü Seçin:",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            roles.forEach { role ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedRole = role }
                                        .padding(vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedRole == role,
                                        onClick = { selectedRole = role }
                                    )
                                    Text(
                                        text = role.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            TextButton(
                                onClick = {
                                    if (selectedRole != userProfile.role) {
                                        roleViewModel.createRoleRequest(
                                            name = userProfile.name,
                                            role = selectedRole, // selectedRole kullanılmalı, userProfile.role değil
                                            onSuccess = {
                                                AppUtil.showToast(context, "Rol isteği başarıyla gönderildi!")
                                                roleViewModel.getRoleRequest() // Güncel isteği çek
                                            },
                                            onFailure = { error ->
                                                AppUtil.showToast(context, "Rol isteği gönderilemedi: $error")
                                            }
                                        )
                                    } else {
                                        AppUtil.showToast(context, "Lütfen mevcut rolünüzden farklı bir rol seçin")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Rol İsteği Gönder", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Zaten bekleyen bir istek var
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Bekleyen Rol İsteğiniz",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            Text(
                                text = "Mevcut Rolünüz: ${userProfile.role.name}",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Text(
                                text = "İstenen Rol: ${currentRequest.role.name}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            Text(
                                text = "Rol isteğiniz admin onayı bekliyor...",
                                fontSize = 14.sp,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }




        }
    }






}
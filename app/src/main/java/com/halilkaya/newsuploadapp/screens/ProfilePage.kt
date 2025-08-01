package com.halilkaya.newsuploadapp.screens
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.halilkaya.newsuploadapp.AppUtil
import com.halilkaya.newsuploadapp.Routes
import com.halilkaya.newsuploadapp.UserRole
import com.halilkaya.newsuploadapp.components.DrawerLayout
import com.halilkaya.newsuploadapp.viewmodel.AuthState
import com.halilkaya.newsuploadapp.viewmodel.AuthViewModel
import com.halilkaya.newsuploadapp.viewmodel.UserViewModel

@Composable
fun ProfilePage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, userViewModel: UserViewModel) {

    val userProfile by userViewModel.user.collectAsStateWithLifecycle()
    val authState = authViewModel.authState.observeAsState()

    var nameInput by remember { mutableStateOf("") }
    var cityInput by remember { mutableStateOf("") }
    var phoneNumberInput by remember { mutableStateOf("") }

    val context = LocalContext.current

    // ProfilePage her açıldığında fresh user data çek
    LaunchedEffect(Unit) {
        userViewModel.getUserProfile()
    }

    //userProfile değiştiğinde input'ları güncelle
    LaunchedEffect(userProfile) {
        cityInput = userProfile.city
        nameInput = userProfile.name
        phoneNumberInput = userProfile.phoneNumber
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
        userViewModel=userViewModel,
        title = "Profil"
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Profiliniz",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = userProfile.name,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = nameInput,
                onValueChange = { nameInput = it },
                label = {
                    Text(
                        text = "İsim:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                placeholder = { Text(text = "İsminizi girin") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Email: " + userProfile.email,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Rol: " + userProfile.role,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = cityInput,
                onValueChange = { cityInput = it },
                label = {
                    Text(
                        text = "Şehir:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                placeholder = { Text(text = "Şehrinizi girin") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(12.dp))
            
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = phoneNumberInput,
                onValueChange = { input ->
                    phoneNumberInput = input.filter { it.isDigit() }
                },
                label = { Text("Telefon Numarası") },
                placeholder = { Text(text = "Telefon numaranızı girin") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(
                onClick = {
                    if(nameInput.isNotEmpty() && cityInput.isNotEmpty()) {
                        userViewModel.saveCompleteProfile(
                            name = nameInput,
                            city = cityInput,
                            role = userProfile.role,
                            phoneNumber = phoneNumberInput,
                            onSuccess = {
                                AppUtil.showToast(context, "Profil başarıyla kaydedildi!")
                            },
                            onFailure = { error ->
                                AppUtil.showToast(context, "Profil kaydedilemedi: $error")
                            }
                        )
                    } else {
                        AppUtil.showToast(context, "Lütfen tüm alanları doldurun")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Profili Kaydet", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

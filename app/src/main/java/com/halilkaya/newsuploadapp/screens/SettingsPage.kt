package com.halilkaya.newsuploadapp.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.halilkaya.newsuploadapp.viewmodel.ThemeViewModel
import com.halilkaya.newsuploadapp.viewmodel.UserViewModel


@Composable
fun SettingsPage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, userViewModel: UserViewModel,themeViewModel: ThemeViewModel,languageViewModel: LanguageViewModel) {
    val authState = authViewModel.authState.observeAsState()



    val context = LocalContext.current
    val userThemePreference by themeViewModel.isDarkMode.collectAsState()
    val systemInDarkTheme = isSystemInDarkTheme()
    val isDarkMode = userThemePreference ?: systemInDarkTheme
    val isUsingSystemDefault = userThemePreference == null
    val userLanguagePreference by languageViewModel.selectedLanguage.collectAsState()
    val isUsingSystemLanguage=userLanguagePreference==null
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
        userViewModel = userViewModel,languageViewModel,
        title = "Settings"
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = AppUtil.getString(context, R.string.settings,currentLocale),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            //theme start

            Text(
                text = AppUtil.getString(context, R.string.theme_settings,currentLocale),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom=16.dp)
            )

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isUsingSystemDefault,
                        onClick = {themeViewModel.resetToSystemDefault()}
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically

            ) {
                RadioButton(
                    selected = isUsingSystemDefault,
                    onClick = { themeViewModel.resetToSystemDefault() }
                )

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "System default (${if (systemInDarkTheme) "Dark" else "Light"})",
                    fontSize = 16.sp
                )
            }

                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = userThemePreference==false,
                            onClick = {themeViewModel.setDarkMode(false)}
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    RadioButton(
                        selected = userThemePreference==false,
                        onClick = {themeViewModel.setDarkMode(false)}
                    )
                    Spacer(modifier=Modifier.width(8.dp))
                    Text(
                        text =AppUtil.getString(context, R.string.light_theme,currentLocale),
                        fontSize=16.sp
                    )

                }

                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = userThemePreference==true,
                            onClick = {themeViewModel.setDarkMode(true)}
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    RadioButton(
                        selected = userThemePreference==true,
                        onClick = {themeViewModel.setDarkMode(true)}
                    )
                    Spacer(modifier=Modifier.width(8.dp))
                    Text(
                        text = AppUtil.getString(context, R.string.dark_theme,currentLocale),
                        fontSize = 16.sp
                    )

                }

                Spacer(modifier=Modifier.height(24.dp))







            //theme end
            Text(
                text = "Dil seçimi / Language",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier=Modifier.padding(bottom = 16.dp)

            )

            Row (
                modifier=Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isUsingSystemLanguage,
                        onClick = {languageViewModel.resetToSystemDefault()}
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                RadioButton(
                    selected = isUsingSystemLanguage,
                    onClick = {languageViewModel.resetToSystemDefault()}
                )
                Spacer(modifier=Modifier.width(8.dp))
                Text(
                    text = "System Default/ Sistem Varsayılanı",
                    fontSize=16.sp
                )

            }


            Row (
                modifier=Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = userLanguagePreference=="tr",
                        onClick = {languageViewModel.setLanguage("tr")}
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ){

                RadioButton(
                    selected = userLanguagePreference=="tr",
                    onClick = {languageViewModel.setLanguage("tr")}
                )
                Spacer(modifier=Modifier.width(8.dp))
                Text(
                    text = "Türkçe",
                    fontSize = 16.sp
                )

            }


            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = userLanguagePreference=="en",
                        onClick = {languageViewModel.setLanguage("en")}
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                RadioButton(
                    selected = userLanguagePreference=="en",
                    onClick = {languageViewModel.setLanguage("en")}
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "English",
                    fontSize = 16.sp
                )

            }




        }
    }
}
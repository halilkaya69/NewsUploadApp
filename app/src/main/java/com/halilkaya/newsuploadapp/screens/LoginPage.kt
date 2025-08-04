package com.halilkaya.newsuploadapp.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.firebase.auth.GoogleAuthProvider
import com.halilkaya.newsuploadapp.AppUtil
import com.halilkaya.newsuploadapp.R
import com.halilkaya.newsuploadapp.Routes
import com.halilkaya.newsuploadapp.viewmodel.AuthState
import com.halilkaya.newsuploadapp.viewmodel.AuthViewModel
import com.halilkaya.newsuploadapp.viewmodel.LanguageViewModel

@Composable
fun LoginPage(modifier: Modifier = Modifier,navController: NavController,authViewModel: AuthViewModel,languageViewModel: LanguageViewModel) {

    var email by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var forgotPasswordDialogBox by remember {
        mutableStateOf(false)
    }

    val authState=authViewModel.authState.observeAsState()
    val context= LocalContext.current
    val currentLocale = languageViewModel.getCurrentLanguageCode()

    //
    val googleSignInOptions= remember { //google ile giris icin gerekli ayarları yapar
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("293746123866-4f7nrt7eqhohedrf92tulgllpj2vdkq7.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }

    val googleSignInClient= remember { //GoogleSignInClient nesnesi olusturur
        GoogleSignIn.getClient(context,googleSignInOptions)
    }

    val launcher= rememberLauncherForActivityResult( // Google Sign-In işlemini başlatmak ve sonucunu almak için launcher tanımlanıyor
        contract = ActivityResultContracts.StartActivityForResult()
    ){ result->
        val task=GoogleSignIn.getSignedInAccountFromIntent(result.data) //Google Sign-In ekranından dönen veriyi task olarak alıyoruz
        try {
            val account = task.result //Eğer giriş başarılıysa, Google hesabı bilgilerini alıyoruz
            val credential = GoogleAuthProvider.getCredential(account.idToken, null) //Firebase'e giriş yapabilmek için kimlik bilgisi (credential) oluşturuluyor
            authViewModel.signInWithGoogle(credential) //Oluşturulan kimlik bilgisi ile Firebase'e giriş yapılması için ViewModel çağrılıyor
        } catch (e: Exception) {
            Toast.makeText(context,AppUtil.getString(context, R.string.google_signin_failed,currentLocale)+ "${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    //

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Authenticated->navController.navigate(Routes.home)
            is AuthState.Error-> Toast.makeText(context,
                (authState.value as AuthState.Error).message, Toast.LENGTH_LONG).show()
            else -> Unit
        }
    }



    Column (
        modifier=Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){


        Image(painter = painterResource(id= R.drawable.elma),
            contentDescription = "logo image",
            modifier = Modifier.size(200.dp)
        )


        Text(text =  AppUtil.getString(context, R.string.login_page,currentLocale), fontSize = 32.sp, fontWeight = FontWeight.Bold)


        Spacer(modifier=Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email=it
            },
            label = {
                Text(text = AppUtil.getString(context, R.string.email,currentLocale))
            }
        )

        Spacer(modifier=Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password=it
            },
            label = {
                Text(text = AppUtil.getString(context, R.string.password,currentLocale))
            }
        )
        Spacer(modifier=Modifier.height(16.dp))

        Button(onClick = {
            authViewModel.login(email, password)
        },
            enabled = authState.value!=AuthState.Loading
        ) {
            Text(text = AppUtil.getString(context, R.string.login,currentLocale))

        }
        Spacer(modifier=Modifier.height(8.dp))

        TextButton(onClick = {
            navController.navigate(Routes.signup)
        }) {
            Text(text = AppUtil.getString(context, R.string.dont_have_account,currentLocale))

        }


        Row (
        ){
            TextButton(
                onClick = {
                    forgotPasswordDialogBox=true
                },
            ) {
                Text(AppUtil.getString(context, R.string.forgot_password,currentLocale))
            }
        }


        //google sign in
        Row (
            modifier=Modifier.fillMaxWidth().padding(40.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            /*AndroidView(modifier = Modifier.fillMaxWidth(0.8f).height(48.dp),
                factory = {context->
                    SignInButton(context).apply {
                        setSize(SignInButton.SIZE_WIDE)
                        setOnClickListener{

                            //
                            val signInIntent=googleSignInClient.signInIntent
                            launcher.launch(signInIntent)



                        }
                    }

                }
            )*/

            /*Image(painter = painterResource(id=R.drawable.facebook),
                contentDescription = "facebook login",

                modifier = Modifier.size(60.dp).clickable {
                    //facebook login
                    Toast.makeText(context,"Facebook login not implemented yer",Toast.LENGTH_LONG).show()
                }

            )*/

            Image(painter = painterResource(id=R.drawable.google),
                contentDescription = "google login",

                modifier = Modifier.size(60.dp).clickable {
                    //google login
                    //Toast.makeText(context,"google login not implemented yer",Toast.LENGTH_LONG).show()
                    val signInIntent=googleSignInClient.signInIntent
                    launcher.launch(signInIntent)

                }

            )


            /*Image(painter = painterResource(id=R.drawable.x),
                contentDescription = "x login",

                modifier = Modifier.size(60.dp).clickable {
                    //x login
                    Toast.makeText(context,"x login not implemented yer",Toast.LENGTH_LONG).show()
                }

            )*/

        }


        //reset password
        if(forgotPasswordDialogBox){
            var resetEmail by remember { mutableStateOf("") }

            AlertDialog(
                title = { Text("Forgot Password")},
                text={ OutlinedTextField(
                    value = resetEmail,
                    onValueChange = {resetEmail=it},
                    label = { Text("Email")},
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )},
                confirmButton = {
                    TextButton(
                        onClick = {
                            authViewModel.resetPassword(resetEmail) {
                                // Success callback
                                Toast.makeText(context, AppUtil.getString(context, R.string.check_email_reset,currentLocale), Toast.LENGTH_LONG).show()
                            }
                            forgotPasswordDialogBox = false
                        },
                        enabled = authState.value != AuthState.Loading
                    ){
                        Text(AppUtil.getString(context, R.string.submit,currentLocale))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {forgotPasswordDialogBox=false}) { Text(AppUtil.getString(context, R.string.cancel,currentLocale)) }
                },
                onDismissRequest = {
                    forgotPasswordDialogBox=false
                }
            )
        }




    }



}
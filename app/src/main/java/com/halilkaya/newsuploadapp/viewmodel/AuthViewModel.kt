package com.halilkaya.newsuploadapp.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel:ViewModel() {

    private val auth:FirebaseAuth=FirebaseAuth.getInstance()

    private val _authState=MutableLiveData<AuthState>()
    val authState:LiveData<AuthState> = _authState

    private val _currentUserId=MutableLiveData<String?>() //iceriden hem eris hem degistir
    val currentUserId:LiveData<String?> = _currentUserId //dısaridan sadece eris ama degistiremez

    init {
        checkAuthStatus()
    }

    fun checkAuthStatus(){
        if(auth.currentUser==null){
            _authState.value=AuthState.Unauthenticated
            _currentUserId.value=null
        }else{
            _authState.value=AuthState.Authenticated
            _currentUserId.value=auth.currentUser?.uid
        }
    }

    fun login(email:String,password:String){

        if(email.isEmpty() || password.isEmpty()){
            _authState.value=AuthState.Error("Email or password cant be empty")
            return
        }

        _authState.value=AuthState.Loading
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener{task->
                if(task.isSuccessful){
                    _authState.value=AuthState.Authenticated
                    _currentUserId.value=auth.currentUser?.uid
                }else{
                    _authState.value=AuthState.Error(task.exception?.message?:"Something went wrong")
                    _currentUserId.value=null
                }

            }
    }





    fun signup(email:String,password:String){

        if(email.isEmpty() || password.isEmpty()){
            _authState.value=AuthState.Error("Email or password cant be empty")
            return
        }

        _authState.value=AuthState.Loading
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener{task->
                if(task.isSuccessful){
                    _authState.value=AuthState.Authenticated
                    _currentUserId.value=auth.currentUser?.uid
                }else{
                    _authState.value=AuthState.Error(task.exception?.message?:"Something went wrong")
                    _currentUserId.value=null
                }

            }
    }

    fun signInWithGoogle(credential: AuthCredential) {
        _authState.value = AuthState.Loading
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                    _currentUserId.value = auth.currentUser?.uid
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Google Sign-In Failed")
                    _currentUserId.value = null
                }
            }
    }



    fun signout(context:Context){
        auth.signOut()
        _authState.value=AuthState.Unauthenticated
        _currentUserId.value=null



        // 2. Google hesabından da çıkış yap

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("794588990427-gqbcveok0f1hef39ucfh6deeplr0q03g.apps.googleusercontent.com")
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)

        googleSignInClient.signOut()


    }


    fun resetPassword(resetEmail: String, onSuccess: () -> Unit) {
        if (resetEmail.isBlank()) {
            _authState.value = AuthState.Error("Please enter your registered email")
            return
        }

        _authState.value = AuthState.Loading
        auth.sendPasswordResetEmail(resetEmail)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Unauthenticated // Reset edilince auth state'i sıfırla
                    onSuccess() // Success callback'i çağır
                } else {
                    _authState.value = AuthState.Error("Registered email not found")
                }
            }
    }

}


sealed class AuthState{
    //data class Authenticated(val uid: String) : AuthState()
    object Authenticated:AuthState()
    object Unauthenticated:AuthState()
    object Loading:AuthState()
    data class Error(val message:String):AuthState()
}
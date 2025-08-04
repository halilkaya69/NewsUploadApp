package com.halilkaya.newsuploadapp.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.halilkaya.newsuploadapp.UserRole
import com.halilkaya.newsuploadapp.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel : ViewModel() {

    private val _user = MutableStateFlow(UserModel())
    val user = _user.asStateFlow()

    val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    fun getUserProfile(){
        val currentUserUid=auth.currentUser?.uid?:return

        // Loading state ekle
        _user.value = _user.value.copy() // Mevcut değeri koru

        db.collection("users")
            .document(currentUserUid)
            .get()
            .addOnSuccessListener { snapshot ->
                if(snapshot.exists()){
                    //varsa al kaydey
                    val userModel=snapshot.toObject(UserModel::class.java)
                    userModel?.let {
                        _user.value=it
                    }
                }else{
                    //yoksa olustur sonra kaydet
                    val currentUserEmail=auth.currentUser?.email?:""
                    val newUserProfile=UserModel(
                        name = "",
                        email = currentUserEmail,
                        city = "",
                        role = UserRole.USER,
                        phoneNumber = ""
                    )

                    db.collection("users")
                        .document(currentUserUid)
                        .set(newUserProfile)
                        .addOnSuccessListener {
                            _user.value=newUserProfile
                        }
                        .addOnFailureListener{
                            _user.value=newUserProfile
                        }

                }

            }
            .addOnFailureListener {
                //dbye gidemezse
                val currentUserEmail=auth.currentUser?.email?:""
                _user.value= UserModel("",currentUserEmail, "" ,UserRole.USER,"")
            }


    }

    // Kullanıcı profilini yenile
    fun refreshUserProfile() {
        getUserProfile()
    }



    fun saveCompleteProfile(
        name:String,
        city:String,
        phoneNumber: String,
        role: UserRole,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ){
        val currentUserUid = auth.currentUser?.uid ?: return
        val currentUserEmail = auth.currentUser?.email ?: ""



        val updatedProfile = UserModel(
            name = name,
            email = currentUserEmail,
            city = city,
            role=role,
            phoneNumber=phoneNumber,


        )

        db.collection("users")
            .document(currentUserUid)
            .set(updatedProfile)
            .addOnSuccessListener {
                _user.value=updatedProfile
                onSuccess()
            }
            .addOnFailureListener {  exception ->
                onFailure(exception.message?:"Save failed")

            }


    }


    fun updateUserRole(uid:String,newRole: UserRole) {

        val roleUpdate = mapOf("role" to newRole)

        db.collection("users")
            .document(uid)
            .update(roleUpdate)
            .addOnSuccessListener {
               // Eğer güncellenene kullanıcı şu anki kullanıcıysa, local state'i de güncelle
               if (uid == auth.currentUser?.uid) {
                   _user.value = _user.value.copy(role = newRole)
               }
            }
            .addOnFailureListener { exception ->
                // Hata durumunda loglama yapabilirsin
                //Log.e("updateUserRole", "Role güncellenemedi: ${exception.message}")
            }
    }
    
    fun updateUserRoleByEmail(email: String, newRole: UserRole, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        // Email ile kullanıcıyı bul ve UID'sini al
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val uid = document.id
                    
                    // Rolü güncelle
                    updateUserRole(uid, newRole)
                    onSuccess()
                } else {
                    onFailure("User not found with email: $email")
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception.message ?: "Failed to find user")
            }
    }


}
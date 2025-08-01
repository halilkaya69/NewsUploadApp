package com.halilkaya.newsuploadapp.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.halilkaya.newsuploadapp.UserRole
import com.halilkaya.newsuploadapp.model.RoleModel
import com.halilkaya.newsuploadapp.model.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class RoleViewModel : ViewModel() {

    private val _currentRoleRequest = MutableStateFlow<RoleModel?>(null)
    val currentRoleRequest = _currentRoleRequest.asStateFlow()

    val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    private val _allRoleRequests = MutableStateFlow<List<RoleModel>>(emptyList())
    val allRoleRequests=_allRoleRequests.asStateFlow()


    fun createRoleRequest(
        name :String,
        role:UserRole,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ){
        val currentUserUid = auth.currentUser?.uid ?: return
        val currentUserEmail = auth.currentUser?.email ?: ""

        val role = RoleModel(
            name = name,
            email = currentUserEmail,
            role=role
        )

        db.collection("rolerequests")
            .document(currentUserUid)
            .set(role)
            .addOnSuccessListener {
                _currentRoleRequest.value=role
                onSuccess()
            }
            .addOnFailureListener {  exception ->
                onFailure(exception.message?:"Save failed")

            }


    }


    fun getRoleRequest() {
        val currentUserUid = auth.currentUser?.uid ?: return

        db.collection("rolerequests")
            .document(currentUserUid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val roleModel = snapshot.toObject(RoleModel::class.java)
                    _currentRoleRequest.value = roleModel
                } else {
                    _currentRoleRequest.value = null
                }
            }
            .addOnFailureListener {
                _currentRoleRequest.value = null
            }
    }



    fun getAllRoleRequests() {
        db.collection("rolerequests")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val roleRequests = querySnapshot.documents.mapNotNull { document ->
                    document.toObject(RoleModel::class.java)
                }
                _allRoleRequests.value = roleRequests // MutableStateFlow<List<RoleModel>>
            }
            .addOnFailureListener { exception ->
                // Hata
            }
    }





    fun updateUserRole(uid:String,newRole: UserRole, userViewModel: UserViewModel,onSuccess: () -> Unit, onFailure: (String) -> Unit) {

        // uid parameter'ı aslında email olarak geliyor, önce kullanıcının rolünü güncelle
        userViewModel.updateUserRoleByEmail(
            email = uid,
            newRole = newRole,
            onSuccess = {
                // Kullanıcı rolü başarıyla güncellendikten sonra role request'i sil
                db.collection("rolerequests")
                    .whereEqualTo("email", uid)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val document = querySnapshot.documents[0]
                            
                            // Role request'i sil
                            document.reference.delete()
                                .addOnSuccessListener {
                                    // Local state'i güncelle
                                    _allRoleRequests.value = _allRoleRequests.value.filterNot { it.email == uid }
                                    onSuccess()
                                }
                                .addOnFailureListener { exception ->
                                    // Role güncellendi ama request silinemedi, gene de başarılı sayalım
                                    _allRoleRequests.value = _allRoleRequests.value.filterNot { it.email == uid }
                                    onSuccess()
                                }
                        } else {
                            // Role güncellendi ama request bulunamadı, gene de başarılı sayalım
                            onSuccess()
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Role güncellendi ama request silinemedi, gene de başarılı sayalım
                        onSuccess()
                    }
            },
            onFailure = { error ->
                onFailure("User role update failed: $error")
            }
        )
    }

    fun rejectRoleRequest(uid: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        // uid aslında email olarak geliyor, önce actual uid'yi bul
        db.collection("rolerequests")
            .whereEqualTo("email", uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    
                    document.reference.delete()
                        .addOnSuccessListener {
                            // Lokal listedeki role isteklerini de güncelle (isteği kaldır)
                            _allRoleRequests.value = _allRoleRequests.value.filterNot { it.email == uid }
                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            onFailure(exception.message ?: "Delete failed")
                        }
                } else {
                    onFailure("Role request not found")
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception.message ?: "Delete failed")
            }
    }


    fun deleteRoleRequest(uid:String/*onSuccess: () -> Unit, onFailure: (String) -> Unit*/) {

        db.collection("rolerequests")
            .document(uid)
            .delete()
            .addOnSuccessListener {
                //_currentRoleRequest.value = RoleModel() // Local state sıfırlanıyor
                //onSuccess()
            }
            .addOnFailureListener { exception ->
                //onFailure(exception.message ?: "Delete failed")
            }
    }

}
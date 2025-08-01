package com.halilkaya.newsuploadapp.model

import android.provider.ContactsContract.CommonDataKinds.Email
import com.halilkaya.newsuploadapp.UserRole

data class UserModel(
    var name: String = "",
    var email:String="",
    var city: String = "",
    var role: UserRole =UserRole.USER,
    var phoneNumber:String="",
)
package com.halilkaya.newsuploadapp.model

import com.halilkaya.newsuploadapp.UserRole


data class RoleModel(
    var name: String = "",
    var email:String="",
    var role: UserRole = UserRole.USER,
)
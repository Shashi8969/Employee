package com.example.rrserviceadmin.model

data class UserModel (
    val userName: String? = null,
    val address: String? = null,
    val location: String? = null,
    val userId: String? = null,
    val email: String? = null,
    val password: String? = null
    )
fun UserModel.toMap(): Map<String, Any?> {
    return mapOf(
        "userName" to userName,
        "Address" to address,
        "adminId" to userId,
        "email" to email,
        "password" to password
    )
}
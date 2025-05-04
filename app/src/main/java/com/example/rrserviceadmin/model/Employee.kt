package com.example.rrserviceadmin.model

import android.widget.Spinner

data class Employee(
    val name: String? = null,
    val phoneNo: String? = null,
    val addharNo: String? = null,
    var empId: String? = null,
    val referenceName: String? = null,
    val address: String? = null,
    val referencePhoneNo: String? = null,
    val imageUrl: String? = null,
    val addharFrontImageUrl: String? = null,
    val addharBackImageUrl: String? = null
)

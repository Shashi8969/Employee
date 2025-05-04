package com.example.rrserviceadmin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.rrserviceadmin.databinding.ActivityEmployeeDetailsBinding
import com.example.rrserviceadmin.model.Employee
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EmployeeDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmployeeDetailsBinding
    private val database = FirebaseDatabase.getInstance()
    private val employeesRef = database.getReference("Employees")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val employeeId = intent.getStringExtra("employeeId")
        if (!employeeId.isNullOrEmpty()) {
            fetchEmployeeDetails(employeeId)
        } else {
            // Handle case where employeeId is not passed
            finish()
        }
    }

    private fun fetchEmployeeDetails(employeeId: String) {
        employeesRef.orderByChild("empId").equalTo(employeeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (childSnapshot in snapshot.children) {
                            val employee = childSnapshot.getValue(Employee::class.java)
                            employee?.let { displayEmployeeDetails(it) }
                            return // Assuming empId is unique, we can return after finding the first match
                        }
                    } else {
                        // Employee not found
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                    finish()
                }
            })
    }

    private fun displayEmployeeDetails(employee: Employee) {
        binding.detailsNameTextView.text = employee.name
        binding.detailsPhoneTextView.text = employee.phoneNo
        binding.detailsAddharTextView.text = "Addhar No: ${employee.addharNo}"
        binding.detailsReferenceNameTextView.text = "Reference: ${employee.referenceName}"
        binding.detailsAddressTextView.text = "Address: ${employee.address}"

        if (!employee.imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(employee.imageUrl)
                .placeholder(R.drawable.addimage)
                .into(binding.detailsImageView)
        } else {
            binding.detailsImageView.setImageResource(R.drawable.addimage)
        }

        if (!employee.addharFrontImageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(employee.addharFrontImageUrl)
                .placeholder(R.drawable.addimage)
                .into(binding.detailsAddharFrontImageView)
        } else {
            binding.detailsAddharFrontImageView.setImageResource(R.drawable.addimage)
        }

        if (!employee.addharBackImageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(employee.addharBackImageUrl)
                .placeholder(R.drawable.addimage)
                .into(binding.detailsAddharBackImageView)
        } else {
            binding.detailsAddharBackImageView.setImageResource(R.drawable.addimage)
        }
    }

}
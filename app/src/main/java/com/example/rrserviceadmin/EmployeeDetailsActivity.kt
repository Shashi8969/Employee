package com.example.rrserviceadmin

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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

        // Retrieve the employeeId from the intent
        val employeeId = intent.getStringExtra("employeeId")
        Log.d("EmployeeDetails", "Received employeeId: $employeeId")

        // Check if employeeId is null or empty
        if (employeeId.isNullOrEmpty()) {
            Log.e("EmployeeDetails", "employeeId is null or empty")
            finish() // Close the activity if employeeId is missing
            return
        }

        fetchEmployeeDetails(employeeId)
    }

    private fun fetchEmployeeDetails(employeeId: String) {
        employeesRef.orderByChild("empId").equalTo(employeeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Iterate through all the children (should ideally be only one)
                        for (childSnapshot in snapshot.children) {
                            val employee = childSnapshot.getValue(Employee::class.java)
                            if (employee != null) {
                                displayEmployeeDetails(employee)
                                return // Exit after displaying details
                            } else {
                                Log.e("EmployeeDetails", "Employee object is null for empId: $employeeId")
                                finish()
                            }
                        }
                        // If no employee is found, log a warning and finish
                        Log.e("EmployeeDetails", "No employee data found for ID: $employeeId")
                        finish()
                    } else {
                        // Employee not found, log and finish
                        Log.e("EmployeeDetails", "Snapshot does not exist for ID: $employeeId")
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Log and finish on error
                    Log.e("EmployeeDetails", "Database error: ${error.message}")
                    finish()
                }
            })
    }

    private fun displayEmployeeDetails(employee: Employee) {
        binding.detailsNameTextView.text = employee.name
        binding.detailsPhoneTextView.text = "Phone: ${employee.phoneNo}"
        binding.detailsAddharTextView.text = "Addhar No: ${employee.addharNo}"
        binding.detailsReferenceNameTextView.text = "Reference: ${employee.referenceName}"
        binding.detailsAddressTextView.text = "Address: ${employee.address}"

        // Load the main employee image
        loadImage(employee.imageUrl, binding.detailsImageView)

        // Load the front Aadhar image
        loadImage(employee.addharFrontImageUrl, binding.detailsAddharFrontImageView)

        // Load the back Aadhar image
        loadImage(employee.addharBackImageUrl, binding.detailsAddharBackImageView)
    }

    // Helper function to load images using Glide
    private fun loadImage(imageUrl: String?, imageView: android.widget.ImageView) {
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.addimage)
                .error(R.drawable.addimage)// error image
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.addimage)
        }
    }
}
package com.example.rrserviceadmin

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rrserviceadmin.adapter.EmployeeAdapter
import com.example.rrserviceadmin.databinding.ActivityEmployeeListBinding
import com.example.rrserviceadmin.model.Employee
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EmployeeListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmployeeListBinding
    private lateinit var employeeAdapter: EmployeeAdapter
    private val employeeList = mutableListOf<Employee>()
    private val database = FirebaseDatabase.getInstance()
    private val employeesRef = database.getReference("Employees")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        binding = ActivityEmployeeListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        setupRecyclerView()
        fetchEmployeeData()
    }

    private fun setupRecyclerView() {
        binding.employeeRecyclerView.layoutManager = LinearLayoutManager(this)
        employeeAdapter = EmployeeAdapter(employeeList) { employee ->
            // Handle item click here
            navigateToEmployeeDetails(employee)
        }
        binding.employeeRecyclerView.adapter = employeeAdapter
    }

    private fun navigateToEmployeeDetails(employee: Employee) {
        val intent = Intent(this, EmployeeDetailsActivity::class.java)
        intent.putExtra("employeeId", employee.empId)
        startActivity(intent)
    }

    private fun fetchEmployeeData() {
        binding.progressBar.visibility = View.VISIBLE
        employeesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                employeeList.clear()
                for (childSnapshot in snapshot.children) {
                    val employee = childSnapshot.getValue(Employee::class.java)
                    employee?.let { employeeList.add(it) }
                }
                updateUI()
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                binding.emptyTextView.text = "Failed to load employees."
                binding.emptyTextView.visibility = View.VISIBLE
                binding.employeeRecyclerView.visibility = View.GONE
            }
        })
    }

    private fun updateUI() {
        binding.progressBar.visibility = View.GONE
        if (employeeList.isEmpty()) {
            binding.emptyTextView.visibility = View.VISIBLE
            binding.employeeRecyclerView.visibility = View.GONE
        } else {
            binding.emptyTextView.visibility = View.GONE
            binding.employeeRecyclerView.visibility = View.VISIBLE
            // Use the adapter's function to update the data
            employeeAdapter.updateEmployeeList(employeeList)

        }
    }
}
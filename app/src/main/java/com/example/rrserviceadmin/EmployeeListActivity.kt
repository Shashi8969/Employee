package com.example.rrserviceadmin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rrserviceadmin.databinding.ActivityEmployeeListBinding
import com.example.rrserviceadmin.databinding.ItemEmployeeBinding
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
        binding.employeeRecyclerView.layoutManager = LinearLayoutManager(this)
        employeeAdapter = EmployeeAdapter(employeeList) { employee ->
            // Handle item click here
            val intent = Intent(this, EmployeeDetailsActivity::class.java)
            intent.putExtra("employeeId", employee.empId) // Or pass the entire employee object using Parcelable/Serializable
            startActivity(intent)
        }
        binding.employeeRecyclerView.adapter = employeeAdapter

        fetchEmployeeData()
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
                binding.progressBar.visibility = View.GONE
                if (employeeList.isEmpty()) {
                    binding.emptyTextView.visibility = View.VISIBLE
                    binding.employeeRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyTextView.visibility = View.GONE
                    binding.employeeRecyclerView.visibility = View.VISIBLE
                    employeeAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                binding.emptyTextView.text = "Failed to load employees."
                binding.emptyTextView.visibility = View.VISIBLE
                binding.employeeRecyclerView.visibility = View.GONE
                // Handle error appropriately
            }
        })
    }
    private class EmployeeAdapter(
        private val employeeList: List<Employee>,
        private val onItemClick: (Employee) -> Unit
    ) : RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
            val binding = ItemEmployeeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return EmployeeViewHolder(binding)
        }

        override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
            val employee = employeeList[position]
            holder.bind(employee)
            Log.d("EmployeeList", "Binding employee at position $position: ${employee.name} - ${employee.empId}") // Added log

            holder.itemView.setOnClickListener {
                Log.d("EmployeeList", "Clicked employee: ${employee.name} - ${employee.empId}") // Added log
                onItemClick(employee)
            }
        }

        override fun getItemCount(): Int = employeeList.size

        inner class EmployeeViewHolder(private val binding: ItemEmployeeBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(employee: Employee) {
                binding.employeeNameTextView.text = employee.name
                binding.employeePhoneTextView.text = employee.phoneNo
                if (!employee.imageUrl.isNullOrEmpty()) {
                    Glide.with(binding.employeeImageView.context)
                        .load(employee.imageUrl)
                        .placeholder(R.drawable.addimage)
                        .into(binding.employeeImageView)
                } else {
                    binding.employeeImageView.setImageResource(R.drawable.addimage)
                }
            }
        }
    }
}
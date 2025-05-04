package com.example.rrserviceadmin.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rrserviceadmin.R
import com.example.rrserviceadmin.databinding.ItemEmployeeBinding
import com.example.rrserviceadmin.model.Employee

class EmployeeAdapter(
    private var employeeList: List<Employee>, // Changed to var for potential list updates
    private val onItemClick: (Employee) -> Unit
) : RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder>() {

    // Helper function to update the employee list
    fun updateEmployeeList(newEmployeeList: List<Employee>) {
        employeeList = newEmployeeList
        notifyDataSetChanged() // Notify RecyclerView about the change
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val binding = ItemEmployeeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EmployeeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        val employee = employeeList[position]
        holder.bind(employee)

        // Log binding info for debugging
        Log.d("EmployeeList", "Binding employee at position $position: ${employee.name} - ${employee.empId}")

        // Remove the old click listener before setting a new one to avoid multiple listeners
        holder.itemView.setOnClickListener(null)
        // Set the click listener
        holder.itemView.setOnClickListener {
            Log.d("EmployeeList", "Clicked employee: ${employee.name} - ${employee.empId}")
            onItemClick(employee)
        }
    }

    override fun getItemCount(): Int = employeeList.size

    inner class EmployeeViewHolder(private val binding: ItemEmployeeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(employee: Employee) {
            binding.employeeNameTextView.text = employee.name
            binding.employeePhoneTextView.text = employee.phoneNo

            // Load the image using Glide or set the default image
            if (!employee.imageUrl.isNullOrEmpty()) {
                Glide.with(binding.employeeImageView.context)
                    .load(employee.imageUrl)
                    .placeholder(R.drawable.addimage)
                    .error(R.drawable.addimage) // set error image
                    .into(binding.employeeImageView)
            } else {
                binding.employeeImageView.setImageResource(R.drawable.addimage)
            }
        }
    }
}
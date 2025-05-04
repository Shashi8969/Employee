package com.example.rrserviceadmin

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.rrserviceadmin.databinding.ActivitySignUpBinding
import com.example.rrserviceadmin.model.UserModel
import com.example.rrserviceadmin.model.toMap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var binding: ActivitySignUpBinding
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        database = Firebase.database.reference
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPasswordVisibilityToggle(binding.signupPassword)
        setupClickListeners()
    }


    private fun setupPasswordVisibilityToggle(passwordEditText: EditText) {
        passwordEditText.setOnTouchListener { _, event ->
            val drawableRight = 2
            if (event.action == MotionEvent.ACTION_UP && event.rawX >= (passwordEditText.right - passwordEditText.compoundDrawables[drawableRight].bounds.width())) {
                isPasswordVisible = !isPasswordVisible
                val selection = passwordEditText.selectionEnd
                passwordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    ContextCompat.getDrawable(this, R.drawable.lock),
                    null,
                    ContextCompat.getDrawable(
                        this,
                        if (isPasswordVisible) R.drawable.eye else R.drawable.eye_hide
                    ),
                    null
                )
                passwordEditText.inputType = if (isPasswordVisible) {
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                } else {
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                }
                passwordEditText.setSelection(selection)
                return@setOnTouchListener true
            }
            false
        }
    }

    private fun setupClickListeners() {
        binding.txtAlready.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.createBtn.setOnClickListener {
            createUserAccount()
        }
    }

    private fun createUserAccount() {
        val email = binding.emailId.text.toString().trim()
        val password = binding.signupPassword.text.toString().trim()
        val userName = binding.etName.text.toString().trim()
        val address = binding.restroName.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || userName.isEmpty() || address.isEmpty() ) {
            Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("SignUp", "Account created successfully for $email")
                    saveUserData(email, userName, address,password)
                } else {
                    Log.e(
                        "SignUp",
                        "Account creation failed for $email: ${task.exception?.message}"
                    )
                    Toast.makeText(
                        this,
                        "Account creation failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun saveUserData(
        email: String,
        userName: String,
        address: String,
        password: String
    ) {
        val adminId = auth.currentUser?.uid
        if (adminId == null) {
            Log.e("SignUp", "User not signed in after account creation!")
            Toast.makeText(this, "Error: User not signed in. Please try again.", Toast.LENGTH_LONG)
                .show()
            return
        }

        val user = UserModel(userName,address,"",userId = adminId, email, password)
        val userUpdates = user.toMap()

        database.child("Admin").child(adminId).updateChildren(userUpdates)
            .addOnSuccessListener {
                Log.d("SignUp", "User data saved successfully for $adminId")
                Toast.makeText(
                    this,
                    "Account created and data saved successfully!",
                    Toast.LENGTH_SHORT
                ).show()
                navigateToLogin()
                saveBasicInformation(adminId, userName, email)  // Call a new function
            }
            .addOnFailureListener { e ->
                Log.e("SignUp", "Error saving user data for $adminId: ${e.message}")
                Toast.makeText(this, "Error saving user data: ${e.message}", Toast.LENGTH_LONG)
                    .show()
                // Optionally, you could delete the newly created account here if saving data fails:
                auth.currentUser?.delete()
            }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }


    private fun saveBasicInformation(adminId: String, userName: String, email: String) {
        val basicInfo = mapOf(
            "name" to userName,  // or "userName" if you prefer
            "email" to email
        )

        database.child("Admin").child(adminId).child("Basic Information").updateChildren(basicInfo)
            .addOnSuccessListener {
                Log.d("SignUp", "Basic Information saved for $adminId")
                Toast.makeText(
                    this,
                    "Account created and data saved successfully!",
                    Toast.LENGTH_SHORT
                ).show()
                navigateToLogin()
            }
            .addOnFailureListener { e ->
                Log.e("SignUp", "Error saving basic information for $adminId: ${e.message}")
                Toast.makeText(
                    this,
                    "Error saving basic information: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

            }
    }
}
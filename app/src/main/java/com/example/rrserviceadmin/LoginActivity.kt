package com.example.rrserviceadmin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.rrserviceadmin.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var email: String
    private lateinit var password: String
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth


        // Set up password visibility toggle
        setupPasswordVisibilityToggle(binding.loginPassword)

        binding.txtCreate.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.LoginButton.setOnClickListener {
            email = binding.etEmail.text.toString().trim()
            password = binding.loginPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loginAccount(email, password)
        }

        binding.googleSignInButton.setOnClickListener {
//            signInWithGoogle()
            Toast.makeText(this, "Google Sign-In button clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginAccount(email: String, password: String) {
        if (!::auth.isInitialized) {
            Log.e("LoginActivity", "Error: auth is not initialized!")
            Toast.makeText(this, "Authentication not initialized", Toast.LENGTH_SHORT).show()
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this@LoginActivity) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Toast.makeText(this@LoginActivity, "Login Successful.", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    // If sign in fails
                    Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
                    Toast.makeText(this@LoginActivity, "Authentication failed.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun setupPasswordVisibilityToggle(passwordEditText: EditText) {
        passwordEditText.setOnTouchListener { _, event ->
            val drawableRight = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (passwordEditText.right - passwordEditText.compoundDrawables[drawableRight].bounds.width())) {
                    isPasswordVisible = !isPasswordVisible
                    val selection = passwordEditText.selectionEnd
                    if (isPasswordVisible) {
                        passwordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            ContextCompat.getDrawable(this, R.drawable.lock),
                            null,
                            ContextCompat.getDrawable(this, R.drawable.eye),
                            null
                        )
                        passwordEditText.inputType =
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    } else {
                        passwordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            ContextCompat.getDrawable(this, R.drawable.lock),
                            null,
                            ContextCompat.getDrawable(this, R.drawable.eye_hide),
                            null
                        )
                        passwordEditText.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }
                    passwordEditText.setSelection(selection)
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }
    }


    private fun navigateToMainActivity() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    //Check if user is already logged in
    override fun onStart(){
        super.onStart()
        val currentUser = auth.currentUser
        if(currentUser != null){
            navigateToMainActivity()
        }
    }
}
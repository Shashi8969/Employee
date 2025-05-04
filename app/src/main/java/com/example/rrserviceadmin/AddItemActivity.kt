package com.example.rrserviceadmin

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.rrserviceadmin.StringHeandling.toSentenceCase
import com.example.rrserviceadmin.databinding.ActivityAddItemBinding
import com.example.rrserviceadmin.model.Employee
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

class AddItemActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storageRef: StorageReference
    private val binding: ActivityAddItemBinding by lazy {
        ActivityAddItemBinding.inflate(layoutInflater)
    }

    // Data for Spinners
    val addressOptions = arrayOf("Select Address", "Muzaffarpur", "Darbhanga", "Madhubani") // Added a default prompt
    val referenceNameOptions = arrayOf("Select Reference Name", "Rishi", "Ravi", "Vinay", "Rahul") // Added a default prompt

    // Adapters for Spinners
    private lateinit var addressAdapter: ArrayAdapter<String>
    private lateinit var referenceNameAdapter: ArrayAdapter<String>

    // Employee Details
    private var name: String? = null
    private var phoneNo: String? = null
    private var addharNo: String? = null
    private var address: String? = null // Changed to String to store selected value
    private var empId: String? = null
    private var referenceName: String? = null // Changed to String to store selected value

    // Image URIs
    private var empImageUri: Uri? = null
    private var addharFrontImageUri: Uri? = null
    private var addharBackImageUri: Uri? = null

    // Activity Result Launchers
    private val pickEmpImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            handleImagePick(
                uri,
                ImageType.EMP
            )
        }
    private val pickAddharFrontImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            handleImagePick(
                uri,
                ImageType.ADDHAR_FRONT
            )
        }
    private val pickAddharBackImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            handleImagePick(
                uri,
                ImageType.ADDHAR_BACK
            )
        }

    // Image Type Enum
    enum class ImageType {
        EMP, ADDHAR_FRONT, ADDHAR_BACK
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initializeFirebase()
        empId = auth.currentUser?.uid
        setupUI()
        setupClickListeners()
        setupSpinners() // Call this method to initialize Spinners
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storageRef = FirebaseStorage.getInstance().reference
    }

    private fun setupUI() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupClickListeners() {
        binding.empPic.setOnClickListener { pickEmpImage.launch("image/*") }
        binding.backBtn.setOnClickListener { finish() }
        binding.addharFront.setOnClickListener { pickAddharFrontImage.launch("image/*") }
        binding.addharBack.setOnClickListener { pickAddharBackImage.launch("image/*") }
        binding.addItemsButton.setOnClickListener {
            if (validateFields()) {
                uploadEmployeeData()
            }
        }
    }

    private fun setupSpinners() {
        addressAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, addressOptions)
        binding.addressSpinner.adapter = addressAdapter

        referenceNameAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, referenceNameOptions)
        binding.referenceNameSpinner.adapter = referenceNameAdapter
    }

    private fun validateFields(): Boolean {
        bindingItems()

        return when {
            name.isNullOrEmpty() -> {
                showErrorAndFocus(binding.name, "Please enter name")
                false
            }

            phoneNo.isNullOrEmpty() -> {
                showErrorAndFocus(binding.phone, "Please enter phone number")
                false
            }

            addharNo.isNullOrEmpty() -> {
                showErrorAndFocus(binding.addhar, "Please enter addhar number")
                false
            }

            address == "Select Address" -> { // Check if a valid address is selected
                Toast.makeText(this, "Please select address", Toast.LENGTH_SHORT).show()
                false
            }

            referenceName == "Select Reference Name" -> { // Check if a valid reference name is selected
                Toast.makeText(this, "Please select reference name", Toast.LENGTH_SHORT).show()
                false
            }

            else -> true
        }
    }

    private fun showErrorAndFocus(view: android.widget.EditText, errorMessage: String) {
        view.error = errorMessage
        view.requestFocus()
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun bindingItems() {
        name = binding.name.text.toString().toSentenceCase()
        phoneNo = binding.phone.text.toString()
        addharNo = binding.addhar.text.toString()
        address = binding.addressSpinner.selectedItem.toString() // Get selected string from Spinner
        referenceName = binding.referenceNameSpinner.selectedItem.toString() // Get selected string from Spinner
    }

    private fun uploadEmployeeData() {
        binding.addItemsButton.isEnabled = false
        binding.addItemsButton.text = "Uploading..."
        binding.addItemsButton.alpha = 0.5f

        val currentAdminUid = auth.currentUser?.uid ?: run {
            showError("Admin not logged in")
            resetUploadButton()
            return
        }
        val empRef = database.getReference("Employees")
        val newItemRef = empRef.push().key ?: run {
            showError("Failed to generate a key")
            resetUploadButton()
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val imageUrl =
                    empImageUri?.let { uploadImage(it, "images/$newItemRef/emp_image.jpg") }
                val addharFrontUrl = addharFrontImageUri?.let {
                    uploadImage(
                        it,
                        "images/$newItemRef/addhar_front.jpg"
                    )
                }
                val addharBackUrl = addharBackImageUri?.let {
                    uploadImage(
                        it,
                        "images/$newItemRef/addhar_back.jpg"
                    )
                }

                val employeeDetails = Employee(
                    name = name,
                    phoneNo = phoneNo,
                    addharNo = addharNo,
                    empId = empId,
                    referenceName = referenceName,
                    address = address,
                    imageUrl = imageUrl,
                    addharFrontImageUrl = addharFrontUrl,
                    addharBackImageUrl = addharBackUrl
                )
                empRef.child(newItemRef).setValue(employeeDetails).await()
                showSuccess("Item added successfully")
                finish()
            } catch (e: Exception) {
                showError("Failed to upload item: ${e.message}")
            } finally {
                resetUploadButton()
            }
        }
    }

    private fun showError(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccess(string: String) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
    }

    private fun resetUploadButton() {
        binding.addItemsButton.isEnabled = true
        binding.addItemsButton.text = "Upload"
        binding.addItemsButton.alpha = 1.0f
    }

    private suspend fun uploadImage(uri: Uri, path: String): String? {
        val imageRef = storageRef.child(path)
        try {
            val compressedImageUri = compressImage(uri)
            imageRef.putFile(compressedImageUri).await()
            return imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            showError("Failed to upload image: ${e.message}")
            return null
        }
    }

    private suspend fun compressImage(uri: Uri): Uri {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val imageFile = File(cacheDir, contentResolver.getFileName(uri))
                inputStream.copyTo(imageFile.outputStream()) // Copy InputStream to a temporary File
                val compressedImageFile = Compressor.compress(this@AddItemActivity, imageFile) {
                    quality(80)
                    resolution(720, 720)
                }
                imageFile.delete() // Delete the temporary file
                Uri.fromFile(compressedImageFile)
            } ?: run {
                Log.e("AddItemActivity", "Compression error: Could not open InputStream")
                showError("Image compression failed")
                uri
            }
        } catch (e: Exception) {
            Log.e("AddItemActivity", "Compression error: ${e.message}")
            showError("Image compression failed")
            uri
        }
    }
    private fun handleImagePick(uri: Uri?, imageType: ImageType) {
        if (uri != null) {
            when (imageType) {
                ImageType.EMP -> {
                    binding.empPic.setImageURI(uri)
                    empImageUri = uri
                }

                ImageType.ADDHAR_FRONT -> {
                    binding.addharFront.setImageURI(uri)
                    addharFrontImageUri = uri
                }

                ImageType.ADDHAR_BACK -> {
                    binding.addharBack.setImageURI(uri)
                    addharBackImageUri = uri
                }
            }
        }
    }

    // Helper function to get file name from Uri
    private fun ContentResolver.getFileName(uri: Uri): String {
        var name = ""
        val returnCursor = this.query(uri, null, null, null, null)
        returnCursor?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }
}
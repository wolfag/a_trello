package com.example.a_trello.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.a_trello.R
import com.example.a_trello.databinding.ActivityMyProfileBinding
import com.example.a_trello.firebase.FirestoreClass
import com.example.a_trello.models.User
import com.example.a_trello.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class MyProfileActivity : BaseActivity() {
    private var binding: ActivityMyProfileBinding? = null

    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageURL: String = ""
    private lateinit var mUserDetails: User
    private var mIsFieldsChanged = false

    companion object {
        private const val READ_STORAGE_PERMISSION_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()
        FirestoreClass().loadUserData(this)

        binding?.btnUpdate?.setOnClickListener {
            updateUserData()
        }

        binding?.ivUserImage?.setOnClickListener {
            imagePress()
        }

        setButtonStatus()
    }

    private fun setButtonStatus() {

    }

    private fun imagePress() {
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Constants.showImageChooser(galleryLauncher)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_STORAGE_PERMISSION_CODE
            )
        }
    }

    private var galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                mSelectedImageFileUri = result.data?.data

                try {
                    binding?.ivUserImage?.let {
                        Glide.with(this).load(mSelectedImageFileUri).centerCrop()
                            .placeholder(R.drawable.ic_user_place_holder).into(
                                it
                            )
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(galleryLauncher)
            }
        } else {
            Toast.makeText(
                this, "Oops, you just denied the permission for storage.", Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateUserData() {
        if (mSelectedImageFileUri != null) {
            uploadImage()
        } else {
            updateProfileData()
        }
    }

    private fun uploadImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        if (mSelectedImageFileUri != null) {
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "${Constants.USERS_IMAGE}${System.currentTimeMillis()}.${
                    Constants.getFileExtension(this, mSelectedImageFileUri!!)
                }"
            )
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener { task ->
                task.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    mProfileImageURL = uri.toString()

                    updateProfileData()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }

    private fun updateProfileData() {
        val userHashMap = HashMap<String, Any>()
        val name = binding?.etName?.text.toString()
        val mobile = binding?.etMobile?.text.toString()


        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileImageURL
        }
        if (name != mUserDetails.name) {
            userHashMap[Constants.NAME] = name
        }
        if (mobile != mUserDetails.mobile.toString()) {
            if (mobile.isEmpty()) {
                userHashMap[Constants.MOBILE] = 0L
            } else {
                userHashMap[Constants.MOBILE] = mobile.toLong()
            }

        }

        if (isShowingProgressDialog()) {
            showProgressDialog(resources.getString(R.string.please_wait))
        }
        FirestoreClass().updateUserProfileData(this, userHashMap)
    }

    fun profileUpdateSuccess() {
        if (isShowingProgressDialog()) {
            hideProgressDialog()
        }

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.tbMyProfileActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back)
            actionBar.title = resources.getString(R.string.my_profile)
        }

        binding?.tbMyProfileActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    fun setUserDataInUI(user: User) {
        mUserDetails = user
        binding?.etEmail?.setText(user.email)
        binding?.etName?.setText(user.name)
        if (user.mobile != 0L) {
            binding?.etMobile?.setText(user.mobile.toString())
        }

        binding?.ivUserImage?.let {
            Glide.with(this).load(user.image).centerCrop()
                .placeholder(R.drawable.ic_user_place_holder).into(it)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
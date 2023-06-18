package com.example.a_trello.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.a_trello.R
import com.example.a_trello.databinding.ActivityCreateBoardBinding
import com.example.a_trello.firebase.FirestoreClass
import com.example.a_trello.models.Board
import com.example.a_trello.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class CreateBoardActivity : BaseActivity() {
    private var mSelectedImageFileUri: Uri? = null
    private var mBoardImageURL: String = ""
    private var binding: ActivityCreateBoardBinding? = null

    private lateinit var mUserName: String

    companion object {
        const val READ_STORAGE_PERMISSION_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        extractIntent()

        binding?.ivBoardImage?.setOnClickListener {
            imagePress()
        }

        binding?.btnCreate?.setOnClickListener {
            handleAdd()
        }
    }

    private fun extractIntent() {
        if (intent.hasExtra(Constants.NAME)) {
            mUserName = intent.getStringExtra(Constants.NAME).toString()
        }
    }

    private fun handleAdd() {
        if (mSelectedImageFileUri != null) {
            uploadImage()
        } else {
            createBoard()
        }
    }

    private fun uploadImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        if (mSelectedImageFileUri != null) {
            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "${Constants.BOARDS_IMAGE}${System.currentTimeMillis()}.${
                    Constants.getFileExtension(this, mSelectedImageFileUri!!)
                }"
            )
            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener { task ->
                task.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    mBoardImageURL = uri.toString()

                    createBoard()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }

    private fun createBoard() {
        val assignedUsers: ArrayList<String> = ArrayList()
        assignedUsers.add(getCurrentUserID())

        var boardName = binding?.etBoardName?.text.toString()

        var board = Board(
            boardName,
            mBoardImageURL,
            mUserName,
            assignedUsers
        )

        if (boardName.isNotEmpty()) {
            if (!isShowingProgressDialog()) {
                showProgressDialog(resources.getString(R.string.please_wait))
            }
            FirestoreClass().createBoard(this, board)
        } else {
            showErrorSnackBar("Please enter board name")
        }


    }

    fun createBoardSuccessfully() {
        if (isShowingProgressDialog()) {
            hideProgressDialog()
        }
        setResult(Activity.RESULT_OK)
        finish()
    }


    private var galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                mSelectedImageFileUri = result.data?.data

                try {
                    binding?.ivBoardImage?.let {
                        Glide.with(this).load(mSelectedImageFileUri).centerCrop()
                            .placeholder(R.drawable.ic_board_place_holder).into(it)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
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


    private fun setupActionBar() {
        setSupportActionBar(binding?.tbCreateBoard)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back)
            actionBar.title = resources.getString(R.string.create_board_title)
        }

        binding?.tbCreateBoard?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
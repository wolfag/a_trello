package com.example.a_trello.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.a_trello.R
import com.example.a_trello.adapter.BoardItemsAdapter
import com.example.a_trello.databinding.ActivityMainBinding
import com.example.a_trello.databinding.NavHeaderMainBinding
import com.example.a_trello.firebase.FirestoreClass
import com.example.a_trello.models.Board
import com.example.a_trello.models.User
import com.example.a_trello.utils.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var binding: ActivityMainBinding? = null

    private lateinit var mUserName: String

    private val createBoardLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                FirestoreClass().getBoardsList(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        binding?.navView?.setNavigationItemSelectedListener(this)

        binding?.appBarMainLayout?.fabCreateBoard?.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            createBoardLauncher.launch(intent)
        }

        FirestoreClass().loadUserData(this, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    fun updateNavigationUserDetails(user: User, isToReadBoardsList: Boolean) {
        if (isShowingProgressDialog()) {
            hideProgressDialog()
        }
        mUserName = user.email

        val viewHeader = binding?.navView?.getHeaderView(0)
        val headerBinding = viewHeader?.let { NavHeaderMainBinding.bind(it) }

        headerBinding?.ivUserImage?.let {
            Glide.with(this).load(user.image).centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(it)
        }

        headerBinding?.tvUserName?.text = user.name

        if (isToReadBoardsList) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this)
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.appBarMainLayout?.tbMainActivity)
        binding?.appBarMainLayout?.tbMainActivity?.setNavigationIcon(R.drawable.ic_menu)
        binding?.appBarMainLayout?.tbMainActivity?.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer() {
        if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        } else {
            binding?.drawerLayout?.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExist()
        }
    }

    private val myProfileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                FirestoreClass().loadUserData(this)
            }
        }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navMyProfile -> {
                myProfileLauncher.launch(Intent(this, MyProfileActivity::class.java))
            }

            R.id.navSignOut -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)

        return true
    }

    private fun visibleBoardList(show: Boolean = true) {
        binding?.appBarMainLayout?.contentMainLayout?.rvBoardsList?.visibility =
            if (show) View.VISIBLE else View.GONE
        binding?.appBarMainLayout?.contentMainLayout?.tvNoBoardAvailable?.visibility =
            if (show) View.GONE else View.VISIBLE
    }

    fun populateBoardListToUI(boardList: ArrayList<Board>) {
        if (isShowingProgressDialog()) {
            hideProgressDialog()
        }

        val contentMainLayout = binding?.appBarMainLayout?.contentMainLayout
        if (boardList.size > 0) {
            visibleBoardList()

            contentMainLayout?.rvBoardsList?.layoutManager = LinearLayoutManager(this)
            contentMainLayout?.rvBoardsList?.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this, boardList)
            contentMainLayout?.rvBoardsList?.adapter = adapter

            adapter.setOnClickListener(object : BoardItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Board) {
                    var intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })

        } else {
            visibleBoardList(false)
        }
    }
}
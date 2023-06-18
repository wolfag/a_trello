package com.example.a_trello.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.a_trello.activities.*
import com.example.a_trello.models.Board
import com.example.a_trello.models.User
import com.example.a_trello.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {
    private val mFirestore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        mFirestore.collection(Constants.USERS_COLLECTION).document(getCurrentUserId())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisterSuccess(userInfo.name, userInfo.email)
            }
            .addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "sign up in fail")
            }
    }

    fun loadUserData(activity: Activity, isToReadBoardList: Boolean = false) {
        mFirestore.collection(Constants.USERS_COLLECTION)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener { doc ->
                val loggedInUser = doc.toObject(User::class.java)
                if (loggedInUser != null) {

                    when (activity) {
                        is SignInActivity -> {
                            activity.signInSuccess(loggedInUser)
                        }

                        is MainActivity -> {
                            activity.updateNavigationUserDetails(loggedInUser, isToReadBoardList)
                        }

                        is MyProfileActivity -> {
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(activity.javaClass.simpleName, "sign in in fail")
            }
    }

    fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var id = ""
        if (currentUser != null) {
            id = currentUser.uid
        }
        return id
    }

    fun updateUserProfileData(activity: MyProfileActivity, userHashMap: HashMap<String, Any>) {
        mFirestore.collection(Constants.USERS_COLLECTION).document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Toast.makeText(activity, "Profile data updated", Toast.LENGTH_LONG).show()
                activity.profileUpdateSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, e.message.toString())
                Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board) {
        mFirestore.collection(Constants.BOARDS_COLLECTION).document().set(board, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(activity, "Board created", Toast.LENGTH_LONG).show()
                activity.createBoardSuccessfully()
            }.addOnFailureListener { exception ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, exception.message.toString())
            }
    }

    fun getBoardsList(activity: MainActivity) {
        mFirestore.collection(Constants.BOARDS_COLLECTION)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
            .get()
            .addOnSuccessListener { doc ->
                val boardsList: ArrayList<Board> = ArrayList()
                for (i in doc.documents) {
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardsList.add(board)
                }
                activity.populateBoardListToUI(boardsList)

            }.addOnFailureListener { e ->
                if (!activity.isShowingProgressDialog()) {
                    activity.hideProgressDialog()
                }
            }
    }

    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        mFirestore.collection(Constants.BOARDS_COLLECTION)
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id

                activity.showBoardDetails(board)
            }
            .addOnFailureListener { e ->
                if (activity.isShowingProgressDialog()) {
                    activity.hideProgressDialog()
                }
            }
    }

    fun addUpdateTaskList(activity: Activity, board: Board) {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFirestore.collection(Constants.BOARDS_COLLECTION)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                if (activity is TaskListActivity) {
                    activity.addUpdateTaskListSuccess()
                } else if (activity is CardDetailsActivity) {
                    activity.addUpdateTaskListSuccess()
                }

            }.addOnFailureListener {
                if (activity is TaskListActivity) {
                    if (activity.isShowingProgressDialog()) {
                        activity.hideProgressDialog()
                    }
                } else if (activity is CardDetailsActivity) {
                    if (activity.isShowingProgressDialog()) {
                        activity.hideProgressDialog()
                    }
                }
            }
    }

    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>) {
        mFirestore.collection(Constants.USERS_COLLECTION)
            .whereIn(Constants.ID, assignedTo).get()
            .addOnSuccessListener { doc ->
                val usersList: ArrayList<User> = ArrayList()
                for (i in doc.documents) {
                    usersList.add(i.toObject(User::class.java)!!)
                }

                if (activity is MembersActivity) {
                    activity.setupMembersList(usersList)
                } else if (activity is TaskListActivity) {
                    activity.boardMembersDetailList(usersList)
                }

            }.addOnFailureListener { e ->
                if (activity is MembersActivity) {
                    if (activity.isShowingProgressDialog()) {
                        activity.hideProgressDialog()
                    }
                } else if (activity is TaskListActivity) {
                    if (activity.isShowingProgressDialog()) {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(activity.javaClass.simpleName, e.message.toString())
            }
    }

    fun getMemberDetail(activity: MembersActivity, email: String) {
        mFirestore.collection(Constants.USERS_COLLECTION)
            .whereEqualTo(Constants.EMAIL, email).get()
            .addOnSuccessListener { doc ->
                if (doc.documents.size > 0) {
                    val user = doc.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found.")
                }
            }
            .addOnFailureListener { e ->
                if (activity.isShowingProgressDialog()) {
                    activity.hideProgressDialog()
                }
                Log.e(activity.javaClass.simpleName, e.message.toString())
            }
    }

    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User) {
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFirestore.collection(Constants.BOARDS_COLLECTION)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, e.message.toString())
            }
    }


}
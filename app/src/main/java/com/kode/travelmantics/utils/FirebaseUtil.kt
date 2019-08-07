package com.kode.travelmantics.utils

import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.kode.travelmantics.ListActivity
import com.kode.travelmantics.TravelDeal

class FirebaseUtil private constructor() {

    companion object {
        private const val RC_SIGN_IN = 123
        lateinit var firebaseDatabase: FirebaseDatabase
        lateinit var databaseReference: DatabaseReference
        lateinit var firebaseAuth: FirebaseAuth
        lateinit var authListener: AuthStateListener
        lateinit var storage: FirebaseStorage
        lateinit var storageRef: StorageReference
        lateinit var deals: ArrayList<TravelDeal>
        var isAdmin: Boolean = false
        lateinit var caller: ListActivity
        var firebaseUtil: FirebaseUtil? = null

        fun openFbReference(ref: String, callerActivity: ListActivity = ListActivity()) {
            if (firebaseUtil == null){
                firebaseUtil = FirebaseUtil()
                firebaseDatabase = FirebaseDatabase.getInstance()
                firebaseAuth = FirebaseAuth.getInstance()
                caller = callerActivity
                authListener = AuthStateListener {
                    if (firebaseAuth.currentUser == null)
                        signIn()
                    else {
                        val userId = firebaseAuth.uid
                        checkAdmin(userId)
                    }
                    Toast.makeText(callerActivity.baseContext, "Welcome back!", Toast.LENGTH_LONG).show()
                }
            }

            connectStorage()

            deals = arrayListOf()
            databaseReference = firebaseDatabase.reference.child(ref)
        }

        fun attachListener() {
            firebaseAuth.addAuthStateListener(authListener)
        }

        fun detachListener() {
            firebaseAuth.removeAuthStateListener(authListener)
        }

        private fun signIn() {
            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build())

            caller.startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build(),
                RC_SIGN_IN)
        }

        private fun checkAdmin(uid: String?) {
            isAdmin = false
            val ref = firebaseDatabase.reference.child("administrators")
                .child(uid!!)

            val childEventListener = object : ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    isAdmin = true
                    caller.showMenu()
                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {

                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {

                }

                override fun onChildRemoved(p0: DataSnapshot) {

                }
            }
            ref.addChildEventListener(childEventListener)
        }

        private fun connectStorage() {
            storage = FirebaseStorage.getInstance()
            storageRef = storage.reference.child("deals_pictures")
        }
    }
}
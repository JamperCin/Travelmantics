package com.kode.travelmantics

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.kode.travelmantics.utils.FirebaseUtil
import kotlinx.android.synthetic.main.activity_list.*

class ListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
    }

    override fun onResume() {
        super.onResume()
        invalidateOptionsMenu()

        FirebaseUtil.openFbReference("traveldeals", this)
        val adapter = DealsRecyclerAdapter()
        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvDeals.layoutManager = layoutManager
        rvDeals.adapter = adapter

        FirebaseUtil.attachListener()
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        val currentUser = FirebaseUtil.firebaseAuth.currentUser
        if (currentUser == null){
            FirebaseUtil.detachListener()
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        FirebaseUtil.detachListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.list_activity_menu, menu)
        val insertMenu = menu?.findItem(R.id.action_insert)
        insertMenu?.isVisible = FirebaseUtil.isAdmin == true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_insert -> {
                val intent = Intent(this, DealActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_logout -> {
                AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener {
                        Log.d("Logout", "User Logged out")
                        Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show()
                        FirebaseUtil.attachListener()
                    }
                FirebaseUtil.detachListener()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun showMenu() {
        invalidateOptionsMenu()
    }
}

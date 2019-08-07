package com.kode.travelmantics

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.view.View
import com.google.android.gms.tasks.Continuation
import com.kode.travelmantics.utils.FirebaseUtil
import kotlinx.android.synthetic.main.activity_deal.*
import com.google.firebase.storage.UploadTask
import com.google.android.gms.tasks.Task
import com.squareup.picasso.Picasso


const val PICTURE_RESULT = 42

class DealActivity : AppCompatActivity() {
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var deal: TravelDeal

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deal)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        FirebaseUtil.openFbReference("traveldeals")
        firebaseDatabase = FirebaseUtil.firebaseDatabase
        databaseReference = FirebaseUtil.databaseReference

        setTextViewValuesFromIntent()
    }

    fun uploadImage(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        }

        startActivityForResult(Intent.createChooser(intent, "Insert Picture"), PICTURE_RESULT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICTURE_RESULT && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            val ref = FirebaseUtil.storageRef.child(imageUri?.lastPathSegment!!)
            val uploadTask = ref.putFile(imageUri)

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    deal.imageUrl = downloadUri.toString()
                    deal.imageName = ref.path
                    Log.d("Url", deal.imageUrl)
                    Log.d("Path", deal.imageName)
                    Toast.makeText(this, "Upload Successful", Toast.LENGTH_LONG).show()
                    showImage(downloadUri.toString())
                } else {
                }
            }
        }
    }

    private fun setTextViewValuesFromIntent() {
        val intent = intent
        var dealFromIntent = intent.getParcelableExtra<TravelDeal>("Deal")
        if (dealFromIntent == null)
            dealFromIntent = TravelDeal()

        deal = dealFromIntent
        showImage(deal.imageUrl)
        txtTitle.setText(deal.title)
        txtDescription.setText(deal.description)
        txtPrice.setText(deal.price)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.deal_menu, menu)
        menu?.findItem(R.id.action_save)?.isVisible = FirebaseUtil.isAdmin == true
        menu?.findItem(R.id.action_delete)?.isVisible = FirebaseUtil.isAdmin == true
        enableEditText(FirebaseUtil.isAdmin)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.action_save -> {
                saveDeal()
                Toast.makeText(this, "Deal Saved", Toast.LENGTH_LONG).show()
                clean()
                finish()
                true
            }
            R.id.action_delete -> {
                deleteDeal()
                Toast.makeText(this, "Deal Deleted", Toast.LENGTH_LONG).show()
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveDeal() {
        deal.title = txtTitle.text.toString()
        deal.description = txtDescription.text.toString()
        deal.price = txtPrice.text.toString()

        if (deal.id == ""){
            deal.apply {
                val key = databaseReference.child("traveldeal").push().key
                id = key!!
            }
            databaseReference.child(deal.id).setValue(deal)
        }
        else
            databaseReference.child(deal.id).setValue(deal)
    }

    private fun deleteDeal() {
        if (deal.id == "")
            Toast.makeText(this, "Please save deal first before deleting", Toast.LENGTH_LONG).show()
        databaseReference.child(deal.id).removeValue()
        Log.d("Image name", deal.imageName)
        if (deal.imageName != "" && deal.imageName.isNotEmpty()) {
            val picRef = FirebaseUtil.storage.reference.child(deal.imageName)
            picRef.delete().addOnSuccessListener {
                Log.d("Delete Image", "Image Successfully deleted")
            }
                .addOnFailureListener {
                    Log.d("Delete Image", it.message)
                }
        }
    }

    private fun clean() {
        txtTitle.setText("")
        txtDescription.setText("")
        txtPrice.setText("")
    }

    private fun enableEditText(isEnabled: Boolean) {
        txtPrice.isEnabled = isEnabled
        txtDescription.isEnabled = isEnabled
        txtTitle.isEnabled = isEnabled
        btnImage.isEnabled = isEnabled
    }

    private fun showImage(url: String?) {
        if ((url != null || url != "") && url?.isEmpty() == false) {
            val width = resources.displayMetrics.widthPixels
            Picasso.get().load(url).resize(width, width * 2/3)
                .centerCrop()
                .into(image)
        }
    }
}

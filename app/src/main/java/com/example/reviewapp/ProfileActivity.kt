package com.example.reviewapp


import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*


class ProfileActivity : AppCompatActivity() {

    private var mAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private val userID = mAuth.currentUser!!.uid
    private val docReference = db.document("users/$userID")
    private var selectedPhotoUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        this.title = "Account"

        val logOutBtn = findViewById<Button>(R.id.logout_btn)
        val deleteAccBtn = findViewById<Button>(R.id.delete_account_btn)

        retrieveUserData()
        selectImageFromDevice()

        logOutBtn.setOnClickListener {
            mAuth.signOut()
            Snackbar.make(it,
                R.string.singed_out,
                Snackbar.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        deleteAccBtn.setOnClickListener{
            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.dialogTitle))
                .setNegativeButton(resources.getString(R.string.decline)) { dialog, which ->
                    closeOptionsMenu()
                }
                .setPositiveButton(resources.getString(R.string.accept)) { dialog, which ->
                    docReference.delete()
                    mAuth.currentUser!!.delete()
                    Snackbar.make(it,
                    R.string.account_deleted,
                    Snackbar.LENGTH_SHORT).show()
                    val intent = Intent(this, RegisterActivity::class.java)
                    startActivity(intent)
                }
                .show()
        }
    }

    private fun selectImageFromDevice() {
        val changeProfilePhotoBtn = findViewById<Button>(R.id.upload_photo)
        val profilePhoto = findViewById<CircleImageView>(R.id.display_profile_photo)

        var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                selectedPhotoUri = data?.data
                var imageUri = selectedPhotoUri
                Glide.with(this).load(imageUri).into(profilePhoto)
                Log.d("ProfileActivity", "photo selected")
            } else {
                val view = findViewById<TextView>(R.id.snackbar_profile)
                Snackbar.make(view, R.string.image_not_seleted, Snackbar.LENGTH_SHORT).show()
            }
        }
        changeProfilePhotoBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            resultLauncher.launch(intent)
        }
    }

     fun uploadImageToStorage(view: View) {
        val filename = UUID.randomUUID().toString()
        var storageRef = FirebaseStorage.getInstance().getReference("images/$filename")
         val view = findViewById<TextView>(R.id.snackbar_profile)


         if (selectedPhotoUri != null) {
             val uploadTask = storageRef.putFile(selectedPhotoUri!!)
             uploadTask.continueWith {
                 if (!it.isSuccessful) {
                     it.exception?.let { t ->
                         throw t
                     }
                 }
                 storageRef.downloadUrl.addOnCompleteListener {
                     if (it.isSuccessful) {
                         it.addOnSuccessListener { task ->
                             var imageUrl = task.toString()
                             docReference.update("profilePhoto", imageUrl)
                             Snackbar.make(view,
                                 R.string.image_uploaded,
                                 Snackbar.LENGTH_SHORT).show()
                         }
                     }
                 }
             }
         } else {
             Snackbar.make(view,
                 R.string.no_updates,
                 Snackbar.LENGTH_SHORT).show()
         }
    }

    private fun retrieveUserData() {
        val emailTextView = findViewById<TextView>(R.id.display_email)
        val nameTextView = findViewById<TextView>(R.id.display_name)
        val profilePhoto = findViewById<CircleImageView>(R.id.display_profile_photo)
        docReference.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    nameTextView.text = document.getString("firstName")
                    emailTextView.text = document.getString("email")
                    if ( document.contains("profilePhoto")) {
                        Glide.with(this).load(document.getString("profilePhoto")).into(profilePhoto)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.overflow_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.option_home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.option_favorites -> {
                val intent = Intent(this, FavoritesActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.option_account -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.option_my_reviews -> {
                val intent = Intent(this, MyReviewsActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
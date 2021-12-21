package com.example.reviewapp

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.reviewapp.adapters.ReviewsAdapter
import com.example.reviewapp.models.ReviewsModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.*


class ReviewsActivity : AppCompatActivity() {

    private var mAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private val userID = mAuth.currentUser!!.uid
    private val docReference = db.document("users/$userID")
    private var selectedPhotoUri : Uri? = null

    private lateinit var reviewsAdapter: ReviewsAdapter
    private lateinit var recyclerView: RecyclerView
    private var reviewsList: MutableList<ReviewsModel> = ArrayList()

    private lateinit var dialog: Dialog
    private lateinit var restaurantName: String
    private lateinit var userName : String
    private lateinit var userPhoto: String
    private var imageUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reviews)
        this.title = "All Reviews"

        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)

        getUserInfo()
        extras()
        openReviewDialog()
        submitReview()
        showReviews()
        addToFavorites()
        checkIsFavorite()
    }

    private fun extras() {
        val extras = intent.extras
        if (extras == null) {

        } else {
            restaurantName = extras.getString("name").toString()
            val restaurantImage = extras.getString("image")

            val restaurantTextView = findViewById<TextView>(R.id.card_title2)
            val restaurantImageView = findViewById<ImageView>(R.id.card_image2)

            val restaurant = hashMapOf(
                "name" to restaurantName,
                "image" to restaurantImage,
            )

            //Add restaurant to database
            db.collection("restaurants")
                .document(restaurantName!!)
                .set(restaurant)
                .addOnSuccessListener {
                    Log.d("restaurant: ", "added successfully to db")
                }
                .addOnFailureListener {
                        e ->
                    Log.w(ContentValues.TAG, "Error adding document", e)
                }

            restaurantTextView.text = restaurantName

            Glide.with(this)
                .load(restaurantImage)
                .placeholder(R.drawable.ic_launcher_background)
                .into(restaurantImageView)
        }
    }

    private fun initRecyclerView() {
        recyclerView = findViewById<RecyclerView>(R.id.reviews_recycler_view)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ReviewsActivity)
            reviewsAdapter = ReviewsAdapter()
            adapter = reviewsAdapter
        }
    }

    // Adds the data to recycler view
    private fun addData(list: MutableList<ReviewsModel>) {
        reviewsAdapter.submitList(list)
    }

    private fun openReviewDialog() {
        val addReviewBtn = findViewById<Button>(R.id.add_review_btn)
        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.layout_custom_dialog)

        addReviewBtn.setOnClickListener {
            dialog.show()
        }
    }

    private fun showReviews() {
        val restaurantRef = db.document("restaurants/$restaurantName")

        restaurantRef.collection("reviews").addSnapshotListener{ value, e ->
            if( e != null) {
                Log.d("tag", "Listen failed.", e)
            } else {
                for (document in value!!) {
                    val userName = document.getString("userName").toString()
                    val userPhoto = document.getString("userPhoto").toString()
                    val review = document.getString("review").toString()
                    val rating = document.getString("rating").toString()
                    val location = document.getString("location").toString()
                    val image = document.getString("image").toString()

                    val list = ReviewsModel(userName, userPhoto, review, rating, location, image)
                    reviewsList.add(list)
                    initRecyclerView()
                    addData(reviewsList)
                }
            }
        }
    }

    private fun getUserInfo() {
        // get user's name and profile photo
        docReference.get()
            .addOnSuccessListener { document ->
                userName = document.getString("firstName").toString()
                if ( document.contains("profilePhoto")) {
                    userPhoto = document.getString("profilePhoto").toString()
                } else {
                    userPhoto = ""
                }
            } .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
    }

    private fun submitReview() {
        val submitReviewBtn = dialog.findViewById<Button>(R.id.submit_review_btn)
        val reviewText = dialog.findViewById<EditText>(R.id.review_text)
        val rating = dialog.findViewById<EditText>(R.id.rating_num)
        val location = dialog.findViewById<EditText>(R.id.location_text)
        selectImageFromDevice()

        submitReviewBtn.setOnClickListener {
            when {
                TextUtils.isEmpty(reviewText.text.toString()) -> {
                    Snackbar.make(it,
                        "Please enter review",
                        Snackbar.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(rating.text.toString()) -> {
                    Snackbar.make(it,
                        "Please enter rating",
                        Snackbar.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(location.text.toString()) -> {
                    Snackbar.make(it,
                        "Please enter location",
                        Snackbar.LENGTH_SHORT).show()
                }
             else -> {
                addReviewToDatabase()
                dialog.dismiss()
                 reviewText.text.clear()
                 rating.text.clear()
                 location.text.clear()
                }
            }
        }
    }

    fun uploadReviewImage(view: View) {
        val filename = UUID.randomUUID().toString()
        var storageRef = FirebaseStorage.getInstance().getReference("review-images/$filename")

        if (selectedPhotoUri != null) {
            Toast.makeText(this,R.string.wait, Toast.LENGTH_SHORT).show()
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
                            imageUrl = task.toString()
                            Log.d("image", imageUrl)
                            Toast.makeText(this,
                                R.string.image_uploaded,
                                Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        imageUrl = ""
                        Log.d("error", " error uploading image")
                    }
                }
            }
        } else {
            Toast.makeText(this,
                R.string.select_image,
                Toast.LENGTH_SHORT).show()
        }
    }


    private fun addReviewToDatabase() {
        val restaurantRef = db.document("restaurants/$restaurantName")
        val reviewText = dialog.findViewById<EditText>(R.id.review_text).text.toString()
        val rating = dialog.findViewById<EditText>(R.id.rating_num).text.toString()
        val location = dialog.findViewById<EditText>(R.id.location_text).text.toString()

        val review = hashMapOf(
            "userName" to userName,
            "userPhoto" to userPhoto,
            "review" to reviewText,
            "rating" to rating,
            "location" to location,
            "image" to imageUrl
        ) 

        // clear list before adding review to database
        reviewsList.clear()

        // Add review to restaurant's collection
        val reviewDoc = restaurantRef.collection("reviews").document()
            reviewDoc.set(review)
            .addOnSuccessListener {
                Log.d("review", "submitted successfully")
        }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
            }

        // Add review to user's collection
        docReference.collection("reviews")
            .document(reviewDoc.id)
            .set(review)
            .addOnSuccessListener {
                Log.d("review", "submitted successfully")
            }
            .addOnFailureListener {
                    e ->
                Log.w(ContentValues.TAG, "Error adding document", e)
            }
    }

    private fun addToFavorites() {
        val favoriteBtn = findViewById<ToggleButton>(R.id.favorite_btn)

        val favRestaurant = hashMapOf(
            "name" to restaurantName,
            "image" to intent.getStringExtra("image")
        )

        favoriteBtn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.favorite_filled,0)
                docReference.collection("favorites")
                    .document(restaurantName)
                    .set(favRestaurant)
                    .addOnSuccessListener {
                        Log.d("restaurant", "added to favorites")
                    }
                    .addOnFailureListener {
                            e ->
                        Log.w(ContentValues.TAG, "Error adding document", e)
                    }
            } else {
                favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.favorite_empty,0)
                docReference.collection("favorites")
                    .document(restaurantName)
                    .delete()
                    .addOnSuccessListener {
                        Log.d("restaurant", "removed from favorites")
                    }
                    .addOnFailureListener {
                            e ->
                        Log.w(ContentValues.TAG, "Error adding document", e)
                    }
            }
        }
    }

    // checks if restaurant is in favorites collection
    private fun checkIsFavorite() {
        val favoriteBtn = findViewById<ToggleButton>(R.id.favorite_btn)
        docReference.collection("favorites")
            .document(restaurantName)
            .get().addOnSuccessListener { document ->
                if (document.exists()) {
                    favoriteBtn.isChecked = true
                    favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.favorite_filled,0)
                } else {
                    favoriteBtn.isChecked = false
                    favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.favorite_empty,0)
                }
            } .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents.", exception)
            }
    }

    private fun selectImageFromDevice() {
        val selectImageBtn = dialog.findViewById<Button>(R.id.add_image_btn)
        val showSelectedImage = dialog.findViewById<ImageView>(R.id.show_selected_image)

        var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                selectedPhotoUri = data?.data
                var imageUri = selectedPhotoUri
                Glide.with(this).load(imageUri).into(showSelectedImage)
            } else {
                Toast.makeText(this, R.string.image_not_seleted, Toast.LENGTH_SHORT).show()
            }
        }
        selectImageBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            resultLauncher.launch(intent)
        }
    }
}
package com.example.reviewapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reviewapp.adapters.MyReviewsAdapter
import com.example.reviewapp.models.MyReviewsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.ArrayList

class MyReviewsActivity : AppCompatActivity() {

    private lateinit var myReviewsAdapter: MyReviewsAdapter
    private lateinit var myRecyclerView: RecyclerView
    private var myReviewsList: MutableList<MyReviewsModel> = ArrayList()

    private var mAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private val userID = mAuth.currentUser!!.uid
    private val docReference = db.document("users/$userID")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_reviews)
        this.title = "My reviews"
        showReviews()
    }

    private fun showReviews() {
        docReference.collection("reviews").addSnapshotListener{ value, e ->
            if( e != null) {
                Log.d("tag", "Listen failed.", e)
            } else {
                for (document in value!!) {
                    val review = document.getString("review").toString()
                    val rating = document.getString("rating").toString()
                    val location = document.getString("location").toString()
                    val image = document.getString("image").toString()

                    val list =  MyReviewsModel(review, rating, location, image)
                    myReviewsList.add(list)
                    initRecyclerView()
                    addData(myReviewsList)
                }
            }
        }
    }

    private fun initRecyclerView() {
        myRecyclerView = findViewById<RecyclerView>(R.id.my_reviews_recycler_view)
        myRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MyReviewsActivity)
            myReviewsAdapter = MyReviewsAdapter()
            adapter = myReviewsAdapter
        }
    }

    // Adds the data to recycler view
    private fun addData(list: MutableList<MyReviewsModel>) {
        myReviewsAdapter.submitList(list)
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
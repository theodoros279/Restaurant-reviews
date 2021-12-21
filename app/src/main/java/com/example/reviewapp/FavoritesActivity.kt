package com.example.reviewapp

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ToggleButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.reviewapp.adapters.FavoritesAdapter
import com.example.reviewapp.adapters.ReviewsAdapter
import com.example.reviewapp.models.FavoritesModel
import com.example.reviewapp.models.MyReviewsModel
import com.example.reviewapp.models.ReviewsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.ArrayList

class FavoritesActivity : AppCompatActivity() {

    private lateinit var favoritesAdapter: FavoritesAdapter
    private lateinit var recyclerView: RecyclerView
    private var favoritesList: MutableList<FavoritesModel> = ArrayList()

    private var mAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private val userID = mAuth.currentUser!!.uid
    private val docReference = db.document("users/$userID")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)
        this.title ="Favorites"
        showFavorites()
    }

    private fun showFavorites() {
        docReference.collection("favorites").addSnapshotListener{ value, e ->
            if( e != null) {
                Log.d("tag", "Listen failed.", e)
            } else {
                for (document in value!!) {
                    val name = document.getString("name").toString()
                    val image = document.getString("image").toString()
                    val list =  FavoritesModel(name, image)

                    favoritesList.add(list)
                    initRecyclerView()
                    addData(favoritesList)
                }
            }
        }
    }

    private fun initRecyclerView() {
        recyclerView = findViewById<RecyclerView>(R.id.favorites_recycler_view)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@FavoritesActivity)
            favoritesAdapter = FavoritesAdapter()
            adapter = favoritesAdapter
        }
    }

    // Adds the data to recycler view
    private fun addData(list: MutableList<FavoritesModel>) {
        favoritesAdapter.submitList(list)
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


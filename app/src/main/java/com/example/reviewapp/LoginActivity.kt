package com.example.reviewapp

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth




class LoginActivity : AppCompatActivity() {
    private var mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        this.setTitle("Sign In")

        val email = findViewById<TextInputEditText>(R.id.login_email)
        val password = findViewById<TextInputEditText>(R.id.login_password)
        val loginBtn = findViewById<Button>(R.id.login_btn)

        // start main activity if user is already logged in
        if (mAuth.currentUser != null && mAuth.currentUser!!.isEmailVerified) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        loginBtn.setOnClickListener {
            when {
                TextUtils.isEmpty(email.text.toString().trim(){it <= ' '}) -> {
                    Snackbar.make(it,
                        R.string.email_empty,
                        Snackbar.LENGTH_SHORT).show()
                }
                TextUtils.isEmpty(password.text.toString().trim(){it <= ' '}) -> {
                    Snackbar.make(it,
                        R.string.password_empty,
                        Snackbar.LENGTH_SHORT).show()
                }
                else -> {
                    mAuth.signInWithEmailAndPassword(email.text.toString().trim(), password.text.toString())
                        .addOnCompleteListener(this,
                            OnCompleteListener<AuthResult?> { task ->
                                if (!task.isSuccessful) {
                                    Snackbar.make(it,
                                        R.string.incorrect_credentials,
                                        Snackbar.LENGTH_SHORT).show()
                                } else if (!mAuth.currentUser!!.isEmailVerified) {
                                    Snackbar.make(it,
                                        R.string.verify_email,
                                        Snackbar.LENGTH_SHORT).show()
                                }
                                else {
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            })
                }
            }
        }
    }

    fun openResetPasswordActivity(view: View) {
        val intent = Intent(this, ResetPasswordActivity::class.java)
        startActivity(intent)
    }

    fun openRegisterActivity(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}


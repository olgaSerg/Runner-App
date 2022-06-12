package com.example.runnerapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.runnerapp.R
import com.example.runnerapp.fragments.LoginFragment
import com.example.runnerapp.fragments.RegistrationFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), RegistrationFragment.OnButtonLoginClickListener,
    LoginFragment.OnButtonRegistrationClickListener, LoginFragment.OnButtonLoginClickListener,
    RegistrationFragment.OnSignUpClickListener {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        val currentUser = auth.currentUser
        if (currentUser != null) {
            loadMainScreenActivity()
        }

        loadLoginFragment()
    }

    override fun onClickButtonLoginReference() {
        loadLoginFragment()
    }

    override fun onClickButtonRegistrationReference() {
        loadRegistrationFragment()
    }

    private fun loadRegistrationFragment() {
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left)
            replace(R.id.fragment_container, RegistrationFragment())
            commit()
        }
    }

    private fun loadLoginFragment() {
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.enter_left_to_right, R.anim.exit_left_to_right)
            replace(R.id.fragment_container, LoginFragment())
            commit()
        }
    }

    override fun onClickButtonLogin() {
        val intent = Intent(this, MainScreenActivity::class.java)
        startActivity(intent)
    }

    private fun loadMainScreenActivity() {
        val intent = Intent(this, MainScreenActivity::class.java)
        startActivity(intent)
    }

    override fun onSignUpClickListener() {
        val intent = Intent(this, MainScreenActivity::class.java)
        startActivity(intent)
    }
}
package com.example.runnerapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.runnerapp.fragments.LoginFragment
import com.example.runnerapp.fragments.RegistrationFragment

class MainActivity : AppCompatActivity(), RegistrationFragment.OnButtonLoginClickListener,
    LoginFragment.OnButtonRegistrationClickListener, LoginFragment.OnButtonLoginClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
}
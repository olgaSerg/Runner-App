package com.example.runnerapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.runnerapp.R
import com.example.runnerapp.State
import com.example.runnerapp.fragments.authentication.LoginFragment
import com.example.runnerapp.fragments.authentication.RegistrationFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

const val LOGIN = "login"
const val REGISTRATION = "registration"
const val STATE = "state"

class AuthActivity : AppCompatActivity(), RegistrationFragment.OnLoginLinkClickListener,
    LoginFragment.OnRegistrationLinkClick, LoginFragment.OnLoginButtonClickListener,
    RegistrationFragment.OnSignUpClickListener {

    private lateinit var auth: FirebaseAuth
    private var state = State()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        // TODO: Remove test credentials
        state.email = "olga@gmail.com"
        state.password = "123123"

        if (savedInstanceState != null) {
            state = savedInstanceState.getSerializable(STATE) as State
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            loadMainScreenActivity()
        } else {
            displayState()
        }
    }

    override fun onLoginLinkClick() {
        state.fragment = LOGIN
        displayState()
    }

    override fun onRegistrationLinkClick() {
        state.fragment = REGISTRATION
        displayState()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(STATE, state)
    }

    private fun loadRegistrationFragment(state:State) {
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left)
            replace(R.id.fragment_container, RegistrationFragment.newInstance(state))
            commit()
        }
    }

    private fun loadLoginFragment(state: State) {
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.enter_left_to_right, R.anim.exit_left_to_right)
            replace(R.id.fragment_container, LoginFragment.newInstance(state))
            commit()
        }
    }

    override fun onLoginButtonClick() {
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

    private fun displayState() {
        when (state.fragment) {
            LOGIN -> loadLoginFragment(state)
            REGISTRATION-> loadRegistrationFragment(state)
        }
    }
}
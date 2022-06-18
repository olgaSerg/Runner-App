package com.example.runnerapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.runnerapp.R
import com.example.runnerapp.State
import com.example.runnerapp.fragments.LoginFragment
import com.example.runnerapp.fragments.RegistrationFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

const val LOGIN = "login"
const val REGISTRATION = "registration"
const val STATE = "state"


class MainActivity : AppCompatActivity(), RegistrationFragment.OnButtonLoginClickListener,
    LoginFragment.OnButtonRegistrationClickListener, LoginFragment.OnButtonLoginClickListener,
    RegistrationFragment.OnSignUpClickListener {

    private lateinit var auth: FirebaseAuth
    private var state = State()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        if (savedInstanceState != null) {
            state = savedInstanceState.getSerializable(STATE) as State
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            loadMainScreenActivity()
        } else {
            restoreState()
        }
    }

    override fun onClickButtonLoginReference() {
        state = State()
        loadLoginFragment(state)
    }

    override fun onClickButtonRegistrationReference() {
        state = State()
        loadRegistrationFragment(state)
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
        state.fragment = REGISTRATION
    }

    private fun loadLoginFragment(state: State) {
        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.enter_left_to_right, R.anim.exit_left_to_right)
            replace(R.id.fragment_container, LoginFragment.newInstance(state))
            commit()
        }
        state.fragment = LOGIN
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

    private fun restoreState() {
        when (state.fragment) {
            LOGIN -> loadLoginFragment(state)
            REGISTRATION-> loadRegistrationFragment(state)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}
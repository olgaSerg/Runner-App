package com.example.runnerapp.fragments.authentication

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.runnerapp.R
import com.example.runnerapp.State
import com.example.runnerapp.activities.STATE
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginFragment : Fragment(R.layout.fragment_login) {

    private var email: TextInputLayout? = null
    private var password: TextInputLayout? = null
    private var loginButton: Button? = null
    private var registrationLink: Button? = null
    private var registrationLinkClick: OnRegistrationLinkClick? = null
    private var loginButtonClickListener: OnLoginButtonClickListener? = null
    private val auth = Firebase.auth
    private var hasError = false
    private var state: State? = null

    interface OnRegistrationLinkClick {
        fun onRegistrationLinkClick()
    }

    interface OnLoginButtonClickListener {
        fun onLoginButtonClick()
    }

    companion object {
        fun newInstance(state: State): LoginFragment {
            val args = Bundle()
            args.putSerializable(STATE, state)
            val loginFragment = LoginFragment()
            loginFragment.arguments = args
            return loginFragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        registrationLinkClick = try {
            activity as OnRegistrationLinkClick
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnRegistrationLinkClick")
        }

        loginButtonClickListener = try {
            activity as OnLoginButtonClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnLoginButtonClickListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        email = view.findViewById(R.id.email_login)
        password = view.findViewById(R.id.password_login)
        loginButton = view.findViewById(R.id.button_login_screen)
        registrationLink = view.findViewById(R.id.button_registration_login_screen)

        val registrationLink = registrationLink ?: return
        val email = email ?: return
        val password = password ?: return
        val loginButton = loginButton ?: return
        state = arguments?.getSerializable(STATE) as State
        val state = state ?: return

        displayState()

        setButtonLoginClickListener(email, password)

        registrationLink.setOnClickListener {
            registrationLinkClick?.onRegistrationLinkClick()
        }

        if (state.isTaskLoginStarted) {
            loginButton.callOnClick()
        }
    }

    private fun displayState() {
        val state = state ?: return
        val email = email ?: return
        val password = password ?: return
        val loginButton = loginButton ?: return

        email.editText?.setText(state.email)
        password.editText?.setText(state.password)

        loginButton.isEnabled = !state.isTaskLoginStarted
    }

    private fun setButtonLoginClickListener(email: TextInputLayout, password: TextInputLayout) {

        val loginButton = loginButton ?: return

        loginButton.setOnClickListener {
            resetErrors()
            val state = state ?: return@setOnClickListener
            state.isTaskLoginStarted = true
            loginButton.isEnabled = false
            state.email = email.editText?.text.toString()
            state.password = password.editText?.text.toString()

            if (state.email.isEmpty()) {
                email.error = getString(R.string.error_empty_field)
                hasError = true
            }

            if (state.password.isEmpty()) {
                password.error = getString(R.string.error_empty_field)
                hasError = true
            }
            if (hasError) {
                state.isTaskLoginStarted = false
                displayState()
                return@setOnClickListener
            }
            loginUser()
        }
    }

    private fun resetErrors() {
        email?.error = null
        password?.error = null
        hasError = false
    }

    private fun loginUser() {
        val state = state ?: return
        val email = email ?: return
        val password = password ?: return
        val loginButton = loginButton ?: return

        auth.signInWithEmailAndPassword(
            email.editText?.text.toString(),
            password.editText?.text.toString()
        ).addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithEmail:success")
                loginButtonClickListener?.onLoginButtonClick()
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithEmail:failure", task.exception)
                Toast.makeText(
                    requireContext(), "Authentication failed.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            state.isTaskLoginStarted = false
            loginButton.isEnabled = true
        }
    }

    override fun onPause() {
        super.onPause()
        val state = state ?: return
        state.email = email?.editText?.text.toString()
        state.password = password?.editText?.text.toString()
    }

    override fun onDestroyView() {
        email= null
        password = null
        loginButton = null
        registrationLink = null
        registrationLinkClick = null
        loginButtonClickListener = null
        super.onDestroyView()
    }
}

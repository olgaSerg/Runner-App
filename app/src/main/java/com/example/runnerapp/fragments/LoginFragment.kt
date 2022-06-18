package com.example.runnerapp.fragments

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.runnerapp.R
import com.example.runnerapp.State
import com.example.runnerapp.activities.STATE
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginFragment : Fragment(R.layout.fragment_login) {

    private var email: TextInputLayout? = null
    private var password: TextInputLayout? = null
    private var buttonLogin: Button? = null
    private var buttonRegistration: Button? = null
    private var buttonRegistrationClickListener: OnButtonRegistrationClickListener? = null
    private var buttonLoginClickListener: OnButtonLoginClickListener? = null
    private lateinit var auth: FirebaseAuth
    private var hasError = false
    private var state: State? = null

    interface OnButtonRegistrationClickListener {
        fun onClickButtonRegistrationReference()
    }

    interface OnButtonLoginClickListener {
        fun onClickButtonLogin()
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
        buttonRegistrationClickListener = try {
            activity as OnButtonRegistrationClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnButtonRegistrationClickListener")
        }

        buttonLoginClickListener = try {
            activity as OnButtonLoginClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnButtonLoginClickListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        email = view.findViewById(R.id.email_login)
        password = view.findViewById(R.id.password_login)
        buttonLogin = view.findViewById(R.id.button_login_screen)
        buttonRegistration = view.findViewById(R.id.button_registration_login_screen)

        val buttonRegistration = buttonRegistration ?: return
        val email = email ?: return
        val password = password ?: return
        val buttonLogin = buttonLogin ?: return
        state = arguments?.getSerializable(STATE) as State
        val state = state ?: return

        displayState(email, password)

        auth = Firebase.auth

        if (state.isTaskLoginStarted) {
            buttonLogin.callOnClick()
            buttonLogin.isEnabled = false
        }

        state.isTaskRegistrationStarted = !buttonLogin.isEnabled

        setButtonLoginClickListener(email, password)

        buttonRegistration.setOnClickListener {
            buttonRegistrationClickListener?.onClickButtonRegistrationReference()
        }
    }

    private fun displayState(email: TextInputLayout, password: TextInputLayout) {
        val state = state ?: return
        email.editText?.setText(state.email)
        password.editText?.setText(state.password)
    }

    private fun setButtonLoginClickListener(email: TextInputLayout, password: TextInputLayout) {

        val buttonLogin = buttonLogin ?: return

        buttonLogin.setOnClickListener {
            state?.isTaskLoginStarted = true
            buttonLogin.isEnabled = false
            state?.email = email.editText?.text.toString()
            state?.password = password.editText?.text.toString()

            if (findEmptyField(state?.email)) {
                email.editText?.error = getString(R.string.error_empty_field)
                hasError = true
            }

            if (findEmptyField(state?.password)) {
                password.editText?.error = getString(R.string.error_empty_field)
                hasError = true
            }
            if (hasError){
                state?.isTaskLoginStarted = false
                buttonLogin.isEnabled = true
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(
                email.editText?.text.toString(),
                password.editText?.text.toString()
            )
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        buttonLoginClickListener?.onClickButtonLogin()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            requireContext(), "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    state?.isTaskLoginStarted = false
                    buttonLogin.isEnabled = true
                }
        }
    }

    private fun findEmptyField(field: String?): Boolean {
        return field!!.isEmpty()
    }

    override fun onPause() {
        super.onPause()
        val state = state ?: return
        state.email = email?.editText?.text.toString()
        state.password = password?.editText?.text.toString()
    }
}

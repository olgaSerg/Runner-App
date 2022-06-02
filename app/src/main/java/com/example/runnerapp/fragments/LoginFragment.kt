package com.example.runnerapp.fragments

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.runnerapp.R
import com.example.runnerapp.models.ProfileLoginModel
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

    interface OnButtonRegistrationClickListener {
        fun onClickButtonRegistrationReference()
    }

    interface OnButtonLoginClickListener {
        fun onClickButtonLogin()
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

        auth = Firebase.auth

        setButtonLoginClickListener()

        buttonRegistration.setOnClickListener {
            buttonRegistrationClickListener?.onClickButtonRegistrationReference()
        }
    }

    private fun setButtonLoginClickListener() {
        val email = email ?: return
        val password = password ?: return
        val buttonLogin = buttonLogin ?: return

        buttonLogin.setOnClickListener {
            val profile = ProfileLoginModel()
            profile.email = email.editText?.text.toString()
            profile.password = password.editText?.text.toString()

            if (findEmptyField(profile.email)) {
                email.editText?.error = getString(R.string.error_empty_field)
            }

            if (findEmptyField(profile.password)) {
                password.editText?.error = getString(R.string.error_empty_field)
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
                }
        }
    }

    private fun findEmptyField(field: String?): Boolean {
        return field!!.isEmpty()
    }
}

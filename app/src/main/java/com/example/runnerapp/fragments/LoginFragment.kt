package com.example.runnerapp.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import com.example.runnerapp.R
import com.example.runnerapp.models.ProfileLoginModel
import com.google.android.material.textfield.TextInputLayout

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var email: TextInputLayout? = null
    private var password: TextInputLayout? = null
    private var buttonLogin: Button? = null
    private var buttonRegistration: Button? = null
    private var buttonRegistrationClickListener : OnButtonRegistrationClickListener? = null
    private var buttonLoginClickListener: OnButtonLoginClickListener? = null

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

        val email = email ?: return
        val password = password ?: return
        val buttonLogin = buttonLogin ?: return
        val buttonRegistration = buttonRegistration ?: return

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
            buttonLoginClickListener?.onClickButtonLogin()

        }

        buttonRegistration.setOnClickListener {
            buttonRegistrationClickListener?.onClickButtonRegistrationReference()
        }
    }

    private fun findEmptyField(field: String?): Boolean {
        return field!!.isEmpty()
    }
}
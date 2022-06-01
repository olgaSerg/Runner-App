package com.example.runnerapp.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import com.example.runnerapp.R
import com.example.runnerapp.models.ProfileRegistrationModel
import com.google.android.material.textfield.TextInputLayout

class RegistrationFragment : Fragment(R.layout.fragment_registration) {

    private var email: TextInputLayout? = null
    private var firstName: TextInputLayout? = null
    private var lastName: TextInputLayout? = null
    private var password: TextInputLayout? = null
    private var confirmPassword: TextInputLayout? = null
    private var buttonRegistration: Button? = null
    private var buttonLogin: Button? = null
    private var buttonLoginClickListener: OnButtonLoginClickListener? = null

    interface OnButtonLoginClickListener {
        fun onClickButtonLoginReference()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        buttonLoginClickListener = try {
            activity as OnButtonLoginClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnButtonLoginClickListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeFields(view)

        val buttonLogin = buttonLogin ?: return

        setButtonRegistrationClickListener()
        buttonLogin.setOnClickListener {
            buttonLoginClickListener?.onClickButtonLoginReference()
        }
    }

    private fun initializeFields(view: View) {
        email = view.findViewById(R.id.email_registration)
        firstName = view.findViewById(R.id.first_name_registration)
        lastName = view.findViewById(R.id.last_name_registration)
        password = view.findViewById(R.id.password_registration)
        confirmPassword = view.findViewById(R.id.confirm_password)
        buttonRegistration = view.findViewById(R.id.button_registration)
        buttonLogin = view.findViewById(R.id.button_login_registration_screen)
    }

    private fun setButtonRegistrationClickListener() {
        val buttonRegistration = buttonRegistration ?: return
        val email = email ?: return
        val firstName = firstName ?: return
        val lastName = lastName ?: return
        val password = password ?: return
        val confirmPassword = confirmPassword ?: return
        val list = listOf(email, firstName, lastName, password, confirmPassword)

        buttonRegistration.setOnClickListener {
            val validationResults = listOf(
                checkPasswordsMatch(password, confirmPassword),
                checkFields(list),
            )

            val isFormValid = validationResults.all { it }

            if (isFormValid) {
                fillProfile(email, firstName, lastName, password)
            }
        }
    }

    private fun checkPasswordsMatch(
        password: TextInputLayout,
        passwordConfirmation: TextInputLayout
    ): Boolean {
        if (password.editText.toString() != passwordConfirmation.editText.toString()) {
            password.error = "Пароли не совпадают"
            passwordConfirmation.error = "Пароли не совпадают"
            return false
        }
        return true
    }

    private fun checkFields(list: List<TextInputLayout>): Boolean {
        var allFilled = true
        for (each in list) {
            if (each.editText?.text?.isEmpty() == true) {
                each.error = "Заполните пустое поле"
                allFilled = false
            }
        }
        return allFilled
    }

    private fun fillProfile(
        email: TextInputLayout,
        firstName: TextInputLayout,
        lastName: TextInputLayout,
        password: TextInputLayout
    ) {
        val profileRegistrationModel = ProfileRegistrationModel()
        profileRegistrationModel.email = email.editText?.text.toString()
        profileRegistrationModel.firstName = firstName.editText?.text.toString()
        profileRegistrationModel.lastName = lastName.editText?.text.toString()
        profileRegistrationModel.password = password.editText?.text.toString()
    }
}
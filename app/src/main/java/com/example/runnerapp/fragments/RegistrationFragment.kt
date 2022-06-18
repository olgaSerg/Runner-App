package com.example.runnerapp.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.Constraints.TAG
import com.example.runnerapp.R
import com.example.runnerapp.State
import com.example.runnerapp.activities.STATE
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegistrationFragment : Fragment(R.layout.fragment_registration) {

    private var email: TextInputLayout? = null
    private var firstName: TextInputLayout? = null
    private var lastName: TextInputLayout? = null
    private var password: TextInputLayout? = null
    private var confirmPassword: TextInputLayout? = null
    private var buttonRegistration: Button? = null
    private var buttonLogin: Button? = null
    private var buttonLoginClickListener: OnButtonLoginClickListener? = null
    private lateinit var auth: FirebaseAuth
    private var state: State? = null
    private var signUpClickListener: OnSignUpClickListener? = null

    interface OnButtonLoginClickListener {
        fun onClickButtonLoginReference()
    }

    interface OnSignUpClickListener {
        fun onSignUpClickListener()
    }

    companion object {
        fun newInstance(state: State): RegistrationFragment {
            val args = Bundle()
            args.putSerializable(STATE, state)
            val registrationFragment = RegistrationFragment()
            registrationFragment.arguments = args
            return registrationFragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        buttonLoginClickListener = try {
            activity as OnButtonLoginClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnButtonLoginClickListener")
        }

        signUpClickListener = try {
            activity as OnSignUpClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnSignUpClickListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeFields(view)

        state = arguments?.getSerializable(STATE) as State

        val buttonLogin = buttonLogin ?: return
        val state = state ?: return

        displayState()

        if (state.isTaskRegistrationStarted) {
            buttonRegistration?.callOnClick()
        }

        state.isTaskRegistrationStarted = !buttonLogin.isEnabled

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
            state?.isTaskRegistrationStarted = true
            buttonRegistration.isEnabled = false
            val validationResults = listOf(
                checkPasswordsMatch(password, confirmPassword),
                checkFields(list),
            )

            val isFormValid = validationResults.all { it }

            if (isFormValid) {
                createUser(email)
            } else {
                state?.isTaskRegistrationStarted = false
                buttonRegistration.isEnabled = true
            }
        }
    }

    private fun checkPasswordsMatch(
        password: TextInputLayout,
        passwordConfirmation: TextInputLayout
    ): Boolean {
        if (password.editText?.text.toString() != passwordConfirmation.editText?.text.toString()) {
            setPasswordError(password, passwordConfirmation)
            return false
        }
        return true
    }

    private fun setPasswordError(password: TextInputLayout, passwordConfirmation: TextInputLayout) {
        password.error = "Пароли не совпадают"
        passwordConfirmation.error = "Пароли не совпадают"
    }

    private fun checkFields(list: List<TextInputLayout>): Boolean {
        var allFilled = true
        for (field in list) {
            if (field.editText?.text?.isEmpty() == true) {
                setFieldError(field)
                allFilled = false
            }
        }
        return allFilled
    }

    private fun setFieldError(field: TextInputLayout) {
        field.error = "Заполните пустое поле"
    }

    private fun createUser(email: TextInputLayout) {
        auth = Firebase.auth

        auth.createUserWithEmailAndPassword(
            email.editText?.text.toString(),
            password?.editText?.text.toString()
        )
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    signUpClickListener?.onSignUpClickListener()
                    Log.d(TAG, "createUserWithEmail:success")
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(requireContext(), "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
                state?.isTaskRegistrationStarted = false
                buttonRegistration?.isEnabled = true
            }
    }

    override fun onPause() {
        super.onPause()
        val state = state ?: return
        state.email = email?.editText?.text.toString()
        state.password = password?.editText?.text.toString()
        state.confirmPassword = confirmPassword?.editText?.text.toString()
        state.firstName = firstName?.editText?.text.toString()
        state.lastName = lastName?.editText?.text.toString()
    }

    private fun displayState() {
        val state = state ?: return
        email?.editText?.setText(state.email)
        password?.editText?.setText(state.password)
        confirmPassword?.editText?.setText(state.confirmPassword)
        firstName?.editText?.setText(state.firstName)
        lastName?.editText?.setText(state.lastName)
    }
}
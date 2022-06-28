package com.example.runnerapp.fragments.authentication

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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegistrationFragment : Fragment(R.layout.fragment_registration) {

    private var email: TextInputLayout? = null
    private var firstName: TextInputLayout? = null
    private var lastName: TextInputLayout? = null
    private var password: TextInputLayout? = null
    private var passwordConfirmation: TextInputLayout? = null
    private var buttonRegistration: Button? = null
    private var loginButton: Button? = null
    private var loginLinkClickListener: OnLoginLinkClickListener? = null
    private var auth = Firebase.auth
    private var state: State? = null
    private var signUpClickListener: OnSignUpClickListener? = null

    interface OnLoginLinkClickListener {
        fun onLoginLinkClick()
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

        loginLinkClickListener =
            activity as? OnLoginLinkClickListener
//        } catch (e: ClassCastException) {
//            throw ClassCastException("$activity must implement OnLoginLinkClickListener")
//        }

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

        val loginButton = loginButton ?: return
        val state = state ?: return

        displayState()

        setButtonRegistrationClickListener()
        loginButton.setOnClickListener {
            loginLinkClickListener?.onLoginLinkClick()
        }

        if (state.isTaskRegistrationStarted) {
            buttonRegistration?.callOnClick()
        }
    }

    private fun initializeFields(view: View) {
        email = view.findViewById(R.id.email_registration)
        firstName = view.findViewById(R.id.first_name_registration)
        lastName = view.findViewById(R.id.last_name_registration)
        password = view.findViewById(R.id.password_registration)
        passwordConfirmation = view.findViewById(R.id.confirm_password)
        buttonRegistration = view.findViewById(R.id.button_registration)
        loginButton = view.findViewById(R.id.button_login_registration_screen)
    }

    private fun setButtonRegistrationClickListener() {
        val buttonRegistration = buttonRegistration ?: return
        val email = email ?: return
        val firstName = firstName ?: return
        val lastName = lastName ?: return
        val password = password ?: return
        val passwordConfirmation = passwordConfirmation ?: return
        val fields = listOf(email, firstName, lastName, password, passwordConfirmation)

        buttonRegistration.setOnClickListener {
            state?.isTaskRegistrationStarted = true
            buttonRegistration.isEnabled = false
            val validationResults = listOf(
                checkPasswordsMatch(password, passwordConfirmation),
                checkFields(fields),
            )

            val isFormValid = validationResults.all { it }

            if (isFormValid) {
                registerUser()
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
        password.error = getString(R.string.passwords_not_match)
        passwordConfirmation.error = getString(R.string.passwords_not_match)
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
        field.error = getString(R.string.fill_empty_field)
    }

    private fun registerUser() {
        val state = state ?: return

        auth = Firebase.auth

        auth.createUserWithEmailAndPassword(
            email?.editText?.text.toString(),
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
                state.isTaskRegistrationStarted = false
                buttonRegistration?.isEnabled = true
            }
    }

    override fun onPause() {
        super.onPause()
        val state = state ?: return
        state.email = email?.editText?.text.toString()
        state.password = password?.editText?.text.toString()
        state.passwordConfirmation = passwordConfirmation?.editText?.text.toString()
        state.firstName = firstName?.editText?.text.toString()
        state.lastName = lastName?.editText?.text.toString()
    }

    private fun displayState() {
        val state = state ?: return
        val loginButton = loginButton ?: return
        state.isTaskRegistrationStarted = !loginButton.isEnabled
        email?.editText?.setText(state.email)
        password?.editText?.setText(state.password)
        passwordConfirmation?.editText?.setText(state.passwordConfirmation)
        firstName?.editText?.setText(state.firstName)
        lastName?.editText?.setText(state.lastName)
    }
}
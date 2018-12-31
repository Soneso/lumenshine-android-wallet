package com.soneso.lumenshine.presentation.widgets

import android.content.Context
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import com.soneso.lumenshine.R
import kotlinx.android.synthetic.main.ls_input_view.view.*

class PasswordInputView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FormInputView(context, attrs, defStyleAttr) {

    init {
        editTextView.setSingleLine()
        editTextView.transformationMethod = PasswordTransformationMethod.getInstance()
    }

    private val minPasswordLength = 9

    fun isValidPassword(): Boolean {
        when {
            inputLevel == resources.getInteger(R.integer.input_mandatory) && editTextView.text.isNullOrBlank() -> {
                error = resources.getText(R.string.error_field_required)
                return false
            }

            !trimmedText.matches(Regex(".*[A-Z].*")) -> {
                error = resources.getText(R.string.error_invalid_password_min_one_upper_case_char)
                return false
            }

            !trimmedText.matches(Regex(".*[a-z].*")) -> {
                error = resources.getText(R.string.error_invalid_password_min_one_lower_case_char)
                return false
            }

            !trimmedText.matches(Regex(".*\\d.*")) -> {
                error = resources.getText(R.string.error_invalid_password_min_one_digit)
                return false
            }

            trimmedText.length < minPasswordLength -> {
                error = resources.getText(R.string.error_invalid_password_min_nine_characters)
                return false
            }
        }
        return true
    }
}
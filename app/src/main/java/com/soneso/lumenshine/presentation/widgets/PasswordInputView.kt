package com.soneso.lumenshine.presentation.widgets

import android.content.Context
import android.graphics.Typeface
import android.text.InputType
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.soneso.lumenshine.R
import kotlinx.android.synthetic.main.ls_input_view.view.*

class PasswordInputView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FormInputView(context, attrs, defStyleAttr) {

    private var showPasswordToggle = false
    private var passwordTypeFace: Typeface? = Typeface.DEFAULT

    init {
        if (!isInEditMode) {
            passwordTypeFace = ResourcesCompat.getFont(context, R.font.encodesans_regular)
        }
        applyAttrs(attrs)
        setupListeners()
    }

    private fun setupListeners() {
        passwordToggleDrawable.setOnClickListener { changePasswordVisibility() }
    }

    private fun applyAttrs(attrs: AttributeSet?) {
        val attributeSet = attrs ?: return
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.PasswordInputView)
        showPasswordToggle = typedArray.getBoolean(R.styleable.PasswordInputView_showPasswordToggle, false)
        val drawableEnd = typedArray.getDrawable(R.styleable.FormInputView_android_drawableEnd)

        val layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        var bottomPadding = 0

        if (isInEditMode) {
            editTextEndDrawable.setImageDrawable(drawableEnd)
            editTextView.layoutParams = layoutParams
        } else {
            if (editTextView.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                editTextView.typeface = passwordTypeFace
            }

            var padding = 0
            if (drawableEnd != null) {
                padding += resources.getDimensionPixelOffset(R.dimen.size_40)
                editTextEndDrawable.visibility = View.VISIBLE
                editTextEndDrawable.setImageDrawable(drawableEnd)
            } else {
                editTextEndDrawable.visibility = View.GONE
            }
            if (showPasswordToggle) {
                padding += resources.getDimensionPixelOffset(R.dimen.size_45)
                passwordToggleDrawable.setImageResource(R.drawable.ic_visibility_states)
                passwordToggleDrawable.visibility = View.VISIBLE
            } else {
                passwordToggleDrawable.visibility = View.GONE
                if (drawableEnd == null) {
                    editTextView.layoutParams = layoutParams
                    bottomPadding = editTextView.paddingBottom
                }
            }
            editTextView.setPadding(0, 0, padding, bottomPadding)
        }


        val imeActionId = typedArray.getInt(R.styleable.FormInputView_android_imeActionId, 0)
        editTextView.setImeActionLabel(typedArray.getString(R.styleable.FormInputView_android_imeActionLabel), imeActionId)
        typedArray.recycle()
    }

    private fun changePasswordVisibility() {
        passwordToggleDrawable.isSelected = !passwordToggleDrawable.isSelected
        if (passwordToggleDrawable.isSelected) {
            passwordToggleDrawable.setImageResource(R.drawable.ic_visibility)
            editTextView.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            passwordToggleDrawable.setImageResource(R.drawable.ic_visibility_off)
            editTextView.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        editTextView.typeface = passwordTypeFace

        editTextView.setSelection(editTextView.text.length)
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
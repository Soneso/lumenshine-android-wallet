package com.soneso.lumenshine.presentation.widgets

import android.content.Context
import android.text.Editable
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.soneso.lumenshine.R
import com.soneso.lumenshine.presentation.util.setOnTextChangeListener
import kotlinx.android.synthetic.main.ls_input_view.view.*
import android.text.InputType


open class FormInputView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    protected var inputLevel = 0
    private var errorText: CharSequence = ""
    private var regexToMatch = ""
    private var showPasswordToggle: Boolean = false
    var onDrawableEndClickListener: (() -> Unit)? = null

    var trimmedText: CharSequence
        get() = editTextView.text?.trim() ?: ""
        set(value) {
            editTextView.text = SpannableStringBuilder(value)
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.ls_input_view, this, true)
        orientation = VERTICAL
        applyAttrs(attrs)
        editTextView.maxLines = 1

        setupInputLevel()
        setupListeners()
    }

    private fun setupListeners() {
        editTextView.setOnTextChangeListener { errorTextView.text = "" }
        editTextEndDrawable.setOnClickListener { onDrawableEndClickListener?.invoke() }
        passwordToggleDrawable.setOnClickListener { changePasswordVisibility() }
    }


    private fun applyAttrs(attrs: AttributeSet?) {
        val attributeSet = attrs ?: return
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.FormInputView)
        inputLevel = typedArray.getInt(R.styleable.FormInputView_input_level, 0)
        regexToMatch = typedArray.getString(R.styleable.FormInputView_regex) ?: ""
        errorText = typedArray.getString(R.styleable.FormInputView_error_text) ?: resources.getText(R.string.invalid)
        showPasswordToggle = typedArray.getBoolean(R.styleable.FormInputView_showPasswordToggle, false)
        val inputType = typedArray.getInt(R.styleable.FormInputView_android_inputType, EditorInfo.TYPE_NULL)
        if (inputType != EditorInfo.TYPE_NULL) {
            editTextView.inputType = inputType
        }
        val hint = typedArray.getString(R.styleable.FormInputView_android_hint)
        editTextView.hint = hint

        editTextView.imeOptions = typedArray.getInt(R.styleable.FormInputView_android_imeOptions, EditorInfo.IME_ACTION_UNSPECIFIED)
        val drawableEnd = typedArray.getDrawable(R.styleable.FormInputView_android_drawableEnd)

        val layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        var bottomPadding = 0

        if (isInEditMode) {
            editTextEndDrawable.setImageDrawable(drawableEnd)
            editTextView.layoutParams = layoutParams
        } else {
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

    private fun setupInputLevel() {
        if (inputLevel == resources.getInteger(R.integer.input_undesired)) {
            visibility = View.GONE
        }
    }

    fun hasValidInput(): Boolean {
        when {
            inputLevel == resources.getInteger(R.integer.input_mandatory) && editTextView.text.isNullOrBlank() -> {
                errorTextView.text = resources.getText(R.string.error_field_required)
                return false
            }
            inputLevel == resources.getInteger(R.integer.input_optional) && editTextView.text.isNullOrBlank() -> {
                return true
            }

            regexToMatch.isNotEmpty() && !trimmedText.matches(Regex(regexToMatch)) -> {
                errorTextView.text = errorText
            }
        }
        return true
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
        editTextView.setSelection(editTextView.text.length)
    }

    fun setOnEditorActionListener(listener: TextView.OnEditorActionListener) {
        editTextView.setOnEditorActionListener(listener)
    }

    fun setSelection(index: Int) {
        editTextView.setSelection(index)
    }

    var error: CharSequence
        get() = errorTextView.text
        set(value) {
            errorTextView.text = value
        }

    val text: Editable
        get() {
            return editTextView.text
        }

}
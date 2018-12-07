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
import com.soneso.lumenshine.R
import com.soneso.lumenshine.presentation.util.setOnTextChangeListener
import kotlinx.android.synthetic.main.ls_input_view.view.*


open class FormInputView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    protected var inputLevel = 0
    private var errorText: CharSequence = ""
    private var regexToMatch = ""
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
    }


    private fun applyAttrs(attrs: AttributeSet?) {
        val attributeSet = attrs ?: return
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.FormInputView)
        inputLevel = typedArray.getInt(R.styleable.FormInputView_input_level, 0)
        regexToMatch = typedArray.getString(R.styleable.FormInputView_regex) ?: ""
        errorText = typedArray.getString(R.styleable.FormInputView_error_text) ?: resources.getText(R.string.invalid)

        val drawableEnd = typedArray.getDrawable(R.styleable.FormInputView_android_drawableEnd)
        editTextEndDrawable.setImageDrawable(drawableEnd)
        if (drawableEnd != null) {
            editTextEndDrawable.visibility = View.VISIBLE
        }

        val inputType = typedArray.getInt(R.styleable.FormInputView_android_inputType, EditorInfo.TYPE_NULL)
        if (inputType != EditorInfo.TYPE_NULL) {
            editTextView.inputType = inputType
        }
        val hint = typedArray.getString(R.styleable.FormInputView_android_hint)
        editTextView.hint = hint

        val textAllCaps = typedArray.getBoolean(R.styleable.FormInputView_android_textAllCaps, false)
        editTextView.isAllCaps = textAllCaps

        editTextView.imeOptions = typedArray.getInt(R.styleable.FormInputView_android_imeOptions, EditorInfo.IME_ACTION_UNSPECIFIED)

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
                return false
            }
        }
        return true
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
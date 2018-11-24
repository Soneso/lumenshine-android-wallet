package com.soneso.lumenshine.presentation.auth.setup


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.soneso.lumenshine.R
import com.soneso.lumenshine.presentation.auth.AuthFragment
import com.soneso.lumenshine.util.LsException
import com.soneso.lumenshine.util.Resource
import kotlinx.android.synthetic.main.fragment_confirm_mnemonic.*

/**
 * A simple [Fragment] subclass.
 *
 */
class ConfirmMnemonicFragment : AuthFragment() {

    private lateinit var helper: MnemonicQuizHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_confirm_mnemonic, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        subscribeToLiveData()
    }

    private fun setupListeners() {
        backButton.setOnClickListener { authActivity.navigate(R.id.to_mnemonic_screen) }
        nextButton.setOnClickListener {
            if (helper.checkPositions(word1Input.text, word2Input.text, word3Input.text, word4Input.text)) {
                errorView.visibility = View.INVISIBLE
                authViewModel.confirmMnemonic()
            } else {
                errorView.visibility = View.VISIBLE
            }
        }
    }

    private fun subscribeToLiveData() {
        authViewModel.liveMnemonic.observe(this, Observer {
            renderQuiz(it ?: return@Observer)
        })
        authViewModel.liveMnemonicConfirmation.observe(this, Observer {
            renderConfirmation(it ?: return@Observer)
        })
    }

    private fun renderQuiz(mnemonic: String) {
        helper = MnemonicQuizHelper(mnemonic)
        word1View.text = helper.wordsToGuess[0]
        word2View.text = helper.wordsToGuess[1]
        word3View.text = helper.wordsToGuess[2]
        word4View.text = helper.wordsToGuess[3]
    }

    private fun renderConfirmation(resource: Resource<Boolean, LsException>) {
        when (resource.state) {
            Resource.LOADING -> showLoadingView()
            Resource.FAILURE -> {
                hideLoadingView()
                showErrorSnackbar(resource.failure())
            }
            Resource.SUCCESS -> {
                hideLoadingView()
                authActivity.goToMain()
            }
        }
    }
}

package com.soneso.lumenshine.presentation.auth


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.soneso.lumenshine.R
import com.soneso.lumenshine.presentation.util.showInfoDialog
import kotlinx.android.synthetic.main.fragment_mnemonic.*

/**
 * A simple [Fragment] subclass.
 *
 */
class MnemonicFragment : AuthFragment() {

    private val wordAdapter = MnemonicWordAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_mnemonic, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showLoadingView()
        setupViews()
        subscribeForLiveData()
        setupListeners()

        authViewModel.fetchMnemonic()
    }

    private fun setupViews() {
        moreInfoLink.text = buildString {
            append(' ')
            append(getString(R.string.here))
        }

        mnemonicRecyclerView.layoutManager = LinearLayoutManager(context)
        mnemonicRecyclerView.isNestedScrollingEnabled = false
        mnemonicRecyclerView.adapter = wordAdapter
    }

    private fun subscribeForLiveData() {

        authViewModel.liveMnemonic.observe(this, Observer {
            hideLoadingView()
            wordAdapter.setMnemonic(it ?: return@Observer)
        })
    }

    private fun setupListeners() {

        nextButton.setOnClickListener {
            authActivity.navigate(R.id.to_confirm_mnemonic_screen)
        }
        moreInfoLink.setOnClickListener {
            activity?.showInfoDialog()
        }
    }

    companion object {
        const val TAG = "MnemonicFragment"

        fun newInstance() = MnemonicFragment()
    }
}

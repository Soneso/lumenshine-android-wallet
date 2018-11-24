package com.soneso.lumenshine.presentation.auth.setup


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.soneso.lumenshine.R
import com.soneso.lumenshine.presentation.auth.AuthFragment
import com.soneso.lumenshine.presentation.util.showInfoDialog
import kotlinx.android.synthetic.main.fragment_mnemonic.*

/**
 * A simple [Fragment] subclass.
 *
 */
class NoteMnemonicFragment : AuthFragment() {

    private val wordAdapter = MnemonicWordAdapter()
    private lateinit var viewModel: NoteMnemonicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[NoteMnemonicViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_mnemonic, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        subscribeForLiveData()
        setupListeners()
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

        viewModel.liveMnemonic.observe(this, Observer {
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
        const val TAG = "NoteMnemonicFragment"

        fun newInstance() = NoteMnemonicFragment()
    }
}

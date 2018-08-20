package com.soneso.lumenshine.presentation.wallets


import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.soneso.lumenshine.R
import com.soneso.lumenshine.presentation.general.SgFragment
import kotlinx.android.synthetic.main.fragment_wallets.*


/**
 * A simple [Fragment] subclass.
 */
class WalletsFragment : SgFragment() {

    private lateinit var walletsViewModel: WalletsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        walletsViewModel = ViewModelProviders.of(this, viewModelFactory)[WalletsViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_wallets, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emptyWalletView.populate("", emptyList())
        singWalletView.populate("blabla*lumenshine.com", listOf(Unit))
        multipleWalletView.populate(null, listOf(Unit, Unit))
    }

    companion object {
        const val TAG = "WalletsFragment"

        fun newInstance() = WalletsFragment()
    }
}

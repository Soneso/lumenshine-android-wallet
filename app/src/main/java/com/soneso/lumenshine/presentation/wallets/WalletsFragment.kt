package com.soneso.lumenshine.presentation.wallets


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.data.WalletCardData
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.presentation.general.LsFragment
import com.soneso.lumenshine.util.Resource
import kotlinx.android.synthetic.main.fragment_wallets.*


/**
 * A simple [Fragment] subclass.
 */
class WalletsFragment : LsFragment() {

    private lateinit var walletsViewModel: WalletsViewModel
    private lateinit var walletAdapter: WalletAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        walletsViewModel = ViewModelProviders.of(this, viewModelFactory)[WalletsViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_wallets, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        subscribeForLiveData()
    }

    private fun subscribeForLiveData() {

        walletsViewModel.liveWallets.observe(this, Observer {

            renderWallet(it ?: return@Observer)
        })
    }

    private fun renderWallet(resource: Resource<WalletCardData, ServerException>) {

        when (resource.state) {
            Resource.SUCCESS -> {
                walletAdapter.addWalletData(resource.success())
            }
        }
    }

    private fun setupRecyclerView() {

        walletAdapter = WalletAdapter()
        walletRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        walletRecyclerView.setHasFixedSize(true)
        walletRecyclerView.adapter = walletAdapter
    }

    companion object {
        const val TAG = "WalletsFragment"

        fun newInstance() = WalletsFragment()
    }
}

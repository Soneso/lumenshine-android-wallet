package com.soneso.lumenshine.presentation.home


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.data.Wallet
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.presentation.general.LsFragment
import com.soneso.lumenshine.presentation.util.forwardToBrowser
import com.soneso.lumenshine.util.Resource
import kotlinx.android.synthetic.main.fragment_home.*
import timber.log.Timber


/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : LsFragment() {

    private lateinit var adapter: HomeFeedAdapter
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, viewModelFactory)[HomeViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFeedRecyclerView()
    }

    private fun setupFeedRecyclerView() {
        recyclerViewFeed.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        adapter = HomeFeedAdapter()
        recyclerViewFeed.adapter = adapter
        adapter.onBlogLinkClickListener = {
            context?.forwardToBrowser(it.postUrl)
        }
        viewModel.liveWallets.observe(this, Observer {
            renderWallet(it ?: return@Observer)
        })
    }

    private fun renderWallet(resource: Resource<Wallet, ServerException>) {
        when (resource.state) {
            Resource.SUCCESS -> {
                val position = adapter.addWalletData(resource.success())
                if (position == 0) {
                    recyclerViewFeed.scrollToPosition(0)
                }
            }
            Resource.FAILURE -> {
                showErrorSnackbar(resource.failure())
                Timber.e(resource.failure())
            }
        }
    }

    companion object {
        const val TAG = "HomeFragment"

        fun newInstance() = HomeFragment()
    }
}

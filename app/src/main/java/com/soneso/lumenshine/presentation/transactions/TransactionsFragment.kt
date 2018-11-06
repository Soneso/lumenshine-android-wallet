package com.soneso.lumenshine.presentation.transactions


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.soneso.lumenshine.R
import com.soneso.lumenshine.model.entities.operations.Operation
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.presentation.general.LsFragment
import com.soneso.lumenshine.util.Resource
import kotlinx.android.synthetic.main.transactions_history.*

/**
 * A simple [Fragment] subclass.
 *
 */
class TransactionsFragment : LsFragment() {

    private lateinit var transactionsViewModel: TransactionsViewModel
    private lateinit var transactionsAdapter: TransactionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transactionsViewModel = ViewModelProviders.of(this, viewModelFactory)[TransactionsViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_transactions, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        subscribeForLiveData()
    }

    private fun setupRecyclerView() {

        transactionsAdapter = TransactionsAdapter()
        transactionsRecyclerView.layoutManager = LinearLayoutManager(context)
        transactionsRecyclerView.setHasFixedSize(true)
        transactionsRecyclerView.adapter = transactionsAdapter
    }

    private fun subscribeForLiveData() {
        transactionsViewModel.liveOperations.observe(this, Observer {
            renderOperations(it ?: return@Observer)
        })
    }

    private fun renderOperations(resource: Resource<List<Operation>, ServerException>) {

        when (resource.state) {
            Resource.LOADING -> {

            }
            Resource.SUCCESS -> {
                transactionsAdapter.setTransactionsData(resource.success())
            }
            Resource.FAILURE -> {

            }
        }
    }

    companion object {
        const val TAG = "TransactionsFragment"

        fun newInstance() = TransactionsFragment()
    }
}

package com.soneso.lumenshine.presentation.transactions


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.usecases.TransactionsUseCases
import com.soneso.lumenshine.model.entities.operations.Operation
import com.soneso.lumenshine.networking.dto.exceptions.ServerException
import com.soneso.lumenshine.presentation.general.LsFragment
import com.soneso.lumenshine.util.Resource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_transactions.*
import java.text.SimpleDateFormat
import java.util.*

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

        transactionsAdapter = TransactionsAdapter(this@TransactionsFragment)
        transactionsRecyclerView.layoutManager = LinearLayoutManager(context)
        transactionsRecyclerView.setHasFixedSize(true)
        transactionsRecyclerView.adapter = transactionsAdapter

        val dividerItemDecoration = DividerItemDecoration(context, VERTICAL)
        transactionsRecyclerView.addItemDecoration(dividerItemDecoration)
    }

    private fun subscribeForLiveData() {
        transactionsViewModel.liveWallets.observe(this, Observer {
            //TODO load the combobox
            when (it.state) {
                Resource.SUCCESS -> {
                    if (!it.success().isEmpty()) {
                        transactionsViewModel.selectWallet(it.success()[0])
                        transactionsViewModel.selectDateFrom(Date(System.currentTimeMillis() - 7 * TransactionsUseCases.DAY_IN_MS))
                        transactionsViewModel.selectDateTo(Date())
                    }
                }
            }
        })

        transactionsViewModel.observeWallet()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { transactionWalletText.text = it.name }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        transactionsViewModel.observeDateFrom()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { transactionDateFromText.text = dateFormat.format(it) }

        transactionsViewModel.observeDateTo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { transactionDateToText.text = dateFormat.format(it) }

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

package com.soneso.lumenshine.presentation.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.data.OperationsSort
import com.soneso.lumenshine.domain.util.SortType
import com.soneso.lumenshine.presentation.general.LsFragment
import kotlinx.android.synthetic.main.fragment_transactions_sort.*

class TransactionsSortFragment : LsFragment() {

    private lateinit var sortViewModel: TransactionsSortViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sortViewModel = ViewModelProviders.of(this, viewModelFactory)[TransactionsSortViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_transactions_sort, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        populateSortData(sortViewModel.getOperationSort())

        dateSortSwitch.setOnCheckedChangeListener { _, isChecked -> dateSortButton.isActivated = isChecked }
        typeSortSwitch.setOnCheckedChangeListener { _, isChecked -> typeSortButton.isActivated = isChecked }
        amountSortSwitch.setOnCheckedChangeListener { _, isChecked -> amountSortButton.isActivated = isChecked }
        currencySortSwitch.setOnCheckedChangeListener { _, isChecked -> currencySortButton.isActivated = isChecked }

        dateSortButton.setOnClickListener { dateSortButton.isSelected = !dateSortButton.isSelected }
        typeSortButton.setOnClickListener { typeSortButton.isSelected = !typeSortButton.isSelected }
        amountSortButton.setOnClickListener { amountSortButton.isSelected = !amountSortButton.isSelected }
        currencySortButton.setOnClickListener { currencySortButton.isSelected = !currencySortButton.isSelected }

        clearSortButton.setOnClickListener { populateSortData(sortViewModel.getOperationSort()) }
    }

    override fun onDestroyView() {

        sortViewModel.updateOperationSort(
                dateSortSwitch.isChecked,
                if (dateSortButton.isSelected) SortType.ASC else SortType.DESC,
                typeSortSwitch.isChecked,
                if (typeSortButton.isSelected) SortType.ASC else SortType.DESC,
                amountSortSwitch.isChecked,
                if (amountSortButton.isSelected) SortType.ASC else SortType.DESC,
                currencySortSwitch.isChecked,
                if (clearSortButton.isSelected) SortType.ASC else SortType.DESC
        )

        super.onDestroyView()
    }

    private fun populateSortData(operationsSort: OperationsSort) {

        dateSortSwitch.isChecked = operationsSort.sortByDate
        dateSortButton.isActivated = operationsSort.sortByDate
        dateSortButton.isSelected = SortType.ASC == operationsSort.dateSortType

        typeSortSwitch.isChecked = operationsSort.sortByType
        typeSortButton.isActivated = operationsSort.sortByType
        typeSortButton.isSelected = SortType.ASC == operationsSort.typeSortType

        amountSortSwitch.isChecked = operationsSort.sortByAmount
        amountSortButton.isActivated = operationsSort.sortByAmount
        amountSortButton.isSelected = SortType.ASC == operationsSort.amountSortType

        currencySortSwitch.isChecked = operationsSort.sortByCurrency
        currencySortButton.isActivated = operationsSort.sortByCurrency
        currencySortButton.isSelected = SortType.ASC == operationsSort.currencySortType
    }

    companion object {
        const val TAG = "TransactionsSortFragment"

        fun newInstance() = TransactionsSortFragment()
    }
}
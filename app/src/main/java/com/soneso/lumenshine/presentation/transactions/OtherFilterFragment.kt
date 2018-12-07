package com.soneso.lumenshine.presentation.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.soneso.lumenshine.R
import com.soneso.lumenshine.model.entities.operations.Operation
import com.soneso.lumenshine.presentation.general.LsFragment
import kotlinx.android.synthetic.main.operation_other_filter.*

class OtherFilterFragment : LsFragment() {

    private lateinit var otherFilterViewModel: OtherFilterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        otherFilterViewModel = ViewModelProviders.of(this, viewModelFactory)[OtherFilterViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.operation_other_filter, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initiateSwitches()
        clearFilterButton.setOnClickListener { clearAllFilters() }
    }

    override fun onDestroyView() {
        val otherTypesFilter: HashSet<Operation.Type> = HashSet()
        if (setOptionsSwitch.isChecked) otherTypesFilter.add(Operation.Type.SET_OPTIONS)
        if (manageDataSwitch.isChecked) otherTypesFilter.add(Operation.Type.MANAGE_DATA)
        if (trustSwitch.isChecked) {
            otherTypesFilter.add(Operation.Type.CHANGE_TRUST)
            otherTypesFilter.add(Operation.Type.ALLOW_TRUST)
        }
        if (accountMergeSwitch.isChecked) otherTypesFilter.add(Operation.Type.ACCOUNT_MERGE)
        if (bumpSequenceSwitch.isChecked) otherTypesFilter.add(Operation.Type.BUMP_SEQUENCE)
        otherFilterViewModel.updateOtherTypesFilter(otherTypesFilter)

        super.onDestroyView()
    }

    private fun initiateSwitches() {
        val otherSelectedOperations = otherFilterViewModel.getOtherTypesFilter()

        if (otherSelectedOperations.contains(Operation.Type.SET_OPTIONS)) setOptionsSwitch.isChecked = true
        if (otherSelectedOperations.contains(Operation.Type.MANAGE_DATA)) manageDataSwitch.isChecked = true
        if (otherSelectedOperations.contains(Operation.Type.CHANGE_TRUST)) trustSwitch.isChecked = true
        if (otherSelectedOperations.contains(Operation.Type.ACCOUNT_MERGE)) accountMergeSwitch.isChecked = true
        if (otherSelectedOperations.contains(Operation.Type.BUMP_SEQUENCE)) bumpSequenceSwitch.isChecked = true
    }

    private fun clearAllFilters() {
        setOptionsSwitch.isChecked = false
        manageDataSwitch.isChecked = false
        trustSwitch.isChecked = false
        accountMergeSwitch.isChecked = false
        bumpSequenceSwitch.isChecked = false
    }

    companion object {
        const val TAG = "OtherFilterFragment"

        fun newInstance() = OtherFilterFragment()
    }
}
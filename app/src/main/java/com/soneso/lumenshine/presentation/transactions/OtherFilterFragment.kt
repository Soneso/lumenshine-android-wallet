package com.soneso.lumenshine.presentation.transactions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.soneso.lumenshine.R
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

        clearFilterButton.setOnClickListener {
            setOptionsSwitch.isChecked = false
            inflationSwitch.isChecked = false
            manageDataSwitch.isChecked = false
            trustSwitch.isChecked = false
            accountMergeSwitch.isChecked = false
            bumpSequenceSwitch.isChecked = false
        }
    }

    companion object {
        const val TAG = "OtherFilterFragment"

        fun newInstance() = OtherFilterFragment()
    }
}
package com.soneso.lumenshine.presentation.auth.registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.soneso.lumenshine.R
import com.soneso.lumenshine.presentation.general.DialogWithHeader
import kotlinx.android.synthetic.main.password_requirements.*

class PasswordRequirementsDialog : DialogWithHeader() {
    private val requirementAdapter = PasswordRequirementAdapter()

    companion object {
        const val TAG = "PasswordRequirementsDialog"

        fun showInstance(fm: FragmentManager) {
            PasswordRequirementsDialog().show(fm, TAG)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        passwordRequirementsRecyclerView.layoutManager = LinearLayoutManager(context)
        passwordRequirementsRecyclerView.isNestedScrollingEnabled = true
        passwordRequirementsRecyclerView.adapter = requirementAdapter


        val requirements = ArrayList<String>()
        requirements.addAll(resources.getStringArray(R.array.password_requirements))
        requirementAdapter.setPasswordRequirements(requirements)
    }

    override fun getTitle(): String {
        return context!!.getString(R.string.password_requirements)
    }

    override fun getContentLayout(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.password_requirements, container, false).apply {
                minimumHeight = resources.displayMetrics.heightPixels
            }
}

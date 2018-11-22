package com.soneso.lumenshine.presentation.auth;

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.soneso.lumenshine.R
import com.soneso.lumenshine.model.entities.PasswordRequirement
import kotlinx.android.synthetic.main.password_requirement_item.view.*

class PasswordRequirementAdapter : RecyclerView.Adapter<PasswordRequirementAdapter.RequirementViewHolder>() {
    private val requirements = ArrayList<PasswordRequirement>()
    private lateinit var context: Context

    override fun getItemCount(): Int {
        return requirements.size
    }

    override fun onBindViewHolder(holder: RequirementViewHolder, position: Int) {
        holder.bindData()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequirementViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        context = parent.context
        val rootView = inflater.inflate(R.layout.password_requirement_item, parent, false)
        return RequirementViewHolder(rootView)
    }

    fun setPasswordRequirements(requirements: ArrayList<String>) {
        this.requirements.clear()
        for (requirement: String in requirements) {
            val requirementParts = requirement.split("|")
            this.requirements.add(PasswordRequirement(requirementParts[0], requirementParts[1]))
        }
        notifyDataSetChanged()
    }

    inner class RequirementViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val requirementTitle = v.passwordRequirementTitle
        private val requirementSubtitle = v.passwordRequirementSubtitle

        fun bindData() {
            val requirement = requirements[adapterPosition]
            requirementTitle.text = requirement.summary
            requirementSubtitle.text = context.getString(R.string.password_requirement_subtitle, requirement.description)
        }
    }
}

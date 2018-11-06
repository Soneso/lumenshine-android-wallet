package com.soneso.lumenshine.presentation.transactions

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.soneso.lumenshine.R
import com.soneso.lumenshine.model.entities.operations.Operation
import com.soneso.lumenshine.model.entities.operations.Payment
import kotlinx.android.synthetic.main.transaction_payment_item.view.*

class TransactionsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val operationsList = ArrayList<Operation>()

    fun setTransactionsData(operations: List<Operation>) {
        operationsList.clear()
        operationsList.addAll(operations)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Operation.Type.PAYMENT.value -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_payment_item, parent, false)
                PaymentHolder(view)
            }
            else -> {
                //TODO
                val view = LayoutInflater.from(parent.context).inflate(R.layout.transaction_payment_item, parent, false)
                PaymentHolder(view)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return operationsList[position].type.value
    }

    override fun getItemCount() = operationsList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            Operation.Type.PAYMENT.value -> {
                (holder as PaymentHolder).populate(operationsList[position] as Payment)
            }
        }
    }


    inner class PaymentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun populate(payment: Payment) {
            itemView.paymentDateText.text = payment.date
            itemView.paymentTypeText.text = payment.type.name
            itemView.paymentAmountText.text = payment.amount
            itemView.paymentCurrencyText.text = payment.currency
            itemView.paymentFeeText.text = payment.fee
            itemView.paymentId.text = payment.id.toString()

            if (payment.isReceived) {
                itemView.paymentTypeText.text = itemView.context.getString(R.string.payment_received)
                itemView.paymentAmountText.setTextColor(Color.GREEN)
            } else {
                itemView.paymentTypeText.text = itemView.context.getString(R.string.payment_sent)
                itemView.paymentAmountText.setTextColor(Color.RED)
            }
        }
    }
}
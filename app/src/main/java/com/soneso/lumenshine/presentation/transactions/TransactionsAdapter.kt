package com.soneso.lumenshine.presentation.transactions

import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.soneso.lumenshine.R
import com.soneso.lumenshine.model.entities.operations.*
import kotlinx.android.synthetic.main.operation_details.view.*
import kotlinx.android.synthetic.main.operation_item.view.*

class TransactionsAdapter(private val transactionsFragment: TransactionsFragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val operationsList = ArrayList<Operation>()
    var onOperationJsonSetListener: ((String) -> Unit)? = null

    fun setTransactionsData(operations: List<Operation>) {
        operationsList.clear()
        operationsList.addAll(operations)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Operation.Type.PAYMENT.value -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.operation_item, parent, false)
                PaymentHolder(transactionsFragment, view)
            }
            Operation.Type.OFFER.value -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.operation_item, parent, false)
                OfferHolder(transactionsFragment, view)
            }
            Operation.Type.PASSIVE_OFFER.value -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.operation_item, parent, false)
                OfferHolder(transactionsFragment, view)
            }
            Operation.Type.CHANGE_TRUST.value -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.operation_item, parent, false)
                ChangeTrustHolder(transactionsFragment, view)
            }
            Operation.Type.MANAGE_DATA.value -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.operation_item, parent, false)
                ManageDataHolder(transactionsFragment, view)
            }
            Operation.Type.BUMP_SEQUENCE.value -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.operation_item, parent, false)
                BumpSequenceHolder(transactionsFragment, view)
            }
            Operation.Type.ACCOUNT_MERGE.value -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.operation_item, parent, false)
                AccountMergeHolder(transactionsFragment, view)
            }
            Operation.Type.SET_OPTIONS.value -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.operation_item, parent, false)
                SetOptionHolder(transactionsFragment, view)
            }
            Operation.Type.ALLOW_TRUST.value -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.operation_item, parent, false)
                OperationHolder(transactionsFragment, view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.operation_item, parent, false)
                OperationHolder(transactionsFragment, view)
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
            Operation.Type.OFFER.value -> {
                (holder as OfferHolder).populate(operationsList[position] as Offer)
            }
            Operation.Type.PASSIVE_OFFER.value -> {
                (holder as OfferHolder).populate(operationsList[position] as Offer)
            }
            Operation.Type.CHANGE_TRUST.value -> {
                (holder as ChangeTrustHolder).populate(operationsList[position] as ChangeTrust)
            }
            Operation.Type.MANAGE_DATA.value -> {
                (holder as ManageDataHolder).populate(operationsList[position] as ManageData)
            }
            Operation.Type.BUMP_SEQUENCE.value -> {
                (holder as BumpSequenceHolder).populate(operationsList[position] as BumpSequence)
            }
            Operation.Type.ACCOUNT_MERGE.value -> {
                (holder as AccountMergeHolder).populate(operationsList[position] as AccountMerge)
            }
            Operation.Type.SET_OPTIONS.value -> {
                (holder as SetOptionHolder).populate(operationsList[position] as SetOptions)
            }
            Operation.Type.ALLOW_TRUST.value -> {
                // #Zica load Allow Trust operation details
                (holder as OperationHolder).populateGeneralInfo(operationsList[position])
            }
        }
    }

    open inner class OperationHolder(private val transactionsFragment: TransactionsFragment, itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun populateGeneralInfo(operation: Operation) {
            itemView.operationDateText.text = operation.date
            itemView.operationFeeText.text = operation.fee
            itemView.operationId.text = operation.id.toString()

            if (!operation.bySelectedWallet) {
                itemView.operationAccountTag.visibility = View.VISIBLE
                itemView.operationAccountPK.visibility = View.VISIBLE
                itemView.operationAccountPK.text = operation.transactionSource

                itemView.sourceCopyButton.visibility = View.VISIBLE
                itemView.sourceCopyButton.setOnClickListener {
                    val clipboard = itemView.context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("operation source", operation.transactionSource)
                    clipboard.primaryClip = clip
                    transactionsFragment.showSnackbar(itemView.context.getString(R.string.source_account) + " " + itemView.context.getString(R.string.public_key_copied))
                }
            }

            if (operation.hasMemo()) {
                itemView.operationMemoTag.visibility = View.VISIBLE
                itemView.operationMemo.visibility = View.VISIBLE
                itemView.operationMemo.text = operation.transactionMemo
            }

            itemView.operationId.setOnClickListener {
                //#ZICA load the JSON from the response
                transactionsFragment.showInfoDialog(R.string.json_content, R.layout.operation_json)
                onOperationJsonSetListener?.invoke("Test JSON")
            }
        }
    }

    inner class PaymentHolder(transactionsFragment: TransactionsFragment, itemView: View) : OperationHolder(transactionsFragment, itemView) {

        fun populate(payment: Payment) {
            populateGeneralInfo(payment)

            itemView.operationAmountText.text = payment.amount
            itemView.operationCurrencyText.text = payment.currency

            if (payment.isReceived) {
                itemView.operationTypeText.text = itemView.context.getString(R.string.payment_received)

                itemView.paymentRecipientTag.visibility = View.GONE
                itemView.paymentRecipientPK.visibility = View.GONE
                itemView.recipientCopyButton.visibility = View.GONE
                itemView.paymentSenderTag.visibility = View.VISIBLE
                itemView.paymentSenderPK.visibility = View.VISIBLE
                itemView.senderCopyButton.visibility = View.VISIBLE
                itemView.senderCopyButton.setOnClickListener {
                    val clipboard = itemView.context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("payment source", payment.source)
                    clipboard.primaryClip = clip
                    transactionsFragment.showSnackbar("Sender " + itemView.context.getString(R.string.public_key_copied))
                }

                itemView.operationAmountText.setTextColor(Color.GREEN)
                itemView.paymentSenderPK.text = payment.source

            } else {
                itemView.operationTypeText.text = itemView.context.getString(R.string.payment_sent)

                itemView.paymentSenderTag.visibility = View.GONE
                itemView.paymentSenderPK.visibility = View.GONE
                itemView.senderCopyButton.visibility = View.GONE
                itemView.paymentRecipientTag.visibility = View.VISIBLE
                itemView.paymentRecipientPK.visibility = View.VISIBLE
                itemView.recipientCopyButton.visibility = View.VISIBLE
                itemView.recipientCopyButton.setOnClickListener {
                    val clipboard = itemView.context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("payment recipient", payment.destination)
                    clipboard.primaryClip = clip
                    transactionsFragment.showSnackbar("Recipient " + itemView.context.getString(R.string.public_key_copied))
                }

                itemView.operationAmountText.setTextColor(Color.RED)
                itemView.paymentRecipientPK.text = payment.destination
            }
        }
    }

    inner class OfferHolder(transactionsFragment: TransactionsFragment, itemView: View) : OperationHolder(transactionsFragment, itemView) {

        init {
            itemView.offerIdTag.visibility = View.VISIBLE
            itemView.offerIdText.visibility = View.VISIBLE
            itemView.offerBuyingTag.visibility = View.VISIBLE
            itemView.offerBuyingText.visibility = View.VISIBLE
            itemView.offerSellingTag.visibility = View.VISIBLE
            itemView.offerSellingText.visibility = View.VISIBLE
            itemView.offerPriceTag.visibility = View.VISIBLE
            itemView.offerPriceText.visibility = View.VISIBLE
        }

        fun populate(offer: Offer) {
            populateGeneralInfo(offer)

            itemView.operationTypeText.text = getType(offer)
            itemView.operationAmountText.text = offer.amount
            itemView.operationCurrencyText.text = offer.getCurrency()

            itemView.offerIdText.text = offer.offerId.toString()
            itemView.offerBuyingText.text = offer.buyingAssetCode
            itemView.offerSellingText.text = offer.getSellingText()
            itemView.offerPriceText.text = offer.assetPrice
        }

        private fun getType(offer: Offer): String =
                if (Operation.Type.OFFER == offer.type) {
                    if (offer.isDeleted())
                        itemView.context.getString(R.string.offer_removed)
                    else
                        itemView.context.getString(R.string.offer_create)
                } else {
                    if (offer.isDeleted())
                        itemView.context.getString(R.string.passive_offer_removed)
                    else
                        itemView.context.getString(R.string.passive_offer_create)
                }
    }

    inner class ChangeTrustHolder(transactionsFragment: TransactionsFragment, itemView: View) : OperationHolder(transactionsFragment, itemView) {

        init {
            itemView.operationAmountTag.visibility = View.GONE
            itemView.operationAmountText.visibility = View.GONE
            itemView.operationTypeText.text = itemView.context.getString(R.string.change_trust)

            itemView.changeTrustTypeTag.visibility = View.VISIBLE
            itemView.changeTrustTypeText.visibility = View.VISIBLE
            itemView.changeTrustAssetTag.visibility = View.VISIBLE
            itemView.changeTrustAssetText.visibility = View.VISIBLE
            itemView.changeTrustIssuerTag.visibility = View.VISIBLE
            itemView.changeTrustIssuerText.visibility = View.VISIBLE
            itemView.changeTrustLimitTag.visibility = View.VISIBLE
            itemView.changeTrustLimitText.visibility = View.VISIBLE
            itemView.issuerCopyButton.visibility = View.VISIBLE
        }

        fun populate(changeTrust: ChangeTrust) {
            populateGeneralInfo(changeTrust)

            itemView.operationCurrencyText.text = changeTrust.assetCode
            itemView.changeTrustTypeText.text = getType(changeTrust)
            itemView.changeTrustAssetText.text = changeTrust.assetCode
            itemView.changeTrustIssuerText.text = changeTrust.assetIssuer
            itemView.changeTrustLimitText.text = changeTrust.limit
            itemView.issuerCopyButton.setOnClickListener {
                val clipboard = itemView.context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("asset issuer", changeTrust.assetIssuer)
                clipboard.primaryClip = clip
                transactionsFragment.showSnackbar(itemView.context.getString(R.string.change_trust_issuer) + " " + itemView.context.getString(R.string.public_key_copied))
            }
        }

        private fun getType(changeTrust: ChangeTrust): String =
                if (changeTrust.isAddTrust())
                    itemView.context.getString(R.string.change_trust_add)
                else
                    itemView.context.getString(R.string.change_trust_remove)
    }

    inner class ManageDataHolder(transactionsFragment: TransactionsFragment, itemView: View) : OperationHolder(transactionsFragment, itemView) {

        init {
            itemView.operationTypeText.text = itemView.context.getString(R.string.manage_data)
            itemView.operationAmountTag.visibility = View.GONE
            itemView.operationAmountText.visibility = View.GONE
            itemView.operationCurrencyTag.visibility = View.GONE
            itemView.operationCurrencyText.visibility = View.GONE

            itemView.manageDataNameTag.visibility = View.VISIBLE
            itemView.manageDataNameText.visibility = View.VISIBLE
            itemView.manageDataValueTag.visibility = View.VISIBLE
            itemView.manageDataValueText.visibility = View.VISIBLE
        }

        fun populate(manageData: ManageData) {
            populateGeneralInfo(manageData)

            itemView.manageDataNameText.text = manageData.name
            itemView.manageDataValueText.text = manageData.value
        }
    }

    inner class BumpSequenceHolder(transactionsFragment: TransactionsFragment, itemView: View) : OperationHolder(transactionsFragment, itemView) {

        init {
            itemView.operationTypeText.text = itemView.context.getString(R.string.bump_sequence)
            itemView.operationAmountTag.visibility = View.GONE
            itemView.operationAmountText.visibility = View.GONE
            itemView.operationCurrencyTag.visibility = View.GONE
            itemView.operationCurrencyText.visibility = View.GONE

            itemView.bumpToTag.visibility = View.VISIBLE
            itemView.bumpToText.visibility = View.VISIBLE
        }

        fun populate(bumpSequence: BumpSequence) {
            populateGeneralInfo(bumpSequence)

            itemView.bumpToText.text = bumpSequence.bumpTo.toString()
        }
    }

    inner class AccountMergeHolder(transactionsFragment: TransactionsFragment, itemView: View) : OperationHolder(transactionsFragment, itemView) {

        init {
            itemView.operationTypeText.text = itemView.context.getString(R.string.account_merge)
            itemView.operationAmountText.setTextColor(Color.RED)
            itemView.operationCurrencyText.setTextColor(Color.RED)

            itemView.mergedAccountTag.visibility = View.VISIBLE
            itemView.mergedAccountText.visibility = View.VISIBLE
            itemView.mergedAccountCopyButton.visibility = View.VISIBLE
        }

        fun populate(accountMerge: AccountMerge) {
            populateGeneralInfo(accountMerge)

            itemView.operationAmountText.text = "TODO"
            itemView.operationCurrencyText.text = "TODO"
            itemView.mergedAccountText.text = accountMerge.mergedAccount

            itemView.mergedAccountCopyButton.setOnClickListener {
                val clipboard = itemView.context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("merged account", accountMerge.mergedAccount)
                clipboard.primaryClip = clip
                transactionsFragment.showSnackbar(itemView.context.getString(R.string.account_merge_account) + " " + itemView.context.getString(R.string.public_key_copied))
            }
        }
    }

    inner class SetOptionHolder(transactionsFragment: TransactionsFragment, itemView: View) : OperationHolder(transactionsFragment, itemView) {

        init {
            itemView.operationTypeText.text = itemView.context.getString(R.string.set_option)
            itemView.operationAmountTag.visibility = View.GONE
            itemView.operationAmountText.visibility = View.GONE
            itemView.operationCurrencyTag.visibility = View.GONE
            itemView.operationCurrencyText.visibility = View.GONE
        }

        fun populate(setOptions: SetOptions) {
            populateGeneralInfo(setOptions)

            if (setOptions.inflationDestination != null) displayInflationDestination(setOptions.inflationDestination!!)
            if (setOptions.setFlags != null) displaySetFlags(setOptions.setFlags!!)
            if (setOptions.clearedFlags != null) displayClearedFlags(setOptions.clearedFlags!!)
            if (setOptions.masterWeight != null) displayMasterWeight(setOptions.masterWeight!!)
            if (setOptions.lowThreshold != null) displayLowThreshold(setOptions.lowThreshold!!)
            if (setOptions.mediumThreshold != null) displayMediumThreshold(setOptions.mediumThreshold!!)
            if (setOptions.highThreshold != null) displayHighThreshold(setOptions.highThreshold!!)
            if (setOptions.signerKey != null) displaySignerInfo(setOptions.signerKey!!, setOptions.signerType!!, setOptions.signerWeight!!)
            if (setOptions.homeDomain != null) displayHomeDomain(setOptions.homeDomain!!)
        }

        private fun displayInflationDestination(inflationDestination: String) {
            itemView.inflationDestinationTag.visibility = View.VISIBLE
            itemView.inflationDestinationText.visibility = View.VISIBLE
            itemView.inflationDestinationCopyButton.visibility = View.VISIBLE

            itemView.inflationDestinationText.text = inflationDestination

            itemView.inflationDestinationCopyButton.setOnClickListener {
                val clipboard = itemView.context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("inflation destination", inflationDestination)
                clipboard.primaryClip = clip
                transactionsFragment.showSnackbar(itemView.context.getString(R.string.inflation_destination) + " " + itemView.context.getString(R.string.public_key_copied))
            }
        }

        private fun displaySetFlags(setFlags: Set<String>) {
            itemView.setFlagsTag.visibility = View.VISIBLE
            itemView.setFlagsText.visibility = View.VISIBLE
            itemView.setFlagsText.text = setFlags.joinToString("\n")
        }

        private fun displayClearedFlags(clearedFlags: Set<String>) {
            itemView.clearFlagsTag.visibility = View.VISIBLE
            itemView.clearFlagsText.visibility = View.VISIBLE
            itemView.clearFlagsText.text = clearedFlags.joinToString("\n")
        }

        private fun displayMasterWeight(masterWeight: Int) {
            itemView.masterWeightTag.visibility = View.VISIBLE
            itemView.masterWeightText.visibility = View.VISIBLE
            itemView.masterWeightText.text = masterWeight.toString()
        }

        private fun displayLowThreshold(lowThreshold: Int) {
            itemView.lowThresholdTag.visibility = View.VISIBLE
            itemView.lowThresholdText.visibility = View.VISIBLE
            itemView.lowThresholdText.text = lowThreshold.toString()
        }

        private fun displayMediumThreshold(mediumThreshold: Int) {
            itemView.mediumThresholdTag.visibility = View.VISIBLE
            itemView.mediumThresholdText.visibility = View.VISIBLE
            itemView.mediumThresholdText.text = mediumThreshold.toString()
        }

        private fun displayHighThreshold(highThreshold: Int) {
            itemView.highThresholdTag.visibility = View.VISIBLE
            itemView.highThresholdText.visibility = View.VISIBLE
            itemView.highThresholdText.text = highThreshold.toString()
        }

        private fun displaySignerInfo(signerKey: String, signerType: String, signerWeight: Int) {
            itemView.signerTag.visibility = View.VISIBLE
            itemView.signerText.visibility = View.VISIBLE
            itemView.signerTypeTag.visibility = View.VISIBLE
            itemView.signerTypeText.visibility = View.VISIBLE
            itemView.signerCopyButton.visibility = View.VISIBLE
            itemView.signerWeightTag.visibility = View.VISIBLE
            itemView.signerWeightText.visibility = View.VISIBLE

            itemView.signerTag.text = if (signerWeight == 0) itemView.context.getString(R.string.signer_removed) else itemView.context.getString(R.string.signer_added)
            itemView.signerText.text = signerKey
            itemView.signerTypeText.text = signerType
            itemView.signerWeightText.text = signerWeight.toString()
            itemView.signerCopyButton.setOnClickListener {
                val clipboard = itemView.context.getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("signer key", signerKey)
                clipboard.primaryClip = clip
                transactionsFragment.showSnackbar("Signer " + itemView.context.getString(R.string.public_key_copied))
            }
        }

        private fun displayHomeDomain(homeDomain: String) {
            itemView.homeDomainTag.visibility = View.VISIBLE
            itemView.homeDomainText.visibility = View.VISIBLE
            itemView.homeDomainText.text = homeDomain
        }
    }
}
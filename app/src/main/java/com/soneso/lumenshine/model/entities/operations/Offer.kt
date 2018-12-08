package com.soneso.lumenshine.model.entities.operations

class Offer(
        id: Long,
        fee: String,
        type: Operation.Type,
        order: Int,
        date: String,
        bySelectedWallet: Boolean,
        transactionSource: String,
        transactionHash: String,
        transactionMemoType: String,
        transactionMemo: String,
        val offerId: Long,
        val amount: String,
        val assetPrice: String,
        val buyingAssetCode: String,
        val sellingAssetType: String
) : Operation(id, type, fee, order, date, transactionSource, transactionHash, transactionMemoType, transactionMemo, bySelectedWallet) {

    fun isDeleted(): Boolean = amount.toFloat() == 0f

    fun getCurrency(): String = "$sellingAssetType-$buyingAssetCode"

    fun getSellingText(): String = "$amount $sellingAssetType"

    override fun getSortAmount() = amount.toDouble()
    override fun getSortCurrency() = getCurrency()
}
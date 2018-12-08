package com.soneso.lumenshine.model.entities.operations

class SetOptions(
        id: Long,
        fee: String,
        order: Int,
        date: String,
        bySelectedWallet: Boolean,
        transactionSource: String,
        transactionHash: String,
        transactionMemoType: String,
        transactionMemo: String
) : Operation(id, Type.SET_OPTIONS, fee, order, date, transactionSource, transactionHash, transactionMemoType, transactionMemo, bySelectedWallet) {

    var inflationDestination: String? = null
    var setFlags: Set<String>? = null
    var clearedFlags: Set<String>? = null
    var masterWeight: Int? = null
    var lowThreshold: Int? = null
    var mediumThreshold: Int? = null
    var highThreshold: Int? = null
    var signerKey: String? = null
    var signerType: String? = null
    var signerWeight: Int? = null
    var homeDomain: String? = null
    
    override fun getSortAmount(): Nothing? = null
    override fun getSortCurrency(): Nothing? = null
}
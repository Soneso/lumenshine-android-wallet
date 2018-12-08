package com.soneso.lumenshine.model.entities.operations

abstract class Operation(
        val id: Long,
        val type: Type,
        val fee: String,
        val order: Int,
        val date: String,
        val transactionSource: String,
        val transactionHash: String,
        private val transactionMemoType: String,
        val transactionMemo: String,
        var bySelectedWallet: Boolean
) {


    enum class Type(val value: Int) {
        CREATE_ACCOUNT(0),
        PAYMENT(1),
        PATH_PAYMENT(2),
        OFFER(3),
        PASSIVE_OFFER(4),
        SET_OPTIONS(5),
        CHANGE_TRUST(6),
        ALLOW_TRUST(7),
        ACCOUNT_MERGE(8),
        MANAGE_DATA(10),
        BUMP_SEQUENCE(11);
    }

    fun hasMemo(): Boolean = transactionMemoType != "none"
    abstract fun getSortAmount(): Double?
    abstract fun getSortCurrency(): String?
}
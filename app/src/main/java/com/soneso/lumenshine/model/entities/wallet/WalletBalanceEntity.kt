package com.soneso.lumenshine.model.entities.wallet

class WalletBalanceEntity(
        val amount: Double,
        val code: String,
        val type: AssetType,
        val sellingLiabilities: Double
) {

    fun availableAmount(walletMinBalance: Double): Double {
        return when (type) {
            AssetType.NATIVE -> {
                amount - walletMinBalance - sellingLiabilities - Constants.TRANSACTION_FEE
            }
            else -> amount - sellingLiabilities
        }
    }
}
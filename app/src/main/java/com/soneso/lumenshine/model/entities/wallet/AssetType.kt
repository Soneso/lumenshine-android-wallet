package com.soneso.lumenshine.model.entities.wallet

enum class AssetType(val stellarName: String) {
    NON_EXISTENT(""), NATIVE("native"), CREDIT_ALPHANUM4("credit_alphanum4"), CREDIT_ALPHANUM12("credit_alphanum4");

    companion object {

        fun fromStellarName(name: String): AssetType {
            for (value in values()) {
                if (value.stellarName == name) {
                    return value
                }
            }
            return NON_EXISTENT
        }
    }
}
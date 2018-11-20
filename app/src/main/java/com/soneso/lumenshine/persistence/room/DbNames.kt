package com.soneso.lumenshine.persistence.room

object DbNames {

    const val DB_NAME = "secure-sg-db"

    const val COLUMN_USERNAME = "username"
    const val TABLE_USER_DATA = "user_data"
    const val COLUMN_PK_0 = "public_key_index_0"
    const val COLUMN_PK_188 = "public_key_index_188"
    const val COLUMN_PASS_KDF_SALT = "ppassword_kdf_salt"
    const val COLUMN_ENCRYPTED_MNEMONIC_MASTER_KEY = "encrypted_mnemonic_master_key"
    const val COLUMN_MMK_ENCRYPTION_IV = "mnemonic_master_key_encryption_iv"
    const val COLUMN_ENCRYPTED_MNEMONIC = "encrypted_mnemonic"
    const val COLUMN_MNEMONIC_ENCRYPTION_IV = "mnemonic_encryption_iv"
    const val COLUMN_ENCRYPTED_WORD_LIST_MASTER_KEY = "encrypted_word_list_master_key"
    const val COLUMN_WLMK_ENCRYPTION_IV = "word_list_master_key_encryption_iv"
    const val COLUMN_ENCRYPTED_WORD_LIST = "encrypted_word_list"
    const val COLUMN_WORD_LIST_ENCRYPTION_IV = "word_list_encryption_iv"

    const val TABLE_WALLETS = "wallets"
    const val COLUMN_ID = "id"
    const val COLUMEN_PUBLIC_KEY = "public_key"
    const val COLUMN_NAME = "name"
    const val COLUMN_FEDERATION_ADDRESS = "federation_address"
    const val COLUMN_IN_HOME_SCREEN = "shown_in_home_screen"
    const val COLUMN_PUBLIC_KEY = "public_key"

    const val TABLE_STELLAR_ACCOUNTS = "stellar_accounts"

    const val TABLE_REGISTRATION_STATUS = "registration_status"
    const val COLUMN_MAIL_CONFIRMATION = "mail_confirmation"
    const val COLUMN_TFA_CONFIRMATION = "tfa_confirmation"
    const val COLUMN_MNEMONIC_CONFIRMATION = "mnemonic_confirmation"

    const val TABLE_TFA_DATA = "tfa_data"
    const val COLUMN_TFA_SECRET = "tfa_secret"
    const val COLUMN_TFA_IMAGE = "tfa_image"
}
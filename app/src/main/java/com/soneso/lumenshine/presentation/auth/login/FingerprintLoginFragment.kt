package com.soneso.lumenshine.presentation.auth.login


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.soneso.lumenshine.R
import com.soneso.lumenshine.presentation.auth.AuthFragment

/**
 * A simple [Fragment] subclass.
 *
 */
class FingerprintLoginFragment : AuthFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_fingerprint_login, container, false)

    companion object {
        const val TAG = "FingerprintLoginFragment"

        fun newInstance() = FingerprintLoginFragment()
    }
}

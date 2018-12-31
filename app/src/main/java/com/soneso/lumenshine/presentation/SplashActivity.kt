package com.soneso.lumenshine.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.soneso.lumenshine.R
import com.soneso.lumenshine.domain.data.CredentialStatus
import com.soneso.lumenshine.presentation.auth.AuthLoggedUserActivity
import com.soneso.lumenshine.presentation.auth.AuthNewUserActivity
import com.soneso.lumenshine.presentation.general.LsActivity

class SplashActivity : LsActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        subscribeToLiveData()
    }

    private fun subscribeToLiveData() {

        val viewModel = ViewModelProviders.of(this, viewModelFactory)[SplashViewModel::class.java]
        viewModel.liveIsUserLoggedIn.observe(this, Observer {
            when (it ?: CredentialStatus.NONE) {
                CredentialStatus.TFA_AND_PASS -> AuthLoggedUserActivity.startInstance(this, true)
                CredentialStatus.TFA -> AuthLoggedUserActivity.startInstance(this, false)
                CredentialStatus.NONE -> AuthNewUserActivity.startInstance(this)
            }
            finish()
        })
    }

    companion object {

        fun startInstance(context: Context) {
            context.startActivity(Intent(context, SplashActivity::class.java))
        }
    }
}

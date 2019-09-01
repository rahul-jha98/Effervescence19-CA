package org.effervescence.app19.ca.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import org.effervescence.app19.ca.R
import org.effervescence.app19.ca.fragments.LoginFragment
import org.effervescence.app19.ca.fragments.SignupFragment
import org.effervescence.app19.ca.fragments.UserDetailsInputFragment
import org.jetbrains.anko.startActivity

class FirebaseLoginActivity : AppCompatActivity() {

    private var mFirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    val RC_SIGN_IN = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_events)

        val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build())

        mAuthStateListener = FirebaseAuth.AuthStateListener {
            val user = mFirebaseAuth.currentUser
            if (user != null) {
                //Already signed in
                startActivity<HomeActivity>()
            } else {
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .build(),
                        RC_SIGN_IN
                )
            }
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        checkPermission()
        if (::mAuthStateListener.isInitialized) {
            mFirebaseAuth.addAuthStateListener(mAuthStateListener)
        }
    }

    override fun onPause() {
        super.onPause()
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener)
    }

    fun checkPermission() {
        if(Build.VERSION.SDK_INT >= 23 && checkSelfPermission(android.Manifest.permission
                        .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
        }
    }
}

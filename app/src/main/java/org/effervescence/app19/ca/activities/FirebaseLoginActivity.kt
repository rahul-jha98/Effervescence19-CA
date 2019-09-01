package org.effervescence.app19.ca.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import org.effervescence.app19.ca.R

class FirebaseLoginActivity : AppCompatActivity() {
    private var mFirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    val RC_SIGN_IN = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firebase_login)

        val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build())

        mAuthStateListener = FirebaseAuth.AuthStateListener {
            val user = mFirebaseAuth.currentUser
            if (user != null) {
                //Already signed in
            } else {
                startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN)
            }
        }
    }
    override fun onResumeFragments() {
        super.onResumeFragments()
//        checkPermission()
        if (::mAuthStateListener.isInitialized) {
            mFirebaseAuth.addAuthStateListener(mAuthStateListener)
        }
    }

    override fun onPause() {
        super.onPause()
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener)
    }

//    fun checkPermission() {
//        if(Build.VERSION.SDK_INT >= 23 && checkSelfPermission(android.Manifest.permission
//                        .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ContextCompat.startActivity(Intent(this, SplashActivity::class.java))
//            finish()
//        }
//    }
}

package org.effervescence.app19.ca.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.effervescence.app19.ca.R
import org.jetbrains.anko.startActivity

class FirebaseLoginActivity : AppCompatActivity() {
    private var mFirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private val RC_SIGN_IN = 1
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build())

        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("Users")

        mAuthStateListener = FirebaseAuth.AuthStateListener {
            val user = mFirebaseAuth.currentUser
            if (user != null) {
                //Already signed in
                startActivity<HomeActivity>()
            } else {
                startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(providers)
                            .setLogo(R.drawable.logo)
                            .setTheme(R.style.FirebaseLoginTheme)
                            .build(),
                    RC_SIGN_IN)

                val id = databaseReference.push().key
                Toast.makeText(applicationContext, "Toast", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_CANCELED) {
            finish()
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

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
import com.google.firebase.database.*
import org.effervescence.app19.ca.R
import org.effervescence.app19.ca.models.User
import org.jetbrains.anko.startActivity
import java.io.File
import javax.xml.transform.Templates

class FirebaseLoginActivity : AppCompatActivity() {
    private var mFirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private val RC_SIGN_IN = 1
    private val RC_SIGN_OUT = 7
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
                onSignedInInitialize(user.uid, user.displayName!!)
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

//                val id = databaseReference.push().key
//                Toast.makeText(applicationContext, "Toast", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        /*if (requestCode == RC_SIGN_IN && resultCode == Activity.RESULT_CANCELED) {
            finish()
        }*/
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
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

    fun onSignedInInitialize(uid: String, displayName: String) {
        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (!p0.hasChild(uid)) {
                    val user = User(name = displayName, score = 0, uid = uid, uploads = 0)
                    databaseReference.child(uid).setValue(user)
                }
            }
        })
    }

//    fun checkPermission() {
//        if(Build.VERSION.SDK_INT >= 23 && checkSelfPermission(android.Manifest.permission
//                        .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ContextCompat.startActivity(Intent(this, SplashActivity::class.java))
//            finish()
//        }
//    }
}

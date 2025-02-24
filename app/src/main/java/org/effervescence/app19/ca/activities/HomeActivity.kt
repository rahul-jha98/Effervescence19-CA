package org.effervescence.app19.ca.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.cloudinary.android.MediaManager
import com.firebase.ui.auth.AuthUI
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar_home.*
import org.effervescence.app19.ca.fragments.*
import org.effervescence.app19.ca.listeners.OnFragmentInteractionListener
import org.effervescence.app19.ca.utilities.Constants
import org.effervescence.app19.ca.utilities.MyPreferences
import org.effervescence.app19.ca.utilities.MyPreferences.set
import org.effervescence.app19.ca.utilities.UserDetails
import org.jetbrains.anko.startActivity
import com.google.firebase.auth.FirebaseAuth
import org.effervescence.app19.ca.R

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnFragmentInteractionListener {


    private var fragment: Fragment? = null
    private var fragmentClass: Class<*>? = null
    private var currentPage = 1
    lateinit var prefs: SharedPreferences



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(org.effervescence.app19.ca.R.layout.activity_home)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, org.effervescence.app19.ca.R.string.navigation_drawer_open, org.effervescence.app19.ca.R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        nav_view.getHeaderView(0).findViewById<TextView>(org.effervescence.app19.ca.R.id.userNameNav).text = UserDetails.Name

        fragmentClass = HomeFragment::class.java
        try {
            fragment = fragmentClass!!.newInstance() as Fragment
        } catch (e: Exception) {
            e.printStackTrace()
        }

        supportFragmentManager.beginTransaction()
                .replace(org.effervescence.app19.ca.R.id.mainContentSpace, fragment!!).commit()

        nav_view.setCheckedItem(org.effervescence.app19.ca.R.id.nav_home)

        prefs = MyPreferences.customPrefs(this, Constants.MY_SHARED_PREFERENCE)
    }

    override fun onBackPressed() {
        when {
            drawer_layout.isDrawerOpen(GravityCompat.START) -> drawer_layout.closeDrawer(GravityCompat.START)
            currentPage != 1 -> {
                fragmentClass = HomeFragment::class.java
                try {
                    fragment = fragmentClass!!.newInstance() as Fragment
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                nav_view.setCheckedItem(org.effervescence.app19.ca.R.id.nav_home)
                supportFragmentManager.beginTransaction()
                        .setCustomAnimations(org.effervescence.app19.ca.R.anim.push_right_out, org.effervescence.app19.ca.R.anim.push_right_in)
                        .replace(org.effervescence.app19.ca.R.id.mainContentSpace, fragment!!)
                        .commit()

                currentPage = 1
            }
            else -> super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(org.effervescence.app19.ca.R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            org.effervescence.app19.ca.R.id.logout -> {
                resetSharedPreference()
                finish()
                return true
            }
            org.effervescence.app19.ca.R.id.change_password -> changePassword()

            org.effervescence.app19.ca.R.id.edit_details -> startActivity(Intent(this, EditUserDetailsActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    private fun resetSharedPreference() {
        prefs[Constants.KEY_TOKEN] = "0"
        prefs[Constants.NAME_KEY] = Constants.NAME_DEFAULT
        prefs[Constants.COLLEGE_NAME_KEY] = Constants.COLLEGE_NAME_DEFAULT
        prefs[Constants.GENDER_KEY] = Constants.GENDER_DEFAULT
        prefs[Constants.DATE_OF_BIRTH_KEY] = Constants.DATE_OF_BIRTH_DEFAULT
        prefs[Constants.MOBILE_NO_KEY] = Constants.MOBILE_NO_DEFAULT
        prefs[Constants.REFERRAL_KEY] = Constants.REFERRAL_DEFAULT
        prefs[Constants.FB_ID_KEY] = Constants.FB_ID_DEFAULT
        prefs[Constants.EVENTS_CACHED_KEY] = Constants.EVENTS_CACHED_DEFAULT
    }

    private fun changePassword() {
        startActivity<ChangePasswordActivity>()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        val currentFragmentClass = fragmentClass
        fragmentClass = null
        var selectedPage = currentPage

        when (item.itemId) {
            org.effervescence.app19.ca.R.id.nav_home -> {
                selectedPage = 1
                fragmentClass = HomeFragment::class.java
            }

            org.effervescence.app19.ca.R.id.nav_events -> {
                selectedPage = 2
                fragmentClass = EventsFragment::class.java
            }

            org.effervescence.app19.ca.R.id.nav_leader_board -> {
                selectedPage = 3
                fragmentClass = LeaderBoardFragment::class.java
            }

            org.effervescence.app19.ca.R.id.nav_about -> {
                selectedPage = 4
                fragmentClass = AboutFragment::class.java
            }

            org.effervescence.app19.ca.R.id.nav_send -> {
                val referralCode = UserDetails.referralCode
                val sendIntent: Intent = Intent().apply {
                    type = "text/plain"
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Hey, use my referral code $referralCode " +
                            "while registering for Effervescence'18 CA app to get extra 10 points. Download now: ")
                }
                startActivity(sendIntent)
            }

            org.effervescence.app19.ca.R.id.nav_info -> {
                val infoIntent = Intent(this, ContactInfoActivity::class.java)
                infoIntent.putExtra("fragmentIndex", 0)
                startActivity(infoIntent)
            }

            org.effervescence.app19.ca.R.id.nav_contacts -> {
                val infoIntent = Intent(this, ContactInfoActivity::class.java)
                infoIntent.putExtra("fragmentIndex", 1)
                startActivity(infoIntent)
            }
        }

        if (currentPage != selectedPage) {

            var startAnimation = org.effervescence.app19.ca.R.anim.push_left_in
            var endAnimation = org.effervescence.app19.ca.R.anim.push_left_out
            if (currentPage > selectedPage) {
                startAnimation = org.effervescence.app19.ca.R.anim.push_right_out
                endAnimation = org.effervescence.app19.ca.R.anim.push_right_in
            }
            currentPage = selectedPage


            if (fragmentClass != null) {
                try {
                    fragment = fragmentClass!!.newInstance() as Fragment
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                supportFragmentManager.beginTransaction()
                        .setCustomAnimations(startAnimation, endAnimation)
                        .replace(org.effervescence.app19.ca.R.id.mainContentSpace, fragment!!)
                        .commit()
            } else {
                fragmentClass = currentFragmentClass
            }

        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun setTitleTo(title: String) {
        supportActionBar!!.title = title
    }

    override fun onDestroy() {
        super.onDestroy()

        MediaManager.get().cancelAllRequests()
    }
}

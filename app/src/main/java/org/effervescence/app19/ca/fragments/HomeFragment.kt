package org.effervescence.app19.ca.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.firebase.ui.auth.AuthUI
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_leader_board.*
import kotlinx.android.synthetic.main.list_item_leaderboard.*

import org.effervescence.app19.ca.R
import org.effervescence.app19.ca.listeners.OnFragmentInteractionListener
import org.effervescence.app19.ca.models.LeaderbooardEntry
import org.effervescence.app19.ca.utilities.Constants
import org.effervescence.app19.ca.utilities.MyPreferences
import org.effervescence.app19.ca.utilities.MyPreferences.get
import org.effervescence.app19.ca.utilities.MyPreferences.set
import org.effervescence.app19.ca.utilities.UserDetails
import org.effervescence.app19.ca.utilities.UserDetails.rank
import org.jetbrains.anko.doAsync
import org.json.JSONObject


class HomeFragment : Fragment() {

    private val list = ArrayList<LeaderbooardEntry>()
    private var listener: OnFragmentInteractionListener? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (listener != null) {
            listener!!.setTitleTo("Home")
        }
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val displayName = FirebaseAuth.getInstance().currentUser!!.displayName

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
//                Toast.makeText(context, "Worked", Toast.LENGTH_LONG).show()

                if (p0.exists()) {
                    for (i in p0.children) {
                        val user = i.getValue(LeaderbooardEntry::class.java)
                        list.add(user!!)
                    }
                }

                list.sortByDescending { it.score }
                displayReportCard(list)
            }
        })

        displayDetails(displayName!!)



        shareAppCardView.setOnClickListener {
            val referralCode = UserDetails.referralCode
            val sendIntent: Intent = Intent().apply {
                type = "text/plain"
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Hey, check out the Effervescence '19 CA App")
            }
            startActivity(sendIntent)
        }
    }

    private fun displayDetails(displayName: String) {
//        userNameTextView.text = UserDetails.userName
        greetingsTextView.text = ("Hey, " + displayName)
        collegeNameTextView.text = "Glad to have you as our campus ambassador :)"
    }

    private fun displayReportCard(list: ArrayList<LeaderbooardEntry>) {

        val id = FirebaseAuth.getInstance().currentUser!!.uid
        var position = 0
        for (i in list) {
            position++
            if (i.uid == id) {
                break
            }
        }
        val rank = position.toString()
        rank_text_view?.textSize = when (rank.length) {
            4 -> 50f
            3 -> 70f
            2 -> 90f
            else -> 100f
        }

        rank_text_view?.text = rank
        rank_loading_spin_kit?.visibility = View.GONE
        rank_inferring_text_view?.visibility = View.GONE
    }

    private fun loadUserDetails() {

        AndroidNetworking.get(Constants.REGULAR_USER_URL)
                .addHeaders(Constants.AUTHORIZATION_KEY, Constants.TOKEN_STRING + UserDetails.Token)
                .setTag("userRequest")
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        UserDetails.points = response.getInt("total_points")
                        UserDetails.rank = response.getInt("rank")
                        doAsync {
                            val prefs = MyPreferences.customPrefs(context!!, Constants.MY_SHARED_PREFERENCE)

                            prefs[Constants.NAME_KEY] = response.optString(Constants.NAME_KEY)
                            prefs[Constants.COLLEGE_NAME_KEY] = response.optString(Constants.COLLEGE_NAME_KEY)
                            prefs[Constants.DATE_OF_BIRTH_KEY] = response.optString(Constants.DATE_OF_BIRTH_KEY)
                            prefs[Constants.GENDER_KEY] = response.optString(Constants.GENDER_KEY)
                            prefs[Constants.MOBILE_NO_KEY] = response.optString(Constants.MOBILE_NO_KEY)
                            prefs[Constants.REFERRAL_KEY] = response.optString(Constants.REFERRAL_KEY)
                            prefs[Constants.FB_ID_KEY] = response.optString(Constants.FB_ID_KEY)

                            UserDetails.Name = prefs[Constants.NAME_KEY, Constants.NAME_DEFAULT]
                            UserDetails.collegeName = prefs[Constants.COLLEGE_NAME_KEY, Constants.COLLEGE_NAME_DEFAULT]
                            UserDetails.mobileNo = prefs[Constants.MOBILE_NO_KEY, Constants.MOBILE_NO_DEFAULT]
                            UserDetails.facebookId = prefs[Constants.FB_ID_KEY, Constants.FB_ID_DEFAULT]
                            UserDetails.referralCode = prefs[Constants.REFERRAL_KEY, Constants.REFERRAL_DEFAULT]

//                            displayDetails()
                        }

                        UserDetails.isFirstLaunch = false
//                        displayReportCard()
                    }

                    override fun onError(error: ANError) {

                    }
                })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        AndroidNetworking.cancel("userRequest")
    }

}

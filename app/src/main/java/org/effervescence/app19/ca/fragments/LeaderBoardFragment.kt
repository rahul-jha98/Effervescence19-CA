package org.effervescence.app19.ca.fragments

import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.SyncStateContract
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_leader_board.*

import org.effervescence.app19.ca.R
import org.effervescence.app19.ca.R.id.leaderRecylcerView
import org.effervescence.app19.ca.adapters.LeaderboardAdapter
import org.effervescence.app19.ca.listeners.OnFragmentInteractionListener
import org.effervescence.app19.ca.listeners.ScrollListener
import org.effervescence.app19.ca.models.LeaderboardList
import org.effervescence.app19.ca.models.LeaderbooardEntry
import org.effervescence.app19.ca.utilities.Constants
import org.effervescence.app19.ca.utilities.UserDetails
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject

class LeaderBoardFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null
    val list = ArrayList<LeaderbooardEntry>()
    lateinit var adapter: LeaderboardAdapter
    private lateinit var mListViewModel: LeaderboardList
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (listener != null) {
            listener!!.setTitleTo("Leader Board")
        }
        return inflater.inflate(R.layout.fragment_leader_board, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mListViewModel = ViewModelProviders.of(activity!!).get(LeaderboardList::class.java)

        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        leaderRecylcerView.layoutManager = layoutManager
        adapter = LeaderboardAdapter(context!!)
        leaderRecylcerView.adapter = adapter
        leaderRecylcerView.isNestedScrollingEnabled = true
        leaderRecylcerView.addOnScrollListener(ScrollListener(back_view))

        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("Users")


        if (mListViewModel.list == null) {
            getLeaderboardData()
        } else {
            adapter.swapList(mListViewModel.list!!)
            back_view.text = "See where you stand among campus ambassadors of other colleges"
            progressLeaderboard.visibility = View.GONE
            leaderRecylcerView.visibility = View.VISIBLE
        }

    }

    private fun getLeaderboardData() {

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                showErrorMessage()
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
                adapter.swapList(list)
                mListViewModel.list = list

                back_view.text = "See where you stand among campus ambassadors of other colleges"
                progressLeaderboard.visibility = View.GONE
                leaderRecylcerView.visibility = View.VISIBLE

            }
        })
    }

    private fun showErrorMessage() {
        back_view.text = "Couldn't fetch the leaderboard. Try checking the internet connection"
        progressLeaderboard.visibility = View.GONE
    }

    private fun populateListFromJson(response: JSONArray) {

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
        AndroidNetworking.cancel("leaderboardRequest")
    }
}

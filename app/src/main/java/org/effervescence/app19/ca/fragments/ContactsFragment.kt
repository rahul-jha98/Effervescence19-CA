package org.effervescence.app19.ca.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.PermissionChecker.checkSelfPermission
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_contacts.*

import org.effervescence.app19.ca.R
import org.effervescence.app19.ca.listeners.OnFragmentInteractionListener

class ContactsFragment : Fragment() {

    private val mRequestCode = 1
    private var listener: OnFragmentInteractionListener? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        if (listener != null) {
            listener!!.setTitleTo("Contacts")
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contacts, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        akshit_fab.setOnClickListener { callNumber("+918872800037") }
        rahul_fab.setOnClickListener { callNumber("+918452800051") }
        kartik_fab.setOnClickListener { callNumber("+917081564408") }
        anurag_fab.setOnClickListener { callNumber("+918956248701") }

        mohit_fab.setOnClickListener { callNumber("+918385990861") }
        jai_fab.setOnClickListener { callNumber("+919560503984") }
        uddept_fab.setOnClickListener { callNumber("+918294218053") }
        kajal_fab.setOnClickListener { callNumber("+918755381662") }
        keshab_fab.setOnClickListener{callNumber(("+919160000000"))}
        Tashruth_fab.setOnClickListener{callNumber(("+917893723232"))}
    }

    @SuppressLint("WrongConstant")
    private fun callNumber(number: String) {

        if (checkSelfPermission(context!!, android.Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(Array(1) {android.Manifest.permission.CALL_PHONE}, mRequestCode)
        } else {
            startActivity(Intent(Intent.ACTION_CALL).setData(Uri.parse("tel:$number")))
        }
    }

    override fun onAttach(context: Context?) {
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
    }
}

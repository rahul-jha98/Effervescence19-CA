package org.effervescence.app19.ca.fragments

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat.startActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.cloudinary.Url
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
//import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import org.effervescence.app19.ca.R
import org.effervescence.app19.ca.adapters.MyEventsRecyclerViewAdapter
import org.json.JSONArray
import org.json.JSONObject
import io.paperdb.Paper
import kotlinx.android.synthetic.main.app_bar_home.*
import kotlinx.android.synthetic.main.fragment_events.*
import kotlinx.android.synthetic.main.fragment_events_list_item.*
import kotlinx.android.synthetic.main.fragment_events_list_item.view.*
import kotlinx.android.synthetic.main.fragment_leader_board.*
import kotlinx.android.synthetic.main.nav_header_home.*
import org.effervescence.app19.ca.listeners.OnFragmentInteractionListener
import org.effervescence.app19.ca.models.EventDetails
import org.effervescence.app19.ca.models.LeaderbooardEntry
import org.effervescence.app19.ca.models.SubmissionDetalis
import org.effervescence.app19.ca.utilities.*
import org.effervescence.app19.ca.utilities.MyPreferences.get
import org.effervescence.app19.ca.utilities.MyPreferences.set
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class EventsFragment : Fragment() {

    companion object {
        const val PICK_IMAGE_REQUEST = 71
    }

    private var currentPoints = -1
    private var currentUploads = -1
    private var mPickedEventId = -1
    private var mFirebaseStorage: FirebaseStorage? = null
    private var mStorageReference: StorageReference? = null
    lateinit var filePath : Uri


    private lateinit var mPrefs: SharedPreferences
    private var mEventDetailsList = ArrayList<EventDetails>()
    private var listener: OnFragmentInteractionListener? = null
    var listAdapter: MyEventsRecyclerViewAdapter = MyEventsRecyclerViewAdapter(mEventDetailsList)
    private lateinit var database: FirebaseDatabase
    private lateinit var tasksDatabaseReference: DatabaseReference
    private lateinit var subsDatabaseReference: DatabaseReference
    private lateinit var userDatabaseReference: DatabaseReference


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mPrefs = MyPreferences.customPrefs(activity!!.applicationContext, Constants.MY_SHARED_PREFERENCE)
        if (listener != null) {
            listener!!.setTitleTo("Events")
        }
        return inflater.inflate(R.layout.fragment_events, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        events_swipe_refresh.isRefreshing = true

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        mFirebaseStorage = FirebaseStorage.getInstance()
        mStorageReference = mFirebaseStorage!!.getReference(uid)

        database = FirebaseDatabase.getInstance()
        tasksDatabaseReference = database.getReference("TASKS")
        subsDatabaseReference = database.getReference("Subs")

        buildRecyclerView()

        listAdapter.setOnClickListener(object : MyEventsRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClicked(position: Int) {
                event_description.setOnClickListener {
                    goToUrl(view.event_description.text.toString());

                }
                upload_button.setOnClickListener{
                    openImagePicker()
                }
                mPickedEventId = position + 1
                //openImagePicker()
            }
        })
        events_swipe_refresh.setOnRefreshListener { updateEventsListCache() }
    }

    private fun updateEventsListCache() {

        mEventDetailsList.clear()
        buildRecyclerView()
    }

    private fun buildRecyclerView() {
        getEventsList()

        events_list.setHasFixedSize(true)
        events_list.layoutManager = LinearLayoutManager(context)
        events_list.adapter = listAdapter
    }

    private fun getEventsList() {
        tasksDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()) {
                    for (i in p0.children) {
                        var task = i.getValue(EventDetails::class.java)
                        task?.uid = i.key
                        mEventDetailsList.add(task!!)
                    }
                }
                listAdapter.notifyDataSetChanged()
                events_swipe_refresh.isRefreshing = false
            }
        })

    }

    fun openImagePicker() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK){
            if(data == null){
                return
            }
            filePath = data!!.data
            try{
                val bitmap = MediaStore.Images.Media.getBitmap(getActivity()?.getContentResolver(),filePath)
                uploadImage()
                // imageView.setImageBitmap(bitmap)
            }catch (e: IOException){
                e.printStackTrace()
            }
        }
    }

    private fun goToUrl( urls: String){
    val openURL = Intent(android.content.Intent.ACTION_VIEW)
    openURL.data = Uri.parse(urls)
    startActivity(openURL)
}

    private fun uploadImage(){
            val progress = ProgressDialog(context).apply {
                setTitle("Uploading Picture....")
                setCancelable(false)
                setCanceledOnTouchOutside(false)
                show()
            }

            val data = FirebaseStorage.getInstance()
            var value = 0.0
            var storage = data.getReference(FirebaseAuth.getInstance().currentUser?.uid.toString()).child(System.currentTimeMillis().toString()).putFile(filePath)
                    .addOnProgressListener { taskSnapshot ->
                        value = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                        Log.v("value","value=="+value)
                        progress.setMessage("Uploaded.. " + value.toInt() + "%")
                    }
                    .addOnSuccessListener { taskSnapshot -> progress.dismiss()
                        Toast.makeText(context,"Image Uploaded",Toast.LENGTH_SHORT).show()
                        val taskId = mEventDetailsList[mPickedEventId-1].uid
                        val key = subsDatabaseReference.push().key
                        val uid = FirebaseAuth.getInstance().currentUser!!.uid
                        val sub = SubmissionDetalis(uid + "%2F" + System.currentTimeMillis().toString(), taskId + uid)

                        subsDatabaseReference.child(key!!).setValue(sub).addOnCompleteListener {
                           // Toast.makeText(context, "Image uploaded successfully!", Toast.LENGTH_LONG).show()
                        }

                        val taskPoints = mEventDetailsList[mPickedEventId-1].points

                        userDatabaseReference = database.getReference("Users").child(uid)

                        userDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                if (p0.exists()) {
                                    for (i in p0.children) {
                                        if (i.key == "score") {

                                            val x = i.getValue().toString()
                                            currentPoints = x.toInt()
                                            updateScore(currentPoints, taskPoints, uid)
                                        }

                                        if (i.key == "uploads") {
                                            val x = i.getValue().toString()
                                            currentUploads = x.toInt()
                                            updateUploads(currentUploads, uid)
                                        }
                                    }
                                }

                            }
                        })
                    }
                    .addOnFailureListener{
                        exception -> exception.printStackTrace()
                    }


    }



    private fun updateScore(score: Int, taskPoints: Int, uid: String) {
        val newScore = score + taskPoints

        val scoreRef = database.getReference("Users").child(uid).child("score")
        scoreRef.setValue(newScore)
    }

    private fun updateUploads(uploads: Int, uid: String) {
        val newUploads = uploads + 1

        val uploadsRef = database.getReference("Users").child(uid).child("uploads")
        uploadsRef.setValue(newUploads)
    }




    private fun isEventsCached(): Boolean {
        return when(mPrefs[Constants.EVENTS_CACHED_KEY, Constants.EVENTS_CACHED_DEFAULT]) {
            "true" -> true
            else -> false
        }
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
        AndroidNetworking.cancel("eventsListRequest")
        listener = null
    }
}
package org.effervescence.app19.ca.fragments

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
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
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
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
import kotlinx.android.synthetic.main.nav_header_home.*
import org.effervescence.app19.ca.listeners.OnFragmentInteractionListener
import org.effervescence.app19.ca.models.EventDetails
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

    private var mPickedEventId = -1
    private var mFirebaseStorage: FirebaseStorage? = null
    private var mStorageReference: StorageReference? = null
    private var filePath: Uri? = null

    private lateinit var mPrefs: SharedPreferences
    private var mEventDetailsList = ArrayList<EventDetails>()
    private var listener: OnFragmentInteractionListener? = null
    var listAdapter: MyEventsRecyclerViewAdapter = MyEventsRecyclerViewAdapter(mEventDetailsList)
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mPrefs = MyPreferences.customPrefs(activity!!.applicationContext, Constants.MY_SHARED_PREFERENCE)
        if (listener != null) {
            listener!!.setTitleTo("Events")
        }
        return inflater.inflate(R.layout.fragment_events, container, false)
        //  view.upload_button.setOnClickListener{uploadImage()};

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        events_swipe_refresh.isRefreshing = true

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        mFirebaseStorage = FirebaseStorage.getInstance()
        mStorageReference = mFirebaseStorage!!.getReference(uid)

        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("TASKS")
        buildRecyclerView()
        listAdapter.setOnClickListener(object : MyEventsRecyclerViewAdapter.OnItemClickListener {
            override fun onItemClicked(position: Int) {
                mPickedEventId = position + 1
                openImagePicker()
            }
        })
        events_swipe_refresh.setOnRefreshListener { updateEventsListCache() }
    }

    private fun updateEventsListCache() {
//        doAsync {
//            Paper.book().delete(Constants.EVENTS_CACHED_KEY)
//        }
//        mPrefs[Constants.EVENTS_CACHED_KEY] = Constants.EVENTS_CACHED_DEFAULT
//        getEventsList()
//        Handler().postDelayed(
//                {
//                    events_swipe_refresh.isRefreshing = false
//                    // This method will be executed once the timer is over
//                },
//                1000 // value in milliseconds
//        )
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
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()) {
                    for (i in p0.children) {
                        val task = i.getValue(EventDetails::class.java)
                        mEventDetailsList.add(task!!)
                    }
                }
                listAdapter.notifyDataSetChanged()
                events_swipe_refresh.isRefreshing = false
            }
        })

//        var i = 0
//        if (isEventsCached()) {
//            doAsync {
//                mEventDetailsList = ArrayList(Paper.book()
//                        .read<ArrayList<EventDetails>>(Constants.EVENTS_CACHED_KEY))
//                uiThread {
//                    listAdapter.swapList(mEventDetailsList)
//                    listAdapter.notifyDataSetChanged()
//                    events_swipe_refresh.isRefreshing = false
//                }
//            }
//        } else {
//            mPrefs[Constants.EVENTS_CACHED_KEY] = "true"
//            AndroidNetworking.get(Constants.EVENTS_LIST_URL)
//                    .setTag("eventsListRequest")
//                    .build()
//                    .getAsJSONArray(object : JSONArrayRequestListener {
//                        override fun onResponse(response: JSONArray) {
//                            if (response.length() > 0) {
//                                mEventDetailsList.clear()
//                                while (response.length() > i) {
////                                    mEventDetailsList.add(createEventDetailsObject(response.getJSONObject(i++)))
//                                }
//                            }
//                            listAdapter.notifyDataSetChanged()
//                            events_swipe_refresh.isRefreshing = false
//                            doAsync {
//                                Paper.book().write(Constants.EVENTS_CACHED_KEY, mEventDetailsList)
//                            }
//                        }
//
//                        override fun onError(error: ANError) {
//                            Log.e("EventsFragment", error.errorBody)
//                            events_swipe_refresh.isRefreshing = false
//                            Toast.makeText(context, "Connection Broke :(", Toast.LENGTH_SHORT).show()
//                        }
//                    })
//        }
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
            filePath = data.data
            try{
                val bitmap = MediaStore.Images.Media.getBitmap(getActivity()?.getContentResolver(),filePath)
                uploadImage()
                // imageView.setImageBitmap(bitmap)
            }catch (e: IOException){
                e.printStackTrace()
            }
        }
    }
//    private fun addUploadRecordToDb(uri: String){
//        val db = FirebaseFirestore.getInstance()
//
//        val data = HashMap<String, Any>()
//        data["imageUrl"] = uri
//
//        db.collection("chat_room")
//                .add(data)
//                .addOnSuccessListener { documentReference ->
//                    //Toast.makeText(activity, "Saved to DB", Toast.LENGTH_LONG).show()
//                }
//                .addOnFailureListener { e ->
//                    // Toast.makeText(activity, "Error saving to DB", Toast.LENGTH_LONG).show()
//                }
//    }

    private fun uploadImage(){
//        openImagePicker();
        if(filePath != null){
            val ref = mStorageReference?.child("/" + UUID.randomUUID().toString())
            val uploadTask = ref?.putFile(filePath!!)

            val urlTask = uploadTask?.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            })?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
//                    addUploadRecordToDb(downloadUri.toString())
                } else {
                    // Handle failures
                }
            }?.addOnFailureListener{

            }
        }else{
            Toast.makeText(activity, "Please Upload an Image", Toast.LENGTH_SHORT).show()
        }
    }


    /* private fun getTitleFromUri(uri: Uri): String {
         var result = ""
         if (uri.scheme == "content") {
             val cursor = activity?.contentResolver?.query(uri, null, null,
                     null, null)
             cursor.use { cursor ->
                 if (cursor != null && cursor.moveToFirst()) {
                     val id = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                     if (id != -1) {
                         result = cursor.getString(id)
                     }
                 }
             }
         }
         if (result == "") {
             result = uri.path
             val cut = result.lastIndexOf('/')
             if (cut != -1)
                 result = result.substring(cut + 1)
         }
         return result
     }*/

//    fun createEventDetailsObject(eventJSONObject: JSONObject): EventDetails {
//
//        return EventDetails(eventJSONObject.optInt(Constants.EVENT_ID_KEY),
//                eventJSONObject.optString(Constants.EVENT_NAME_KEY),
//                eventJSONObject.optString(Constants.EVENT_DESCRIPTION_KEY),
//                eventJSONObject.optInt(Constants.EVENT_PRIZE_KEY),
//                eventJSONObject.optInt(Constants.EVENT_POINTS_KEY),
//                eventJSONObject.optInt(Constants.EVENT_FEE_KEY))
//    }

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
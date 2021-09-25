package com.bignerdranch.android.criminalintent

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat.format
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_TIME = 1
private const val REQUEST_CONTACT = 2
private const val REQUEST_PHOTO = 3
private const val DATE_FORMAT = "EEE, MMM, dd"
//private const val TAG = "CrimeFragment"

class CrimeFragment : Fragment(), DataPickerFragment.Callbacks, TimePickerFragment.Callbacks{

    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var photoView: ImageView
    private lateinit var photoButton: ImageButton
    private lateinit var solvedCheckBox: CheckBox

    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime,container,false)
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        timeButton = view.findViewById(R.id.crime_time) as Button
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect)
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        photoView = view.findViewById(R.id.crime_photo) as ImageView
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton

      //  dateButton.apply {
        //    text = crime.date.toString()
          //  isEnabled = false
        //}
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
                viewLifecycleOwner,
                Observer{crime ->
                    crime?.let {
                        this.crime = crime
                        photoFile = crimeDetailViewModel.getPhotoFile(crime)
                        photoUri = FileProvider.getUriForFile(requireActivity(),
                          "com.bignerdranch.android.criminalintent.fileprovider",
                            photoFile)
                        updateUI()}
                }
        )
    }

    override fun onStart() {
        super.onStart()
        val titleWatcher = object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                crime.title = s.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        }
        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener{_, isChecked  ->
                                    crime.isSolved = isChecked
            }
            dateButton.setOnClickListener {
                DataPickerFragment.newInstance(crime.date).apply {
                    setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                    show(this@CrimeFragment.parentFragmentManager, DIALOG_DATE)
                }

            }

            timeButton.setOnClickListener {
                TimePickerFragment.newInstance(crime.date).apply {
                   setTargetFragment(this@CrimeFragment, REQUEST_TIME)
                    show(this@CrimeFragment.parentFragmentManager, DIALOG_TIME)
                }

            }

            reportButton.setOnClickListener {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                    putExtra(
                            Intent.EXTRA_SUBJECT,
                            getString(R.string.crime_report_subject))
                }.also { intent ->
                    val chooserIntent = Intent.createChooser(intent,getString(R.string.send_report))
                    startActivity(chooserIntent)
                }
            }

            suspectButton.apply {
                val picContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                setOnClickListener {
                    startActivityForResult(picContactIntent, REQUEST_CONTACT)
                }
                val packageManager: PackageManager = requireActivity().packageManager
                val resolvedActivity: ResolveInfo? =
                        packageManager.resolveActivity(picContactIntent,PackageManager.MATCH_DEFAULT_ONLY)
                if (resolvedActivity == null ) {
                    isEnabled = false
                }

            }

            photoButton.apply {
                val packageManager: PackageManager = requireActivity().packageManager

                val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                val resolvedActivity: ResolveInfo? =
                    packageManager.resolveActivity(captureImage,PackageManager.MATCH_DEFAULT_ONLY)

                if (resolvedActivity == null ) {
                    isEnabled = false
                }
                setOnClickListener {
                    captureImage.putExtra(MediaStore.EXTRA_OUTPUT,photoUri)

                    val cameraActivities: List <ResolveInfo> =
                        packageManager.queryIntentActivities(captureImage,
                            PackageManager.MATCH_DEFAULT_ONLY)

                    for (cameraActivity in cameraActivities){
                        requireActivity().grantUriPermission(
                            cameraActivity.activityInfo.packageName,
                            photoUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    }
                    startActivityForResult(captureImage, REQUEST_CONTACT)
                }
            }
        }
    }



    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri,
        Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }


    private fun updateUI(){
        val crimeDate = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
                .format(this.crime.date)
        dateButton.text = crimeDate
        val crimeTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(this.crime.date)
        titleField.setText(crime.title)
        timeButton.text = crimeTime
        dateButton.text = crime.date.toString()
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }
        if (crime.suspect.isNotEmpty()){
            suspectButton.text = crime.suspect
        }
        updatePhotoView()
    }

     private fun updatePhotoView(){
         if (photoFile.exists()){
            val bitmap = getScaledBitmap(photoFile.path,requireActivity())
             photoView.setImageBitmap(bitmap)
         }else{
             photoView.setImageDrawable(null)
         }
     }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when{
            resultCode != Activity.RESULT_OK -> return

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                val cursor = contactUri?.let {
                    requireActivity().contentResolver
                            .query(it, queryFields,null,null,null)
                }
                cursor?.use {
                    if (it.count == 0){
                        return
                    }
                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    crimeDetailViewModel.saveCrime(crime)
                    suspectButton.text = suspect
                }
            }
            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                updatePhotoView()
            }
        }
    }


    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }
        val dateString = format(DATE_FORMAT,crime.date).toString()
        val suspect = if (crime.suspect.isBlank()){
            getString(R.string.rime_report_no_suspect)
        }else{
            getString(R.string.crime_report_suspect,crime.suspect)
        }
        return getString(R.string.crime_report,crime.title, dateString,solvedString,suspect)

    }



    companion object{
        fun newInstance(crimeId: UUID): CrimeFragment{
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
            }
        }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }



    override fun onTimeSelected(date: Date) {
        crime.date = date
        updateUI()
        
    }
}
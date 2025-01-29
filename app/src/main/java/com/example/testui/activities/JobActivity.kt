package com.example.testui.activities


import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testui.DayViewContainer
import com.example.testui.LoadingAnim
import com.example.testui.R
import com.example.testui.adapters.JobAdapter
import com.example.testui.bottom_fragments.HomeFragment.Companion.ACTION_CLOSED
import com.example.testui.bottom_fragments.HomeFragment.Companion.ACTION_CURRENT
import com.example.testui.bottom_fragments.HomeFragment.Companion.ACTION_PENDING
import com.example.testui.bottom_fragments.HomeFragment.Companion.ACTION_TOTAL
import com.example.testui.databinding.ActivityJobBinding
import com.example.testui.models.JobData
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.view.WeekCalendarView
import com.kizitonwose.calendar.view.WeekDayBinder
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class JobActivity : LoadingAnim() {

    private lateinit var binding:ActivityJobBinding
    private lateinit var calendarView:WeekCalendarView
    @RequiresApi(Build.VERSION_CODES.O)
    private var selectedDate: LocalDate = LocalDate.now()

    private lateinit var totalBtn: MaterialButton
    private lateinit var currentBtn: MaterialButton
    private lateinit var pendingBtn: MaterialButton
    private lateinit var closedBtn: MaterialButton

    private lateinit var jobAdapter: JobAdapter
    private lateinit var jobList: MutableList<JobData>

    private lateinit var firestore:FirebaseFirestore

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityJobBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainTotalJobs)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firestore = FirebaseFirestore.getInstance()

        totalBtn = binding.totalBtn
        currentBtn = binding.currentBtn
        pendingBtn = binding.pendingBtn
        closedBtn = binding.closedBtn

        val toolbar = findViewById<Toolbar>(R.id.toolbar1)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_left)
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.black))

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        toolbar.inflateMenu(R.menu.main_menu)

        toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.profile_icon -> {
                    Toast.makeText(this, "Profile Icon clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        val recyclerView:RecyclerView = binding.totalJobsRecyclerview
        recyclerView.layoutManager = LinearLayoutManager(this)

        jobList = mutableListOf()
        jobAdapter = JobAdapter(jobList)
        recyclerView.adapter = jobAdapter

        val action = intent.getStringExtra("ACTION") ?: ACTION_TOTAL

        fetchDataFromFireStore(action)

        calendarView = binding.calendarView

        val currentDate = LocalDate.now()
        val firstDayOfMonth = currentDate.withDayOfMonth(1)
        val lastDayOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth())

        val formatter = DateTimeFormatter.ofPattern("MMM yyyy")
        val formattedDate = currentDate.format(formatter)
        binding.currentMonthTxt.text = formattedDate

        calendarView.setup(
            startDate = firstDayOfMonth,
            endDate = lastDayOfMonth,
            firstDayOfWeek = DayOfWeek.MONDAY
        )

        calendarView.smoothScrollToDate(LocalDate.now())

        calendarView.dayBinder = object : WeekDayBinder<DayViewContainer>{

            override fun create(view: View): DayViewContainer{
                return DayViewContainer(view)
            }

            override fun bind(container: DayViewContainer, data: WeekDay) {
                container.dayText.text = data.date.dayOfWeek.name.take(3)
                container.dateText.text = data.date.dayOfMonth.toString()

                if(data.date == selectedDate){
                    container.dayText.setTextColor(getColor(R.color.white))
                    container.dateText.setTextColor(getColor(R.color.white))
                    container.calendarLayout.setBackgroundResource(R.drawable.calendar_days_bg)
                }else{
                    container.dayText.setTextColor(getColor(R.color.calendar_text))
                    container.dateText.setTextColor(getColor(R.color.calendar_text))
                    container.calendarLayout.setBackgroundColor(getColor(R.color.white))
                }

                container.view.setOnClickListener {
                    val previousSelectedDate = selectedDate
                    selectedDate = data.date

                    calendarView.notifyDateChanged(previousSelectedDate)
                    calendarView.notifyDateChanged(selectedDate)
                }
            }


        }


        handleButtonClick(action)

        totalBtn.setOnClickListener { handleButtonClick(ACTION_TOTAL) }
        currentBtn.setOnClickListener { handleButtonClick(ACTION_CURRENT) }
        pendingBtn.setOnClickListener { handleButtonClick(ACTION_PENDING) }
        closedBtn.setOnClickListener { handleButtonClick(ACTION_CLOSED) }


        jobAdapter.onItemClick = { job ->

            firestore.collection("jobs_data")
                .whereEqualTo("jobId", job.jobId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents[0]
                        val progressState = document.getString("progressState")

//                        val intent = when (progressState) {
//                            "UploadPic" -> Intent(this, UploadPicActivity::class.java)
//                            "UploadDocs" -> Intent(this, UploadDocsActivity::class.java)
//                            "UploadVideo" -> Intent(this, VideoActivity2::class.java)
//                            else -> Intent(this, PictureActivity::class.java)
//                        }

                        val intent = Intent(this, CheckInActivity::class.java)
                        intent.putExtra("jobId", job.jobId)
                        intent.putExtra("companyName", job.companyName)
                        intent.putExtra("contactName", job.contactName)
                        intent.putExtra("workAssigned", job.workAssigned)
                        intent.putExtra("address", job.address)

                        // Save jobId for further use
                        val sharedPreferences = getSharedPreferences("job_data", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("jobId", job.jobId)
                        editor.apply()

                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "No job found with jobId: ${job.jobId}", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to fetch job progress", Toast.LENGTH_SHORT).show()
                }
        }
    }



    private fun formattedDate():String{
        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchDataFromFireStore(action:String) {

        showLoadingDialog()

        val jobsCollection = firestore.collection("jobs_data")

        val query = when (action) {

            ACTION_CURRENT -> {
                jobsCollection
                    .whereEqualTo("createdAt",formattedDate())
                    .whereEqualTo("jobStatus", "Current")
            }
            ACTION_TOTAL -> {
                jobsCollection
            }
            ACTION_CLOSED -> {
                jobsCollection
                    .whereEqualTo("jobStatus","Closed")
            }
            else -> {
                jobsCollection
            }
        }

        query.get()
            .addOnSuccessListener { documents ->
                jobList.clear()
                for(document in documents){
                    val job  = document.toObject(JobData::class.java)
                    jobList.add(job)
                }

                jobAdapter.notifyDataSetChanged()

                if(jobList.isEmpty()){
                    binding.noJobTxt.visibility = View.VISIBLE
                }else{
                    binding.noJobTxt.visibility = View.GONE
                }

                hideLoadingDialog()
            }

            .addOnFailureListener { exception ->

                hideLoadingDialog()
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return true
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleButtonClick(action: String) {

        resetButtonColors()

        when (action) {
            ACTION_TOTAL -> {
                totalBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.light_orange))
                totalBtn.setTextColor(getColor(R.color.white))
                performTotalOperation()
            }
            ACTION_CURRENT -> {
                currentBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.light_orange))
                currentBtn.setTextColor(getColor(R.color.white))
                performCurrentOperation()
            }
            ACTION_PENDING -> {
                pendingBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.light_orange))
                pendingBtn.setTextColor(getColor(R.color.white))
                performPendingOperation()
            }
            ACTION_CLOSED -> {
                closedBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.light_orange))
                closedBtn.setTextColor(getColor(R.color.white))
                performClosedOperation()
            }
        }
    }

    private fun resetButtonColors() {
        val defaultColor = ContextCompat.getColor(this, R.color.job_btn_color)
        totalBtn.setBackgroundColor(defaultColor)
        currentBtn.setBackgroundColor(defaultColor)
        pendingBtn.setBackgroundColor(defaultColor)
        closedBtn.setBackgroundColor(defaultColor)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun performTotalOperation() {
        fetchDataFromFireStore(ACTION_TOTAL)
        jobAdapter.setShowProceedBtn(false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun performCurrentOperation() {
        fetchDataFromFireStore(ACTION_CURRENT)
        jobAdapter.setShowProceedBtn(true)
    }

    private fun performPendingOperation() {
        // Logic for "Pending" button
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun performClosedOperation() {
        fetchDataFromFireStore(ACTION_CLOSED)
        jobAdapter.setShowProceedBtn(false)
    }

//    private fun getStartOfToday(): Date {
//        val calendar = Calendar.getInstance()
//        calendar.set(Calendar.HOUR_OF_DAY, 0)
//        calendar.set(Calendar.MINUTE, 0)
//        calendar.set(Calendar.SECOND, 0)
//        calendar.set(Calendar.MILLISECOND, 0)
//        return calendar.time
//    }
//
//    private fun getEndOfToday(): Date {
//        val calendar = Calendar.getInstance()
//        calendar.set(Calendar.HOUR_OF_DAY, 23)
//        calendar.set(Calendar.MINUTE, 59)
//        calendar.set(Calendar.SECOND, 59)
//        calendar.set(Calendar.MILLISECOND, 999)
//        return calendar.time
//    }
}
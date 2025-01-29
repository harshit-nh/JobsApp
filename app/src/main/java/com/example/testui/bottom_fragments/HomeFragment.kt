package com.example.testui.bottom_fragments

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.testui.R
import com.example.testui.activities.JobActivity
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.card.MaterialCardView


class HomeFragment : Fragment() {

    private lateinit var pieChart: PieChart
    private lateinit var linearLayout: LinearLayout
    private lateinit var dayText: TextView
    private lateinit var weekText: TextView
    private lateinit var monthText: TextView
    private lateinit var yearText: TextView
    private lateinit var allText: TextView



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        pieChart = view.findViewById(R.id.pieChart)

        val totalCard = view.findViewById<MaterialCardView>(R.id.totalJobCard)
        val currentCard = view.findViewById<MaterialCardView>(R.id.currentJobCard)
        val pendingCard = view.findViewById<MaterialCardView>(R.id.pendingJobCard)
        val closedCard = view.findViewById<MaterialCardView>(R.id.closeJobCard)

        linearLayout = view.findViewById(R.id.horizontal_toggle_container)

        dayText = view.findViewById(R.id.days_txt)
        weekText = view.findViewById(R.id.week_txt)
        monthText = view.findViewById(R.id.month_txt)
        yearText = view.findViewById(R.id.year_txt)
        allText = view.findViewById(R.id.all_txt)


        highlightOption(dayText)
        loadPieChart("Day")

        dayText.setOnClickListener { onOptionSelected(dayText,"Day") }
        weekText.setOnClickListener { onOptionSelected(weekText,"Week") }
        monthText.setOnClickListener { onOptionSelected(monthText,"Month") }
        yearText.setOnClickListener { onOptionSelected(yearText,"Year") }
        allText.setOnClickListener { onOptionSelected(allText,"All") }

        totalCard.setOnClickListener { navigateToJobsActivity("TOTAL") }
        currentCard.setOnClickListener { navigateToJobsActivity("CURRENT") }
        pendingCard.setOnClickListener { navigateToJobsActivity("PENDING") }
        closedCard.setOnClickListener { navigateToJobsActivity("CLOSED") }

        return view
    }

    private fun onOptionSelected(selectedText: TextView, option: String){
        resetOptionsColor()
        highlightOption(selectedText)

        loadPieChart(option)
    }

    private fun highlightOption(textView:TextView){
        textView.setTextColor(Color.parseColor("#4BA6E9"))
        textView.textSize = 20f
    }

    private fun resetOptionsColor(){
        dayText.setTextColor(Color.BLACK)
        dayText.textSize = 18f

        weekText.setTextColor(Color.BLACK)
        weekText.textSize = 18f

        monthText.setTextColor(Color.BLACK)
        monthText.textSize = 18f

        yearText.setTextColor(Color.BLACK)
        yearText.textSize = 18f

        allText.setTextColor(Color.BLACK)
        allText.textSize = 18f
    }

    private fun navigateToJobsActivity(action:String){
        val intent = Intent(requireContext(),JobActivity::class.java)
        when(action){
            "TOTAL" -> intent.putExtra("ACTION", ACTION_TOTAL)
            "CURRENT" -> intent.putExtra("ACTION", ACTION_CURRENT)
            "PENDING" -> intent.putExtra("ACTION", ACTION_PENDING)
            "CLOSED" -> intent.putExtra("ACTION", ACTION_CLOSED)

        }
        startActivity(intent)
    }


    private fun loadPieChart(option:String){

        // on below line we are setting user percent value,
        // setting description as enabled and offset for pie chart
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)

        // on below line we are setting drag for our pie chart
        pieChart.setDragDecelerationFrictionCoef(0.95f)

        // on below line we are setting hole
        // and hole color for pie chart
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)

        // on below line we are setting circle color and alpha
        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)

        // on  below line we are setting hole radius
        pieChart.holeRadius = 70f
        pieChart.transparentCircleRadius = 70f

        // on below line we are setting center text
        pieChart.setDrawCenterText(true)
        pieChart.centerText = "60%"
        pieChart.setCenterTextSize(30F)
        pieChart.setCenterTextTypeface(Typeface.MONOSPACE)

        // on below line we are setting
        // rotation for our pie chart
        pieChart.setRotationAngle(0f)

        // enable rotation of the pieChart by touch
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true

        // on below line we are setting animation for our pie chart
        pieChart.animateY(1400, Easing.EaseInOutQuad)

        // on below line we are disabling our legend for pie chart
        pieChart.legend.isEnabled = false
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(12f)

        // on below line we are creating array list and
        // adding data to it to display in pie chart
        val entries: ArrayList<PieEntry> = ArrayList()

        when(option){
            "Day" ->{
                entries.add(PieEntry(60f))
                entries.add(PieEntry(20f))
                entries.add(PieEntry(10f))
                entries.add(PieEntry(10f))
            }
            "Week" ->{
                entries.add(PieEntry(20f))
                entries.add(PieEntry(10f))
                entries.add(PieEntry(60f))
                entries.add(PieEntry(10f))
            }
            "Month" ->{
                entries.add(PieEntry(10f))
                entries.add(PieEntry(20f))
                entries.add(PieEntry(30f))
                entries.add(PieEntry(40f))
            }
            "Year" ->{
                entries.add(PieEntry(10f))
                entries.add(PieEntry(40f))
                entries.add(PieEntry(30f))
                entries.add(PieEntry(20f))
            }
            "All" ->{
                entries.add(PieEntry(10f))
                entries.add(PieEntry(30f))
                entries.add(PieEntry(10f))
                entries.add(PieEntry(50f))
            }
        }


        // on below line we are setting pie data set
        val dataSet = PieDataSet(entries, "Mobile OS")

        // on below line we are setting icons.
        dataSet.setDrawIcons(false)

        // on below line we are setting slice for pie
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f

        // add a lot of colors to list
        val colors: ArrayList<Int> = ArrayList()
        colors.add(resources.getColor(R.color.current_color))
        colors.add(resources.getColor(R.color.pending_color))
        colors.add(resources.getColor(R.color.total_color))
        colors.add(resources.getColor(R.color.light_orange))

        // on below line we are setting colors.
        dataSet.colors = colors

        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 7f

        // on below line we are setting pie data set
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter())
        pieChart.setData(data)

        // undo all highlights
        pieChart.highlightValues(null)

        // loading chart
        pieChart.invalidate()
    }

    companion object {
        const val ACTION_TOTAL = "TOTAL"
        const val ACTION_CURRENT = "CURRENT"
        const val ACTION_PENDING = "PENDING"
        const val ACTION_CLOSED = "CLOSED"
    }
}
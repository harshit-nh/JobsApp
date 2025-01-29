package com.example.testui

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.kizitonwose.calendar.view.ViewContainer

class DayViewContainer(view: View): ViewContainer(view) {

    val dayText:TextView = view.findViewById(R.id.dayText)
    val dateText:TextView = view.findViewById(R.id.dateText)
    val calendarLayout:LinearLayout = view.findViewById(R.id.calendar_layout)
}
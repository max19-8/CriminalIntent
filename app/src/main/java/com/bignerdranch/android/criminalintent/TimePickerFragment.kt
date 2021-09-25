package com.bignerdranch.android.criminalintent

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*
import java.util.Calendar.*

private const val ARG_TIME = "time"

class TimePickerFragment : DialogFragment() {

    interface Callbacks {
        fun onTimeSelected(date: Date)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val calendarYear = calendar.get(YEAR)
        val calendarMonth = calendar.get(MONTH)
        val calendarDay = calendar.get(DAY_OF_MONTH)
        val timeListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            val resultTime: Date = GregorianCalendar(calendarYear, calendarMonth, calendarDay, hour, minute).time
            targetFragment?.let {fragment ->
                (fragment as Callbacks).onTimeSelected(resultTime)
            }
        }


        val date = arguments?.getSerializable(ARG_TIME) as Date
        calendar.time = date
        val initialHours = calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinutes = calendar.get(Calendar.MINUTE)
        return TimePickerDialog(
                requireContext(),
                timeListener,
                initialHours,
                initialMinutes,
                true
        )
    }

    companion object{
        fun newInstance(date: Date): TimePickerFragment{
            val args = Bundle().apply {
                putSerializable(ARG_TIME,date)
            }
            return TimePickerFragment().apply {
                arguments = args
            }

        }
    }
}

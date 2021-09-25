package com.bignerdranch.android.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_DATE = "date"

class DataPickerFragment : DialogFragment() {

    interface Callbacks{
        fun onDateSelected(date: Date)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dataListener = DatePickerDialog.OnDateSetListener { _, year, month, day   ->
            val resultDate: Date = GregorianCalendar(year,month,day).time
            targetFragment?.let { fragment ->
                (fragment as Callbacks).onDateSelected(resultDate)
            }

        }
        val calendar = Calendar.getInstance()
        val date = arguments?.getSerializable(ARG_DATE) as Date
        calendar.time = date
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(
                requireContext(),
                dataListener,
                initialYear,
                initialMonth,
                initialDay
        )
    }
    companion object{
        fun newInstance(date: Date): DataPickerFragment{
            val args = Bundle().apply {
                putSerializable(ARG_DATE,date)
            }
            return DataPickerFragment().apply {
                arguments = args
            }

        }
    }
}
package com.bignerdranch.android.criminalintent

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.IllegalArgumentException
import java.util.*
import androidx.recyclerview.widget.ListAdapter

//private const val TAG = "CrimeListFragment"

class CrimeListFragment: Fragment() {

    interface Callbacks{
        fun onCrimeSelected (crimeId: UUID)
    }
    private  var callbacks: Callbacks? = null
    private lateinit var crimeRecyclerView: RecyclerView
    private lateinit var emptyView: TextView
  //  private var adapter: CrimeAdapter? = CrimeAdapter(emptyList())

    private val crimeListViewModel: CrimeListViewModel by lazy {
        ViewModelProvider(this).get(CrimeListViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       val view = inflater.inflate(R.layout.fragment_crime_list,container,false)
        emptyView = view.findViewById(R.id.emptyView)
        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView
        crimeRecyclerView.layoutManager = LinearLayoutManager(context)
        crimeRecyclerView.adapter = CrimeAdapter()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimesListLiveData.observe(
            viewLifecycleOwner,
            Observer {crimes ->
                //adapter = CrimeAdapter(crimes)
                         // crimeRecyclerView.adapter = adapter
                //(crimeRecyclerView.adapter as CrimeAdapter).submitList(crimes)
               crimes?.let {
                 //  (crimeRecyclerView.adapter as CrimeAdapter).submitList(crimes)
                    //updateUI(crimes)
                  updateUI(crimes)
                }
            }
        )
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.new_crime ->{
                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }

    private fun updateUI(crimes: List<Crime>){
       // (crimeRecyclerView.adapter as CrimeAdapter).submitList(crimes)
       // adapter?.submitList(crimes)
       // Log.d(TAG , "UPDATEUI")

       // adapter = CrimeAdapter(crimes)
       // crimeRecyclerView.adapter = CrimeAdapter(crimes)

        (crimeRecyclerView.adapter as CrimeAdapter).submitList(crimes)
        if (crimes.isEmpty()) {
            crimeRecyclerView.visibility = View.INVISIBLE
            emptyView.visibility =  View.VISIBLE
        }else{
            crimeRecyclerView.visibility = View.VISIBLE
            emptyView.visibility =  View.INVISIBLE
        }
      //  adapter?.submitList(crimes)

      // crimeRecyclerView.adapter = adapter
    }


    private abstract class CrimeHolder(view: View)
        :RecyclerView.ViewHolder(view), View.OnClickListener {
        var crime = Crime()
        val titleTextView: TextView = view.findViewById(R.id.crime_title)
        val dateTextView: TextView = view.findViewById(R.id.crime_date)
        val solvedImageView: ImageView = view.findViewById(R.id.crime_solved)



    }
    private inner class NormalCrimeHolder(view: View)
        : CrimeHolder(view),View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        fun bind (crime:Crime){
            this.crime = crime
            titleTextView.text = this.crime.title
         //   dateTextView.text = this.crime.date.toString()
            dateTextView.text = DateFormat.format("EEE dd MMM yyyy, hh:mm", this.crime.date)
            solvedImageView.visibility = if (crime.isSolved){
                View.VISIBLE
            }else{
                View.INVISIBLE
            }
        }


        override fun onClick(v: View?) {
            callbacks?.onCrimeSelected(crime.id)
        }
    }

    private inner class CrimePoliceHolder(view: View)
        :CrimeHolder(view),View.OnClickListener{

        val policeButton: Button = view.findViewById(R.id.police_button)
        init {
            itemView.setOnClickListener(this)
        }

        fun bind (crime:Crime){
            this.crime = crime
            titleTextView.text = this.crime.title
           // dateTextView.text = this.crime.date.toString()
           dateTextView.text = DateFormat.format("EEE d MMM yyyy HH:mm",this.crime.date)
            policeButton.setOnClickListener {  Toast.makeText(context,"call to police",Toast.LENGTH_SHORT).show() }
            solvedImageView.visibility = if (crime.isSolved){
                View.VISIBLE
            }else{
                View.INVISIBLE
            }
        }

        override fun onClick(v: View?) {
            Toast.makeText(context,"${crime.title} pressed",Toast.LENGTH_SHORT).show()
        }
    }


    private inner class CrimeAdapter()
        :ListAdapter<Crime,CrimeHolder>(CrimeDiffUtil()){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            return when (viewType) {
                0 -> {

                    val view = layoutInflater.inflate(R.layout.list_item_crime, parent, false)
                    NormalCrimeHolder(view)
                }
                else -> {
                    val view = layoutInflater.inflate(R.layout.list_item_crime_call_police, parent, false)
                    CrimePoliceHolder(view)
                }
            }
        }

     //  override fun getItemViewType(position: Int): Int {
    //        val crime = crimes[position]
      //      return when (crime.requiresPolice) {
       //        true -> 1
       //         else -> 0
     //       }
     //   }

     //   override fun getItemCount() = crimes.size

        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
           // val crime =
                   // crimes[position]
            when(holder){
                is NormalCrimeHolder -> holder.bind(getItem(position))
                is CrimePoliceHolder -> holder.bind(getItem(position))
                else -> throw  IllegalArgumentException()
            }

        }

    }

    class CrimeDiffUtil : DiffUtil.ItemCallback<Crime>(){
        override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {

            return areItemsTheSame(oldItem, newItem)
        }
    }

    companion object{
        fun newInstance(): CrimeListFragment{
            return CrimeListFragment()
        }
    }
}
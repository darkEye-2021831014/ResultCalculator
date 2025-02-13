package com.example.cgpa

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RecordsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecordsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val viewModel by activityViewModels<SharedViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_records, container, false)

        //three dot operation
        val menu:ImageButton = view.findViewById(R.id.menu)
        val drawer:DrawerLayout = view.findViewById(R.id.drawer_Layout_Records)
        menu.setOnClickListener {
            drawer.openDrawer(GravityCompat.START) // Opens from the left
        }

        //inside three dot menu
        //result calculator
        view.findViewById<Button>(R.id.resultCalculator).setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java);
            startActivity(intent);
            requireActivity().finish();
        }
        //exit app
        view.findViewById<Button>(R.id.exit).setOnClickListener{
            requireActivity().finishAffinity()
        }


        //get data
        val recyclerView: RecyclerView = view.findViewById(R.id.recordContainer)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.userData.observe(viewLifecycleOwner) { data ->
            val items:MutableList<Any> = mutableListOf();
            var date:String = "n/a";

            val tmp:MutableList<Item> = mutableListOf();
            var expense:Long = 0L
            var income =0L
            var dateText:String="";

            for(item in data) {
                if(item.date!=date) {
                    if(date != "n/a") {
                        if(expense==0L)
                            items.add(Date(dateText,"Income: $income"))
                        else if(income ==0L)
                            items.add(Date(dateText,"Expense: $expense"))
                        else
                            items.add(Date(dateText,"Expense: $expense  Income: $income"))
                        for(tmpItem in tmp)
                            items.add(tmpItem);
                        tmp.clear();
                    }
                    dateText = item.dateData;
                    date = item.date
                }

                tmp.add(Item(item.name,item.icon,item.valueText))
                if(item.value<0) expense+= abs(item.value);
                else income += abs(item.value)
            }

            if(date != "n/a") {
                if(expense==0L)
                    items.add(Date(dateText,"Income: $income"))
                else if(income ==0L)
                    items.add(Date(dateText,"Expense: $expense"))
                else
                    items.add(Date(dateText,"Expense: $expense  Income: $income"))
                for(tmpItem in tmp)
                    items.add(tmpItem);
                tmp.clear();
            }

            //list of data

            recyclerView.adapter = ItemAdapter(
                items,
                onEditClick = { item ->
                    // Handle edit action
                    Toast.makeText(requireContext(), "Editing: $item", Toast.LENGTH_SHORT).show()
                },
                onDeleteClick = { item ->
                    // Handle delete action
                    Toast.makeText(requireContext(), "Deleting: $item", Toast.LENGTH_SHORT).show()
                },
                onDetailsClick = { item ->
                    // Handle details action
                    Toast.makeText(requireContext(), "Details of: $item", Toast.LENGTH_SHORT).show()
                }
            )

        }





        return view
    }




    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RecordsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RecordsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

package com.example.cgpa

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.charts.PieChart

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BudgetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BudgetFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private val viewModel:SharedViewModel by activityViewModels()



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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_budget, container, false)

        val monthlyBudget = view.findViewById<LinearLayout>(R.id.budgetItem)
        val monthlyStat = view.findViewById<LinearLayout>(R.id.monthlyStat)


        //monthly statistics operation
        val income = view.findViewById<TextView>(R.id.income)
        val monthlyExpense = view.findViewById<TextView>(R.id.monthlyExpense)
        val balance = view.findViewById<TextView>(R.id.balance)
        val monthOf = view.findViewById<TextView>(R.id.monthOf)

        viewModel.monthlyData.value?.let{
            data->
            val month = Utility.getMonth()
            val year = Utility.getYear()
            income.text = Utility.formatedValue(data[month to year]?.income?:0L)
            monthlyExpense.text = Utility.formatedValue(data[month to year]?.expense?:0L)
            balance.text = Utility.formatedValue((data[month to year]?.income?:0L)-(data[month to year]?.expense?:0L))
            monthOf.text = Utility.getMonthName()
        }

        monthlyStat.setOnClickListener {
            Utility.bottomSheet(requireActivity(),MonthlyStatFragment(),"MonthlyStatFragment")
        }












        //monthly budget operations
        monthlyBudget.setOnClickListener {
            Utility.bottomSheet(requireActivity(),BudgetModificationFragment(),"BudgetModificationFragment")
        }

        val progressBar = view.findViewById<PieChart>(R.id.progressBar);

        val remaining = view.findViewById<TextView>(R.id.remaining)
        val budget = view.findViewById<TextView>(R.id.budget)
        val expense = view.findViewById<TextView>(R.id.expense)

        viewModel.budgetData.observe(viewLifecycleOwner){
            data->

            val budgetItem = data.firstOrNull()  //first item is monthly budget
            val expenseValue = viewModel.monthlyData.value?.get(Utility.getMonth() to Utility.getYear())?.expense?:0L
            expense.text = Utility.formatedValue(expenseValue)

            if(budgetItem!=null){
                budget.text = Utility.formatedValue(budgetItem.budget)
                val remain = (budgetItem.budget-expenseValue)
                remaining.text = Utility.formatedValue(remain)

                var percent = if (budgetItem.budget!=0L && remain > 0L) (remain*100.0)/budgetItem.budget else 0.0
                percent = Utility.formatDouble(percent)
                val percentage = percent.toFloat()

                if(budgetItem.budget == 0L)
                    Helper.progressBarCircular(progressBar,0F,"--", Color.WHITE)
                else if(remain<0)
                    Helper.progressBarCircular(progressBar,percentage,"Exceed", Color.RED)
                else
                    Helper.progressBarCircular(progressBar,percentage,"Remaining\n${percentage}%", Color.WHITE)
            }
            else {
                budget.text = "0"
                remaining.text = Utility.formatedValue(0L - expenseValue)
                Helper.progressBarCircular(progressBar, 0F, "--", Color.WHITE)
            }
        }

        return view;
    }























    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BudgetFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BudgetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

package com.example.cgpa

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.NumberPicker
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.rpc.Help
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SharedRecordsManager : BottomSheetDialogFragment() {
    private val viewModel by activityViewModels<SharedViewModel>()
    private lateinit var recyclerView: RecyclerView


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return BottomSheetDialog(requireContext(), theme).apply {
//            // We prevent the BottomSheetDialog from altering the system UI colors
//            window?.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            setCanceledOnTouchOutside(false) // Prevent dismiss by outside touch
//        }
        return BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme).apply {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.shared_manager, container, false)

        recyclerView = view.findViewById(R.id.sharedRecordContainer)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        setUpRecyclerView()


        //addBudget operation
        view.findViewById<Button>(R.id.addSharedRecord).setOnClickListener {
            showInputDialog(requireContext(),"Enter Shared Record Title"){
                input->
                if(input.isNotEmpty() && input.isNotBlank()){
                    var ok=true
                    viewModel.sharedData.value?.let{
                        for(item in it){
                            if(item.name==input)ok=false;
                        }
                    }
                    if(ok)
                        viewModel.addItem(SharedItem(input,FirebaseAuth.getInstance().currentUser?.email),requireContext())
                }
            }
        }
        return view
    }




    private fun setUpRecyclerView(){
        viewModel.sharedData.observe(viewLifecycleOwner){data->
            val adapter = SharedRecordAdapter(
                requireContext(),
                data,
                onClick = {
                        item->

                    showOptionDialog(requireContext(),"Choose A Operation!"){
                        confirm->
                        if(confirm){
                            //add button
                            showInputDialog(requireContext(),"Enter An Email To Give Permission!"){
                                input->
                                if(!item.emails.contains(input) && input.isNotBlank() && input.isNotEmpty() &&
                                    input.contains(Regex(".+@gmail\\.com$")) && !input.contains(" ")){

                                    item.emails.add(input)
                                    val list = viewModel.sharedData.value?: mutableListOf()
                                    val last = list.last()
                                    list.remove(last)
                                    viewModel.sharedData.value = list
                                    viewModel.addItem(last,requireContext())
                                }
                                else Utility.showToast(requireContext(),"Please Enter Valid Email Address")
                            }
                        }
                        else{
                            //remove button
                            showInputDialog(requireContext(),"Enter An Email To Revoke Permission!"){
                                    input->
                                if(item.emails.contains(input)){
                                    item.emails.remove(input)
                                    var list = viewModel.sharedData.value?: mutableListOf()
                                    val last = list.last()
                                    list.remove(last)
                                    viewModel.sharedData.value = list
                                    viewModel.addItem(last,requireContext())

                                    val user = Firebase.auth.currentUser
                                    list = viewModel.sharedData.value?: mutableListOf()
                                    if(!item.emails.contains(user?.email) && item.owner!=user?.email)
                                    {
                                        list.remove(item)
                                        viewModel.sharedData.value = list
                                    }
                                }
                                else
                                    Utility.showToast(requireContext(),"Email Not Found!")
                            }
                        }
                    }
                },
                onLongClick = {
                        item->
                    Helper.showConfirmationDialog(requireContext(),"Do You Want To Delete This Item? "){
                        confirm ->
                        if(confirm)
                            viewModel.removeItem(item, requireContext())
                        }
                }
            )
            recyclerView.adapter = adapter
        }

    }

    fun showOptionDialog(context:Context,message:String,confirm:(Boolean)->Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Options")
        builder.setMessage(message)

        builder.setPositiveButton("ADD") { dialog, _ ->
            // Action when YES is clicked
            dialog.dismiss()
            confirm(true)
        }

        builder.setNegativeButton("REMOVE") { dialog, _ ->
            // Action when NO is clicked
            dialog.dismiss()
            confirm(false)
        }

        val dialog = builder.create()
        dialog.setCancelable(true);
        dialog.show()
    }

    fun showInputDialog(context: Context, title: String, onInputConfirmed: (String) -> Unit) {
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT

        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val userInput = input.text.toString()
                onInputConfirmed(userInput)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

















    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        view.findViewById<ImageButton>(R.id.back).setOnClickListener {
//            dismiss()
//        }
    }

    override fun onStart() {
        super.onStart()

        // Set system bar colors to your desired color (it will not be overwritten now)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.lessBlack)
        requireActivity().window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.lessBlack)

        val dialog = dialog as? BottomSheetDialog
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)

            // Ensure full-screen height
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            behavior.peekHeight = 0 // Start at the very bottom
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED // Ensure it starts collapsed (not hidden)

            behavior.isDraggable = false
//            behavior.skipCollapsed = true
//            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })

            // Smooth animation from bottom to full screen
            val animator = ValueAnimator.ofInt(0, resources.displayMetrics.heightPixels)
            animator.duration = 1000 // Slow animation (1 second)
            animator.addUpdateListener { animation ->
                val height = animation.animatedValue as Int
                behavior.peekHeight = height
            }
            animator.start()

            // Expand fully after animation
            it.postDelayed({
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }, 0)
        }
    }

    override fun onStop() {
        super.onStop()

        // Ensure system bar colors remain as you originally set them in MainActivity
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.lessBlack)
        requireActivity().window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.lessBlack)
    }

    override fun getTheme(): Int {
        return com.google.android.material.R.style.Theme_Material3_Light_BottomSheetDialog
    }
}

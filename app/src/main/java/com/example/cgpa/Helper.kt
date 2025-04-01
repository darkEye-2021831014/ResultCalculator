package com.example.cgpa

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.MutableLiveData
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.Charset


class Helper(private val context: Context) {
    companion object
    {
        const val TAG = "MainActivity"
        const val PDF_FILE = "ResultOutput.pdf"
        const val PDF_REPORT_FILE = "report.pdf"
        const val TEXT_FILE = "ResultOutput.txt"
        const val ITEM_INFO_FILE = "ItemInfo.json"
        const val FOLDER = "MyFolder"


        fun writeTextFile(myFolder: File,fileContent:String){
            try {
                val textFile = File(myFolder, TEXT_FILE);

                BufferedWriter(FileWriter(textFile)).use { writer ->
                    writer.write(fileContent)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            Log.i(TAG, "Successfully wrote to the $TEXT_FILE file");
        }

        fun getFileName(context:Context,uri: Uri): String {
            var name = "unknown.pdf"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    cursor.moveToFirst()
                    name = cursor.getString(nameIndex)
                }
            }
            return name
        }

        fun showConfirmationDialog(context:Context,message:String,confirm:(Boolean)->Unit) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Confirmation")
            builder.setMessage(message)

            builder.setPositiveButton("Yes") { dialog, _ ->
                // Action when YES is clicked
                dialog.dismiss()
                confirm(true)
            }

            builder.setNegativeButton("No") { dialog, _ ->
                // Action when NO is clicked
                dialog.dismiss()
                confirm(false)
            }

            val dialog = builder.create()
            dialog.setCancelable(false);
            dialog.show()
        }







        //expense tracker
        fun getIcon(context:Context,icon:Int): Drawable?
        {
            return ContextCompat.getDrawable(context, icon)
        }

        fun saveItemInfoList(itemList: MutableLiveData<MutableList<ItemInfo>>, context: Context): Boolean {
            // Create the directory if it doesn't exist
            val directory = File(context.getExternalFilesDir(null), Helper.FOLDER)
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, Helper.ITEM_INFO_FILE)
            return try {
                // Serialize the item list to JSON
                val gson = Gson()
                val json = gson.toJson(itemList.value)  // Get the value of LiveData

                // Write the JSON data to the file (overwrite by default)
                val outputStream = FileOutputStream(file, false)  // false for overwriting the file
                outputStream.write(json.toByteArray())
                outputStream.flush()
                outputStream.close()

                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }

        fun saveIcon(iconId:Int, context: Context): String? {
            val icon = getIcon(context,iconId) ?: return null;

            // Check if this drawable is already saved
            val drawableId = getDrawableId(iconId)
            val directory = File(context.getExternalFilesDir(null), Helper.FOLDER)

            if (!directory.exists()) {
                directory.mkdirs()
            }

            // Check if the icon file already exists
            val iconFile = File(directory, "$drawableId.png")
            if (iconFile.exists()) {
                return iconFile.absolutePath // Return the existing file path
            }

            // If the file doesn't exist, save the icon as a new image
            val bitmap = icon.toBitmap()
            try {
                val outputStream = FileOutputStream(iconFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                return iconFile.absolutePath // Return the new file path
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }

        // Helper function to return a unique identifier for the drawable
        fun getDrawableId(iconId:Int): String {
            // You can customize this method to return a unique identifier based on your drawable.
            // For example, you can use resource IDs or any other logic based on the drawable.
            return when (iconId) {
                R.drawable.food_record -> "icon_1"
                R.drawable.drinks_records -> "icon_2"
                R.drawable.cloth_records -> "icon_3"
                R.drawable.education_records -> "icon_4"
                R.drawable.healthcare_records -> "icon_5"
                R.drawable.transportation_records -> "icon_6"
                R.drawable.phone_records -> "icon_7"
                R.drawable.other_records -> "icon_8"

                R.drawable.other_records -> "icon_8"
                R.drawable.other_records -> "icon_8"
                R.drawable.other_records -> "icon_8"
                R.drawable.other_records -> "icon_8"
                // Add more cases as necessary
                else -> "icon_${System.currentTimeMillis()}"
            }
        }





        fun loadIcon(imagePath: String?, context: Context): BitmapDrawable? {
            if (imagePath != null) {
                val file = File(imagePath)
                if (file.exists()) {
                    // Decode the image from the file path
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    // Create a Drawable from the Bitmap
                    return BitmapDrawable(context.resources, bitmap)
                }
            }
            return null
        }



        fun retrieveItemInfo(context: Context): List<ItemInfo> {
            val directory = File(context.getExternalFilesDir(null), FOLDER)

            val file = File(directory, ITEM_INFO_FILE)
            val items = mutableListOf<ItemInfo>()

            try {
                if (file.exists()) {
                    // Read the file content
                    val inputStream = FileInputStream(file)
                    val json = inputStream.bufferedReader(Charset.defaultCharset()).use { it.readText() }
                    inputStream.close()

                    // Check if the file content is empty
                    if (json.isNotEmpty()) {
                        // Deserialize JSON into a list of items
                        val gson = Gson()
                        val itemType = object : TypeToken<List<ItemInfo>>() {}.type
                        items.addAll(gson.fromJson(json, itemType))
                    } else {
                        Log.i(TAG, "File is empty, no data to deserialize")
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return items
        }







        fun deleteAllSavedIcons(context: Context): Boolean {
            val directory = File(context.getExternalFilesDir(null), FOLDER)

            // Check if the directory exists
            if (!directory.exists() || !directory.isDirectory) {
                return false
            }

            // List all files in the directory
            val files = directory.listFiles()

            // Filter files that start with "icon"
            val iconFiles = files?.filter { it.name.startsWith("icon") }

            // Delete all matching files
            var allDeleted = true
            iconFiles?.forEach { file ->
                Log.i(TAG, file.name);
                if (!file.delete()) {
                    allDeleted = false // If any file couldn't be deleted, set to false
                }
            }

            return allDeleted
        }




        fun progressBarCircular(pieChart: PieChart, progress: Float, centerMessage:String, textColor:Int) {
            val entries = mutableListOf(
                PieEntry(progress, ""),
                PieEntry(100 - progress, "") // Empty part
            )

            val dataSet = PieDataSet(entries, "").apply {
                colors = listOf(Color.YELLOW, Color.LTGRAY) // Progress and background
                valueTextSize = 16f
                setDrawValues(false) // Hide percentage outside
            }

            pieChart.apply {
                data = PieData(dataSet)
                setUsePercentValues(true)

                animateY(700)
                centerText = centerMessage
                isDrawHoleEnabled = true
                holeRadius = 80f
                setHoleColor(Color.TRANSPARENT)
                setCenterTextColor(textColor)
                isHighlightPerTapEnabled = false
                setTouchEnabled(false)
                setCenterTextSize(8f)
                center

                description.isEnabled = false
                legend.isEnabled = false
                invalidate() // Refresh chart
            }
        }




    }
}




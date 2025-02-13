package com.example.cgpa

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException


class Helper(private val context: Context) {
    companion object
    {
        const val TAG = "MainActivity"
        const val PDF_FILE = "ResultOutput.pdf"
        const val TEXT_FILE = "ResultOutput.txt"
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

    }


}




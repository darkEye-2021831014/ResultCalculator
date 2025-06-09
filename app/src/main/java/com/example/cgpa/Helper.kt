package com.example.cgpa

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
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
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.slf4j.helpers.Util
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.Charset
import java.util.UUID


class Helper(private val context: Context) {
    companion object
    {
        const val TAG = "MainActivity"
        const val PDF_FILE = "ResultOutput.pdf"
        const val PDF_REPORT_FILE = "report.pdf"
        const val TEXT_FILE = "ResultOutput.txt"
        const val ITEM_INFO_FILE = "ItemInfo.json"
        const val BUDGET_ITEM_FILE = "budgetItem.json"
        const val SHARED_ITEM_FILE = "sharedItem.json"
        const val ITEM_INFO_COLLECTION = "ItemInfo"
        const val BUDGET_ITEM_COLLECTION = "budgetItem"
        const val SHARED_ITEM_COLLECTION = "sharedItem"

        const val FOLDER = "MyFolder"
        const val COLLECTION = "User"
        const val SHARED = "Shared"


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


        fun <T:Any> saveList (list: MutableLiveData<MutableList<T>>, context: Context,JSON_FILE_NAME:String): Boolean
        {
            // Create the directory if it doesn't exist
            val directory = File(context.getExternalFilesDir(null), FOLDER)
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, JSON_FILE_NAME)
            return try {
                // Serialize the item list to JSON
                val gson = Gson()
                val json = gson.toJson(list.value)  // Get the value of LiveData

                // Write the JSON data to the file (overwrite by default)
                val outputStream = FileOutputStream(file, false)  // false for overwriting the file
                outputStream.write(json.toByteArray())
                outputStream.flush()
                outputStream.close()
                Utility.log("File Saved In: ${file.absolutePath}")
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }


        fun saveIcon(iconId:Int, context: Context): String? {
            val icon = getIcon(context,iconId) ?: return null;

            // Check if this drawable is already saved
            val drawableId = getDrawableId(context,iconId)
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
                Utility.log("New Saved Icon ${iconFile.absolutePath}")
                return iconFile.absolutePath // Return the new file path
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }

        // Helper function to return the unique name of the drawable
        private fun getDrawableId(context: Context, iconId: Int): String {
            return try {
                context.resources.getResourceEntryName(iconId) // Get the actual drawable name
            } catch (e: Resources.NotFoundException) {
                "icon_unknown" // Fallback name for invalid IDs
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



        inline fun <reified T> loadList(context: Context, JSON_FILE_NAME: String): List<T> {
            val directory = File(context.getExternalFilesDir(null), FOLDER)
            val file = File(directory, JSON_FILE_NAME)

            if (!file.exists()) return emptyList()

            return try {
                val json = file.readText()
                if (json.isNotEmpty()) {
                    val gson = Gson()
                    val itemType = object : TypeToken<List<T>>() {}.type
                    gson.fromJson(json, itemType) ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                emptyList()
            }
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







        suspend fun <T : Any> uploadListToFirestoreSuspend(
            userId: String?,
            itemList: List<T>,
            collectionName: String
        ) = withContext(Dispatchers.IO) {
            try {
                val collection = Firebase.firestore
                    .collection(COLLECTION)
                    .document(userId.toString())
                    .collection(collectionName)

                for (item in itemList) {

                    var customId = UUID.randomUUID().toString()
                    if (item is ItemInfo && item.id.isNullOrEmpty()) {
                        customId = "${item.name}_$customId"
                        item.id = customId
                    } else if (item is BudgetItem && item.id.isNullOrEmpty()) {
                        customId = "${item.heading}_$customId"
                        item.id = customId
                    }
                    else if( item is SharedItem && item.id.isNullOrEmpty()){
                        customId = "${item.name}_$customId"
                        item.id = customId
                    }


                    val id = when(item) {
                        is ItemInfo -> item.id
                        is BudgetItem -> item.id
                        is SharedItem -> item.id
                        else -> continue
                    }

                    collection.document(id.toString()).set(item).await()
                }

                // Optional: return success if needed
            } catch (e: Exception) {
                // You can log it or rethrow it based on your needs
                Utility.log("Upload Failed: ${e.message}")
            }
        }

        suspend fun deleteFromDatabase(
            collectionName:String,
            itemId:String
            )= withContext(Dispatchers.IO){
                try{
                    val email = FirebaseAuth.getInstance().currentUser?.email
                    val collection = Firebase.firestore
                        .collection(COLLECTION)
                        .document(email.toString())
                        .collection(collectionName)
                    collection.document(itemId).delete().await()
                }
                catch (e:Exception){
                    Utility.log("Remove Failed: ${e.message}")
                }
        }





        suspend fun <T : Any> downloadListFromFirestoreSuspend(
            userId: String?,
            collectionName: String,
            clazz: Class<T>
        ): List<T> = withContext(Dispatchers.IO) {
            try{
                val collection = Firebase.firestore
                    .collection(COLLECTION)
                    .document(userId.toString())
                    .collection(collectionName)
                val docs = collection.get().await()
                docs.documents.mapNotNull { it.toObject(clazz) }
            }
            catch (e:Exception){
                Utility.log("Download Failed: ${e.message}")
                emptyList()
            }
        }






        //prioritize loading account data first over local data
        suspend fun loadSavedData(context: Context, viewModel: SharedViewModel) {
            val user = FirebaseAuth.getInstance().currentUser

            val userData = retrieveItemInfo(context)
            val budgetData = loadList<BudgetItem>(context, BUDGET_ITEM_FILE)
            val sharedData = loadList<SharedItem>(context, SHARED_ITEM_FILE)

            if (user != null) {
                val remoteUserData = downloadListFromFirestoreSuspend(user.email, ITEM_INFO_COLLECTION, ItemInfo::class.java)
                val remoteBudgetData = downloadListFromFirestoreSuspend(user.email, BUDGET_ITEM_COLLECTION, BudgetItem::class.java)
                val remoteSharedData = downloadSharedList()

                // Update ViewModel with downloaded data
                viewModel.addList(viewModel.userData,remoteUserData.toMutableList())
                viewModel.addList(viewModel.budgetData, remoteBudgetData.toMutableList())
                viewModel.addList(viewModel.sharedData, remoteSharedData.toMutableList())

                // Save downloaded data to local storage
                saveList(viewModel.userData, context, ITEM_INFO_FILE)
                saveList(viewModel.budgetData, context, BUDGET_ITEM_FILE)
                saveList(viewModel.sharedData, context, SHARED_ITEM_FILE)

                Utility.log("UserData, BudgetData and SharedData Loaded from Firestore and Saved Locally")
            } else {
                viewModel.addList(viewModel.userData,userData.toMutableList())
                viewModel.addList(viewModel.budgetData, budgetData.toMutableList())
                viewModel.addList(viewModel.sharedData, sharedData.toMutableList())

                Utility.log("Loaded Local userData: $userData")
                Utility.log("Loaded Local budgetData: $budgetData")
                Utility.log("Loaded Local sharedData: $sharedData")
            }
        }












        suspend fun uploadSharedList(
            itemList: List<SharedItem>,
        ) = withContext(Dispatchers.IO) {
            try {
                val collection = Firebase.firestore
                    .collection(SHARED)

                for (item in itemList) {

                    if(item.id.isNullOrEmpty()) {
                        var customId = UUID.randomUUID().toString()
                        customId = "${item.owner}_$customId"
                        item.id = customId
                    }

                    val id = item.id
                    collection.document(id.toString()).set(item).await()
                }

                // Optional: return success if needed
            } catch (e: Exception) {
                // You can log it or rethrow it based on your needs
                Utility.log("Upload Failed: ${e.message}")
            }
        }

        suspend fun deleteSharedItem(
            itemId:String
        )= withContext(Dispatchers.IO){
            try{
                val collection = Firebase.firestore
                    .collection(SHARED)
                collection.document(itemId).delete().await()
            }
            catch (e:Exception){
                Utility.log("Remove Failed: ${e.message}")
            }
        }





        suspend fun downloadSharedList(): List<SharedItem> {
            val user = Firebase.auth.currentUser ?: return emptyList()
            val email = user.email ?: return emptyList()

            return try {
                // Single query using OR logic (Firestore doesn't support OR directly)
                val snapshot = Firebase.firestore.collection("Shared")
                    .where(
                        Filter.or(
                            Filter.equalTo("owner", email),
                            Filter.arrayContains("emails", email)
                        )
                    )
                    .get()
                    .await()

                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(SharedItem::class.java)?.copy(id = doc.id)
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching shared items", e)
                emptyList()
            }
        }



    }
}




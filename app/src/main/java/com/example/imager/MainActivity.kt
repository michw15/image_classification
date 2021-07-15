package com.example.imager

import DatabaseHandler
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.imager.ml.MobilenetV110224Quant
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class MainActivity : AppCompatActivity() {

    lateinit var bitmap :Bitmap
    lateinit var imgview:ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imgview = findViewById(R.id.imageView2)
        val fileName = "label.txt"
        val inputString = application.assets.open(fileName).bufferedReader().use { it.readText() }
        var townList = inputString.split("\n")

        var tv:TextView = findViewById(R.id.textView);

        var select:Button = findViewById(R.id.button)

        select.setOnClickListener(View.OnClickListener {

            var intent:Intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"

            startActivityForResult(intent, 100)
        })

        var predict:Button = findViewById(R.id.button2)
        predict.setOnClickListener {

            var resized: Bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

            val model = MobilenetV110224Quant.newInstance(this)

            // Creates inputs for reference.
            val inputFeature0 =
                TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.UINT8)

            var tbuffer = TensorImage.fromBitmap(resized)
            var byteBuffer = tbuffer.buffer

            inputFeature0.loadBuffer(byteBuffer)

            // Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer

            var max = getMAx(outputFeature0.floatArray)

            tv.setText(townList[max])

            // Releases model resources if no longer used.
            model.close()

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        imgview.setImageURI(data?.data)

        var uri: Uri?= data?.data

        bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
    }

    fun getMAx(arr:FloatArray) : Int{

        var ind = 0
        var min = 0.0f

        for (i in 0..1000)
        {
            if (arr[i]>min)
            {
                ind = i
                min = arr[i]
            }
        }
        return ind
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        var btnAdd:Button = findViewById(R.id.btnAdd)
//        setSupportActionBar(toolbar)
//
//        btnAdd.setOnClickListener { view ->
//
//            addRecord()
//        }
//
//        setupListofDataIntoRecyclerView()
//    }
//
//    /**
//     * Function is used to show the list of inserted data.
//     */
//    private fun setupListofDataIntoRecyclerView() {
//
//        if (getItemsList().size > 0) {
//
//            rvItemsList.visibility = View.VISIBLE
//            tvNoRecordsAvailable.visibility = View.GONE
//
//            // Set the LayoutManager that this RecyclerView will use.
//            rvItemsList.layoutManager = LinearLayoutManager(this)
//            // Adapter class is initialized and list is passed in the param.
//            val itemAdapter = ItemAdapter(this, getItemsList())
//            // adapter instance is set to the recyclerview to inflate the items.
//            rvItemsList.adapter = itemAdapter
//        } else {
//
//            rvItemsList.visibility = View.GONE
//            tvNoRecordsAvailable.visibility = View.VISIBLE
//        }
//    }
//
//    /**
//     * Function is used to get the Items List from the database table.
//     */
//    private fun getItemsList(): ArrayList<UserModelClass> {
//        //creating the instance of DatabaseHandler class
//        val databaseHandler: DatabaseHandler = DatabaseHandler(this)
//        //calling the viewEmployee method of DatabaseHandler class to read the records
//        val empList: ArrayList<UserModelClass> = databaseHandler.viewEmployee()
//
//        return empList
//    }
//
//    //Method for saving the employee records in database
//    private fun addRecord() {
//        val name = etName.text.toString()
//        val email = etEmailId.text.toString()
//        val databaseHandler: DatabaseHandler = DatabaseHandler(this)
//        if (!name.isEmpty() && !email.isEmpty()) {
//            val status =
//                databaseHandler.addUser(UserModelClass(0, name, email))
//            if (status > -1) {
//                Toast.makeText(applicationContext, "Record saved", Toast.LENGTH_LONG).show()
//                etName.text.clear()
//                etEmailId.text.clear()
//
//                setupListofDataIntoRecyclerView()
//            }
//        } else {
//            Toast.makeText(
//                applicationContext,
//                "Name or Email cannot be blank",
//                Toast.LENGTH_LONG
//            ).show()
//        }
//    }
//
//    /**
//     * Method is used to show the Custom Dialog.
//     */
//    fun updateRecordDialog(empModelClass: UserModelClass) {
//        val updateDialog = Dialog(this, R.style.Theme_Dialog)
//        updateDialog.setCancelable(false)
//        /*Set the screen content from a layout resource.
//         The resource will be inflated, adding all top-level views to the screen.*/
//        updateDialog.setContentView(R.layout.dialog_update)
//
//        updateDialog.etUpdateName.setText(empModelClass.name)
//        updateDialog.etUpdateEmailId.setText(empModelClass.email)
//
//        updateDialog.tvUpdate.setOnClickListener(View.OnClickListener {
//
//            val name = updateDialog.etUpdateName.text.toString()
//            val email = updateDialog.etUpdateEmailId.text.toString()
//
//            val databaseHandler: DatabaseHandler = DatabaseHandler(this)
//
//            if (!name.isEmpty() && !email.isEmpty()) {
//                val status =
//                    databaseHandler.updateEmployee(UserModelClass(empModelClass.id, name, email))
//                if (status > -1) {
//                    Toast.makeText(applicationContext, "Record Updated.", Toast.LENGTH_LONG).show()
//
//                    setupListofDataIntoRecyclerView()
//
//                    updateDialog.dismiss() // Dialog will be dismissed
//                }
//            } else {
//                Toast.makeText(
//                    applicationContext,
//                    "Name or Email cannot be blank",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        })
//        updateDialog.tvCancel.setOnClickListener(View.OnClickListener {
//            updateDialog.dismiss()
//        })
//        //Start the dialog and display it on screen.
//        updateDialog.show()
//    }
//
//    /**
//     * Method is used to show the Alert Dialog.
//     */
//    fun deleteRecordAlertDialog(empModelClass: UserModelClass) {
//        val builder = AlertDialog.Builder(this)
//        //set title for alert dialog
//        builder.setTitle("Delete Record")
//        //set message for alert dialog
//        builder.setMessage("Are you sure you wants to delete ${empModelClass.name}.")
//        builder.setIcon(android.R.drawable.ic_dialog_alert)
//
//        //performing positive action
//        builder.setPositiveButton("Yes") { dialogInterface, which ->
//
//            //creating the instance of DatabaseHandler class
//            val databaseHandler: DatabaseHandler = DatabaseHandler(this)
//            //calling the deleteEmployee method of DatabaseHandler class to delete record
//            val status = databaseHandler.deleteEmployee(UserModelClass(empModelClass.id, "", ""))
//            if (status > -1) {
//                Toast.makeText(
//                    applicationContext,
//                    "Record deleted successfully.",
//                    Toast.LENGTH_LONG
//                ).show()
//
//                setupListofDataIntoRecyclerView()
//            }
//
//            dialogInterface.dismiss() // Dialog will be dismissed
//        }
//        //performing negative action
//        builder.setNegativeButton("No") { dialogInterface, which ->
//            dialogInterface.dismiss() // Dialog will be dismissed
//        }
//        // Create the AlertDialog
//        val alertDialog: AlertDialog = builder.create()
//        // Set other dialog properties
//        alertDialog.setCancelable(false) // Will not allow user to cancel after clicking on remaining screen area.
//        alertDialog.show()  // show the dialog to UI
//    }
}
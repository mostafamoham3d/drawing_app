package com.example.drawingapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Gallery
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private var mImageButtonCurrentPaint:ImageButton?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawing_view.setBrushSize(20F)
        mImageButtonCurrentPaint=ll_paint_colors[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallet_sellected))
        ib_brush.setOnClickListener {
            showBrushSizeChooserDialog()
        }
        ib_redo.setOnClickListener {
            drawing_view.redo()
        }
        ib_undo.setOnClickListener {
            drawing_view.undo()
        }
        ib_gallery_btn.setOnClickListener {
            if(isReadStorageAllowed())
            {
                val pickphoto=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickphoto, Gallery)
            }else
            {
                requestpermession()
            }
        }
        ib_save_btn.setOnClickListener {
            if(isReadStorageAllowed())
            {
                BitmapAsyncTask(getBitmapFromView(fl_drawing_view)).execute()
            }else
            {
                requestpermession()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK){
            if(requestCode== Gallery){
                try {
                    if(data!!.data!=null)
                    {
                        iv_background.visibility=View.VISIBLE
                        iv_background.setImageURI(data.data)
                    }else
                    {
                        Toast.makeText(this, "Error in parsing the image or its corrupted ", Toast.LENGTH_SHORT).show()
                    }
                }catch (e:Exception)
                {
                    e.printStackTrace()
                }
            }
        }
    }
    private fun showBrushSizeChooserDialog()
    {
        val brushDialog=Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size: ")
        val smallbtn=brushDialog.ib_small_brush
        smallbtn.setOnClickListener {
            drawing_view.setBrushSize(10F)
            brushDialog.dismiss()
        }
        val medbtn=brushDialog.ib_medium_brush
        medbtn.setOnClickListener {
            drawing_view.setBrushSize(20F)
            brushDialog.dismiss()
        }
        val largebtn=brushDialog.ib_large_brush
        largebtn.setOnClickListener { drawing_view.setBrushSize(30F)
        brushDialog.dismiss()}
        brushDialog.show()
    }

    fun paintClicked(view: View)
    {
        if(view!==mImageButtonCurrentPaint)
        {

            val imgbtn=view as ImageButton
            val colorTag=imgbtn.tag.toString()
            drawing_view.setColor(colorTag)
            imgbtn.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallet_sellected))
            mImageButtonCurrentPaint!!.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallet_normal))
            mImageButtonCurrentPaint=view
            eraser.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_eraser_30x30))
            eraser.setBackgroundResource(R.drawable.no_border)
        }
    }
    fun erase(view: View)
    {
        eraser.background=ContextCompat.getDrawable(this,R.drawable.border)
        eraser.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_eraser_30x30))
        drawing_view.setColor(eraser.tag.toString())
       // drawing_view.erase()
        mImageButtonCurrentPaint!!.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.pallet_normal))
    }

    private fun requestpermession()
    {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE).toString())){
            Toast.makeText(this, "Need permission to add a background", Toast.LENGTH_SHORT).show()
        }
    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE), storage_code)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== storage_code){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }else
            {
                Toast.makeText(this, "You Denied the permission", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun isReadStorageAllowed():Boolean
    {
        val result=ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
        return result==PackageManager.PERMISSION_GRANTED
    }

    private fun getBitmapFromView(view: View):Bitmap
    {
        val returnedBitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas=Canvas(returnedBitmap)
        val bgDrawable=view.background
        if(bgDrawable!=null)
        {
            bgDrawable.draw(canvas)
        }else
        {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }


    private inner class BitmapAsyncTask(val bitmap:Bitmap):AsyncTask<Any,Void,String>()
    {
        override fun doInBackground(vararg params: Any?): String {
            var  reult=""
            if(bitmap !=null)
            {
                try {

                    val bytes=ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)
                    val f=File(externalCacheDir!!.absoluteFile.toString()+File.separator+"DrawingApp_"+System.currentTimeMillis()/1000+".png")
                    val fos=FileOutputStream(f)
                    fos.write(bytes.toByteArray())
                    fos.close()
                    reult =f.absolutePath
                }catch (e:Exception)
                {
                    reult=""
                    e.printStackTrace()
                }
            }
            return reult
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(!result!!.isEmpty())
            {
           Toast.makeText(this@MainActivity,"File saved",Toast.LENGTH_SHORT).show()
            }
            else
            {
                Toast.makeText(this@MainActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result),null){
                path,uri->
                val shareIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = "image/png"
                }
                startActivity(Intent.createChooser(shareIntent, "share"))
            }

        }

    }

    companion object{
        private const val storage_code=1
        private const val Gallery=2
    }

}
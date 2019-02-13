package com.letsbuildthatapp.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                accessImage()
            } else {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
        } else {
            accessImage()
        }
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode){
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    accessImage()
                }
        }
    }



    private fun accessImage () {
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null
        )
        val imageUriArray = arrayListOf<Uri>()
        var elementsNum = 0
        var pointer = 0
        var showTimer: Timer? = null
        var handler = Handler()
        var playFlag = false


        if (cursor.moveToFirst()) {
            imageView.setImageURI(retreiveUri(cursor))
            do {
                imageUriArray.add(retreiveUri(cursor))
                elementsNum++
            } while (cursor.moveToNext())
        }
        cursor.close()


        previous_button.setOnClickListener {
            if (--pointer >= 0){
                imageView.setImageURI(imageUriArray[pointer])

            } else {
                pointer = elementsNum
                imageView.setImageURI(imageUriArray[pointer])
            }
        }


        next_button.setOnClickListener {
            if (++pointer < elementsNum){
                imageView.setImageURI(imageUriArray[pointer])

            } else {
                pointer = 0
                imageView.setImageURI(imageUriArray[pointer])
            }
        }


        play_pause_button.setOnClickListener {

            if (!playFlag) {
                showTimer = Timer()
                playFlag = true

                showTimer!!.schedule(object: TimerTask () {
                    override fun run() {
                        if (++pointer < elementsNum){
                            handler.post {
                                imageView.setImageURI(imageUriArray[pointer])
                            }
                        } else {
                            pointer = 0
                            handler.post {
                                imageView.setImageURI(imageUriArray[pointer])
                            }
                        }
                    }
                },2000, 2000)
            } else {
                playFlag = false
                showTimer!!.cancel()
                showTimer = null

            }
        }
    }



    private fun retreiveUri (cursor: Cursor?): Uri {
        Log.d("kotlintest", "uriShowImage")
        val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursor!!.getLong(fieldIndex)
        val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

        return imageUri

    }
}

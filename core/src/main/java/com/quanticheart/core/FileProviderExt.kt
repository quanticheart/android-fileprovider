package com.quanticheart.core

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider.getUriForFile
import java.io.File
import java.util.*

//
// Created by Jonn Alves on 26/12/22.
//
fun Activity.createInternalFileTest(): Uri {
    val file = File(filesDirDocuments, "${Date().time}-test.txt")
    file.createTxtFile("test file with file provider")

    val uri = file.getUriWithAuthorities(this)
    Log.e("TEST", file.getUriWithAuthorities(this).toString())
    Toast.makeText(this, "Saved your text", Toast.LENGTH_LONG).show()
    return uri
}

val Context.filesDirImages: File
    get() {
        val imagePath = File(filesDir, "images")
        if (!imagePath.exists()) {
            imagePath.mkdirs()
        }
        return imagePath
    }

val Context.filesDirMovies: File
    get() {
        val imagePath = File(filesDir, "movies")
        if (!imagePath.exists()) {
            imagePath.mkdirs()
        }
        return imagePath
    }

val Context.filesDirDocuments: File
    get() {
        val imagePath = File(filesDir, "documents")
        if (!imagePath.exists()) {
            imagePath.mkdirs()
        }
        return imagePath
    }

val Context.filesDirDocumentsUri: Uri
    get() = filesDirDocuments.getUriWithAuthorities(this)

fun File.getUriWithAuthorities(context: Context): Uri =
    getUriForFile(context, context.getString(R.string.file_provider_authorities), this)

fun File.createDirIfNotExists() {
    if (!exists()) {
        mkdirs()
    }
}

fun Uri.delete(): Boolean {
    return this.path?.let {
        val fDelete = File(it)
        if (fDelete.exists()) {
            if (fDelete.delete()) {
                println("file Deleted :$it")
                true
            } else {
                println("file not Deleted :$it")
                false
            }
        } else false
    } ?: run {
        false
    }
}
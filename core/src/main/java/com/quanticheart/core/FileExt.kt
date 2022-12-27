package com.quanticheart.core

import android.app.RecoverableSecurityException
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.File
import java.io.FileWriter
import java.net.URI

//
// Created by Jonn Alves on 26/12/22.
//
fun File.createTxtFile(text: String) {
    try {
        val writer = FileWriter(this)
        writer.append(text)
        writer.flush()
        writer.close()
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}

fun Uri.deleteRecursive(context: Context) {
    val fileOrDirectory = File(this.getRealPathFromURI(context))
    if (fileOrDirectory.isDirectory)
        fileOrDirectory.listFiles()?.let {
            for (child in it)
                if (child.isDirectory)
                    child.getUriWithAuthorities(context).deleteRecursive(context)
                else
                    this.deleteFile(context)

        }
    fileOrDirectory.delete()
}

private fun Uri.getRealPathFromURI(context: Context): String? {
    val result: String?
    val cursor = context.contentResolver.query(this, null, null, null, null)
    if (cursor == null) { // Source is Dropbox or other similar local file path
        result = path
    } else {
        cursor.moveToFirst()
        val idx: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        result = cursor.getString(idx)
        cursor.close()
    }
    return result
}

fun Uri.deleteFile(context: Context): IntentSender? {
    return try {
        // android 28 and below
        context.contentResolver.delete(this, null, null)
        null
    } catch (e: SecurityException) {
        // android 29 (Android 10)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                MediaStore.createDeleteRequest(context.contentResolver, listOf(this)).intentSender
            }
            // android 30 (Android 11)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                val recoverableSecurityException = e as? RecoverableSecurityException
                recoverableSecurityException?.userAction?.actionIntent?.intentSender
            }
            else -> null
        }
    }
}
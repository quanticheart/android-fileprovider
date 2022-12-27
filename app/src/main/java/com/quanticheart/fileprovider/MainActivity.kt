package com.quanticheart.fileprovider

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.IntentSenderRequest
import androidx.core.view.WindowCompat
import com.quanticheart.core.*
import com.quanticheart.fileprovider.databinding.ActivityMainBinding


class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var uri: Uri? = null
        binding.btnIntrFile.setOnClickListener {
            uri = createInternalFileTest()
        }
        binding.btnDelIntrFile.setOnClickListener {
            uri?.deleteFile(this)
        }
        binding.btnDelFolderIntrFile.setOnClickListener {
            filesDirDocumentsUri.deleteRecursive(this)
        }
    }
}
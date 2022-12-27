@file:Suppress("ProtectedInFinal")

package com.quanticheart.core

import androidx.appcompat.app.AppCompatActivity

//
// Created by Jonn Alves on 26/12/22.
//
open class BaseActivity : AppCompatActivity() {
    protected val activityLauncher = BetterActivityResult.registerActivityForResult(this)
}
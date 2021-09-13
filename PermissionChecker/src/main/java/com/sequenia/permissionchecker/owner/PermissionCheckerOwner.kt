package com.sequenia.permissionchecker.owner

import android.content.Context
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract

interface PermissionCheckerOwner {

    fun getContext(): Context

    fun <I, O> registerForActivityResult(
        contract: ActivityResultContract<I, O>,
        callback: ActivityResultCallback<O>
    ): ActivityResultLauncher<I>

    fun shouldShowRequestPermissionRationale(permission: String): Boolean
}
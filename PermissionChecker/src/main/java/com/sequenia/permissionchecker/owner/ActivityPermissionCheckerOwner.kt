package com.sequenia.permissionchecker.owner

import android.content.Context
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity

class ActivityPermissionCheckerOwner(
    private val activity: AppCompatActivity
) : PermissionCheckerOwner {

    override fun getContext(): Context {
        return activity
    }

    override fun <I, O> registerForActivityResult(
        contract: ActivityResultContract<I, O>,
        callback: ActivityResultCallback<O>
    ): ActivityResultLauncher<I> {
        return activity.registerForActivityResult(contract, callback)
    }

    override fun shouldShowRequestPermissionRationale(permission: String): Boolean {
        return activity.shouldShowRequestPermissionRationale(permission)
    }
}
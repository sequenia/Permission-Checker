package com.sequenia.permissionchecker.owner

import android.content.Context
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment

class FragmentPermissionCheckerOwner(
    private val fragment: Fragment
) : PermissionCheckerOwner {

    override fun getContext(): Context {
        return fragment.requireContext()
    }

    override fun <I, O> registerForActivityResult(
        contract: ActivityResultContract<I, O>,
        callback: ActivityResultCallback<O>
    ): ActivityResultLauncher<I> {
        return fragment.registerForActivityResult(contract, callback)
    }

    override fun shouldShowRequestPermissionRationale(permission: String): Boolean {
        return fragment.shouldShowRequestPermissionRationale(permission)
    }
}
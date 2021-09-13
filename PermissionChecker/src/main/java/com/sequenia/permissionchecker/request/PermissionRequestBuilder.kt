package com.sequenia.permissionchecker.request

import com.sequenia.permissionchecker.SimplePermissionCallback

/**
 * Builder for [permission request][PermissionRequest]
 */
class PermissionRequestBuilder(private val permission: String) {
    var onGranted: SimplePermissionCallback? = null
    var onDenied: SimplePermissionCallback? = null

    fun build(): PermissionRequest {
        return PermissionRequest(
            permission,
            onGranted,
            onDenied
        )
    }
}
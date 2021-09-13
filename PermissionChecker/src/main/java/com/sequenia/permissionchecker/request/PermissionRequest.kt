package com.sequenia.permissionchecker.request

import com.sequenia.permissionchecker.SimplePermissionCallback

data class PermissionRequest(
    val permission: String,
    val onGranted: SimplePermissionCallback? = null,
    val onDenied: SimplePermissionCallback? = null
)
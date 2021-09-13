package com.sequenia.permissionchecker

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.sequenia.permissionchecker.request.PermissionRequest
import com.sequenia.permissionchecker.request.PermissionRequestBuilder

fun AppCompatActivity.registerPermissionChecker(): PermissionChecker {
    return PermissionChecker(this)
}

fun Fragment.registerPermissionChecker(): PermissionChecker {
    return PermissionChecker(this)
}

fun PermissionChecker.check(
    permissions: Array<out String>? = null,
    check: PermissionsCheckerDsl.() -> Unit
) {
    this.clear()
    PermissionsCheckerDsl(this, permissions).apply(check)
    this.check()
}

@DslMarker
annotation class PermissionCheckerDslMarker

@PermissionCheckerDslMarker
class PermissionsCheckerDsl(
    private val checker: PermissionChecker,
    permissions: PermissionsList? = null
) {
    init {
        val permissionRequests =
            permissions?.map { PermissionRequest(permission = it) } ?: emptyList()
        checker.addPermissionRequests(permissionRequests)
    }

    /**
     * Add permission request
     */
    fun permission(permission: String, apply: PermissionRequestBuilderDsl.() -> Unit) {
        val builder = PermissionRequestBuilder(permission)
        PermissionRequestBuilderDsl(builder).apply(apply)
        val request = builder.build()
        checker.addPermissionRequests(listOf(request))
    }

    /**
     * Add onAllGranted callback for requests
     */
    fun onAllGranted(onGranted: SimplePermissionCallback) {
        checker.setOnAllGrantedCallback(onGranted)
    }

    /**
     * Add onAnyGranted callback for requests
     */
    fun onAnyDenied(onDenied: PermissionsListCallback) {
        checker.setOnAnyDeniedCallback(onDenied)
    }

    /**
     * Add onDeniedPermanent callback for requests
     */
    fun onDeniedPermanent(onDenied: PermissionsListCallback) {
        checker.setOnDeniedPermanentCallback(onDenied)
    }

    /**
     * Add onShowRationale callback for requests
     */
    fun onShowRationale(applyDsl: PermissionRationaleDsl.(PermissionsList) -> Unit) {
        val callback: PermissionsListCallback = { it: PermissionsList ->
            PermissionRationaleDsl(checker).apply {
                applyDsl(it)
            }
        }
        checker.setOnShowRationaleCallback(callback)
    }
}

@PermissionCheckerDslMarker
class PermissionRationaleDsl(private val checker: PermissionChecker) {

    /**
     * Accept rationale and continue permissions request
     */
    fun acceptRationale() {
        checker.check()
    }
}

@PermissionCheckerDslMarker
class PermissionRequestBuilderDsl constructor(private val builder: PermissionRequestBuilder) {

    /**
     * Add OnGranted callback to permission request
     */
    fun onGranted(onGranted: SimplePermissionCallback) {
        builder.onGranted = onGranted
    }

    /**
     * Add OnDenied callback to permission request
     */
    fun onDenied(onDenied: SimplePermissionCallback) {
        builder.onDenied = onDenied
    }
}
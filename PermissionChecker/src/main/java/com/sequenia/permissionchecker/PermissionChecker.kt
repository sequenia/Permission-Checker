package com.sequenia.permissionchecker


import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.fragment.app.Fragment
import com.sequenia.permissionchecker.owner.ActivityPermissionCheckerOwner
import com.sequenia.permissionchecker.owner.FragmentPermissionCheckerOwner
import com.sequenia.permissionchecker.owner.PermissionCheckerOwner
import com.sequenia.permissionchecker.request.PermissionRequest

internal typealias PermissionsList = Array<out String>
internal typealias PermissionsListCallback = (permissions: PermissionsList) -> Unit
internal typealias SimplePermissionCallback = () -> Unit

/**
 * Class for checking permissions
 */
class PermissionChecker(private val permissionCheckerOwner: PermissionCheckerOwner) {

    private val permissionsRequestLauncher = register()

    private val permissionToPermissionRequest = mutableMapOf<String, PermissionRequest>()
    private val permissionToPermissionState = mutableMapOf<String, PermissionRequestState>()

    private var callbacks = PermissionCheckerCallbacks()

    constructor(fragment: Fragment) : this(FragmentPermissionCheckerOwner(fragment))

    constructor(activity: AppCompatActivity) : this(ActivityPermissionCheckerOwner(activity))

    /**
     * Set callback for any of permissions denied. Called if any of permissions denied
     */
    fun setOnAnyDeniedCallback(callback: PermissionsListCallback) {
        callbacks.onAnyDeniedCallback = callback
    }

    /**
     * Set callback for all permissions granted. Called if all of permissions granted
     */
    fun setOnAllGrantedCallback(callback: SimplePermissionCallback) {
        callbacks.onAllGrantedCallback = callback
    }

    /**
     * Set callback for show rationale. Called if any of permissions need to snow rationale.
     * After user accepts rationale, [check] should be called again, without calling [clear]
     */
    fun setOnShowRationaleCallback(callback: PermissionsListCallback) {
        callbacks.onShowRationaleCallback = callback
    }

    /**
     * Set callback for denied forever. Called if any of permissions denied permanent
     */
    fun setOnDeniedPermanentCallback(callback: PermissionsListCallback) {
        callbacks.onDeniedPermanentCallback = callback
    }

    /**
     * Add permission requests, only one [PermissionRequest] per permission allowed.
     * If [PermissionRequest] already exists for permission, it will be replaced
     */
    fun addPermissionRequests(permissionRequests: List<PermissionRequest>) {
        permissionRequests.forEach {
            permissionToPermissionRequest[it.permission] = it
            permissionToPermissionState[it.permission] = PermissionRequestState.PENDING
        }
    }

    /**
     * Clear state of permission check requests and callbacks
     */
    fun clear() {
        permissionToPermissionRequest.clear()
        permissionToPermissionState.clear()
        callbacks = PermissionCheckerCallbacks()
    }

    /**
     * Execute permission check requests. Doesn't implicitly clears state, so [clear] must be
     * executed before creating new requests
     */
    fun check() {
        setPermissionStates()

        if (shouldShowRationale()) {
            val permissions = getPermissionRequestsByState(PermissionRequestState.RATIONALE)
            callbacks.onShowRationaleCallback!!.invoke(permissions.toPermissionsArray())
            return
        }

        val pendingPermissions = getPendingPermissionRequests().toPermissionsArray()
        permissionsRequestLauncher.launch(pendingPermissions)
    }

    private fun shouldShowRationale(): Boolean {
        val hasRationaleState = permissionToPermissionState.any {
            it.value == PermissionRequestState.RATIONALE
        }
        return hasRationaleState && callbacks.onShowRationaleCallback != null
    }

    private fun setPermissionStates() {
        permissionToPermissionRequest.forEach {
            val permission = it.key

            if (checkSelfPermission(permission) == PERMISSION_GRANTED) {
                setStateForGrantedPermission(permission)
                return@forEach
            }
            setStateForDeniedPermission(permission)
        }
    }

    private fun setStateForGrantedPermission(permission: String) {
        permissionToPermissionState[permission] = PermissionRequestState.GRANTED
    }

    private fun setStateForDeniedPermission(permission: String) {
        // If permission was with rationale state already, which means that rationale was already
        // shown, set pending state to proceed request for permission
        if (permissionToPermissionState[permission] == PermissionRequestState.RATIONALE) {
            permissionToPermissionState[permission] = PermissionRequestState.PENDING
            return
        }

        val shouldShowRationale = permissionCheckerOwner
            .shouldShowRequestPermissionRationale(permission)
        if (shouldShowRationale && callbacks.onShowRationaleCallback != null) {
            permissionToPermissionState[permission] = PermissionRequestState.RATIONALE
            return
        }
        permissionToPermissionState[permission] = PermissionRequestState.PENDING
    }

    private fun register(): ActivityResultLauncher<PermissionsList> {
        return permissionCheckerOwner.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            getActivityResultCallback()
        )
    }

    private fun getActivityResultCallback(): ActivityResultCallback<Map<String, Boolean>> {
        return ActivityResultCallback<Map<String, Boolean>> { permissionsResult ->
            setPermissionsStateFromResult(permissionsResult)
            resolvePermissionCallbacks()
        }
    }

    private fun setPermissionsStateFromResult(permissionsResult: Map<String, Boolean>) {
        permissionsResult.forEach { permissionResult ->
            val permission = permissionResult.key
            val isGranted = permissionResult.value

            val shouldShowRequestPermissionRationale =
                permissionCheckerOwner.shouldShowRequestPermissionRationale(permission)
            permissionToPermissionState[permission] = when {
                isGranted -> PermissionRequestState.GRANTED
                !isGranted && !shouldShowRequestPermissionRationale -> {
                    PermissionRequestState.DENIED_PERMANENT
                }
                !isGranted -> PermissionRequestState.DENIED
                else -> throw Exception("Unresolved permission state")
            }
        }
    }

    private fun resolvePermissionCallbacks() {
        permissionToPermissionRequest.forEach {
            val permission = it.key
            val request = it.value

            when (permissionToPermissionState[permission]) {
                PermissionRequestState.GRANTED -> {
                    request.onGranted?.invoke()
                }

                PermissionRequestState.DENIED_PERMANENT,
                PermissionRequestState.DENIED -> {
                    request.onDenied?.invoke()
                }
            }
        }

        val deniedForeverPermissions = getDeniedPermanentPermissionRequests().toPermissionsArray()
        if (deniedForeverPermissions.isNotEmpty()) {
            callbacks.onDeniedPermanentCallback?.invoke(deniedForeverPermissions)
        }

        val deniedPermissions = getDeniedPermissionRequests().toPermissionsArray()
        if (deniedPermissions.isNotEmpty()) {
            callbacks.onAnyDeniedCallback?.invoke(deniedPermissions)
        } else {
            callbacks.onAllGrantedCallback?.invoke()
        }

        clear()
    }

    private fun checkSelfPermission(permission: String): Int {
        return ContextCompat.checkSelfPermission(permissionCheckerOwner.getContext(), permission)
    }

    private fun getDeniedPermissionRequests(): Map<String, PermissionRequest> {
        return getPermissionRequestsByState(
            PermissionRequestState.DENIED,
            PermissionRequestState.DENIED_PERMANENT
        )
    }

    private fun getDeniedPermanentPermissionRequests(): Map<String, PermissionRequest> {
        return getPermissionRequestsByState(PermissionRequestState.DENIED_PERMANENT)
    }

    private fun getPendingPermissionRequests(): Map<String, PermissionRequest> {
        return getPermissionRequestsByState(PermissionRequestState.PENDING)
    }

    private fun getPermissionRequestsByState(
        vararg states: PermissionRequestState
    ): Map<String, PermissionRequest> {
        return permissionToPermissionRequest.filter {
            val permissionState = permissionToPermissionState[it.key]
            states.contains(permissionState)
        }
    }

    private fun Map<String, PermissionRequest>.toPermissionsArray(): Array<String> {
        return map { it.key }.toTypedArray()
    }
}

private data class PermissionCheckerCallbacks(
    var onAllGrantedCallback: SimplePermissionCallback? = null,
    var onAnyDeniedCallback: PermissionsListCallback? = null,
    var onShowRationaleCallback: PermissionsListCallback? = null,
    var onDeniedPermanentCallback: PermissionsListCallback? = null,
)

private enum class PermissionRequestState {
    PENDING,
    GRANTED,
    DENIED,
    RATIONALE,
    DENIED_PERMANENT
}
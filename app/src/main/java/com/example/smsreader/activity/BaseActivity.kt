package com.example.smsreader.activity

import android.content.pm.PackageManager
import android.support.annotation.NonNull
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.util.SimpleArrayMap
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

abstract class BaseActivity : AppCompatActivity() {

    companion object {
        private const val DEFAULT_NUMBER_OF_PERMISSION = 1
    }

    private val permissionCallbackMap = SimpleArrayMap<Int, PermissionCallback>(DEFAULT_NUMBER_OF_PERMISSION)

    /**
     * Request permission and get result on call back.
     *
     * @param permissions
     * @param callback
     */
    internal fun requestPermission(permissions: Array<String>, callback: PermissionCallback) {
        val requestCode = permissionCallbackMap.size() + 1
        permissionCallbackMap.put(requestCode, callback)
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String>, @NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val callback = permissionCallbackMap.get(requestCode) ?: return

        // if permission request cancelled
        if (grantResults.size < 0 && permissions.isNotEmpty()) {
            callback.onPermissionDenied(permissions)
            return
        }

        val grantedPermissions = arrayListOf<String>()
        val blockedPermissions = arrayListOf<String>()
        val deniedPermissions = arrayListOf<String>()

        for ((index, permission) in permissions.withIndex()) {
            val permissionList = if (grantResults[index] == PackageManager.PERMISSION_GRANTED) grantedPermissions
            else if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) blockedPermissions
            else deniedPermissions

            permissionList.add(permission)
        }

        if (grantedPermissions.size == permissions.size)
            callback.onPermissionGranted(grantedPermissions.toTypedArray())

        if (deniedPermissions.size > 0)
            callback.onPermissionDenied(deniedPermissions.toTypedArray())

        if (blockedPermissions.size > 0)
            callback.onPermissionBlocked(blockedPermissions.toTypedArray())

        permissionCallbackMap.remove(requestCode)
    }

    internal fun showToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    internal fun showSnackMessage(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }

}

internal interface PermissionCallback {

    fun onPermissionGranted(grantedPermissions: Array<String>)

    fun onPermissionDenied(deniedPermissions: Array<String>)

    fun onPermissionBlocked(blockedPermissions: Array<String>)

}
package com.sequenia.example

import android.Manifest
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sequenia.permissionchecker.check
import com.sequenia.permissionchecker.registerPermissionChecker

class MainActivity : AppCompatActivity() {

    val permissionChecker = registerPermissionChecker()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.ask_multiple_button).setOnClickListener {
            askMultiplePermission()
        }

        findViewById<Button>(R.id.ask_one_button).setOnClickListener {
            askOnePermission()
        }
    }

    private fun askMultiplePermission() {
        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        permissionChecker.check(permissions) {
            onAllGranted {
                Toast.makeText(baseContext, "All granted", Toast.LENGTH_SHORT).show()
            }

            onAnyDenied {
                Toast.makeText(baseContext, "Any denied", Toast.LENGTH_SHORT).show()
            }

            onShowRationale {
                showRationale(it) { acceptRationale() }
            }

            onDeniedPermanent {
                Toast.makeText(baseContext, "Denied permanent", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun askOnePermission() {
        permissionChecker.check {
            permission(Manifest.permission.SEND_SMS) {
                onGranted {
                    Toast.makeText(baseContext, "All granted", Toast.LENGTH_SHORT).show()
                }

                onDenied {
                    Toast.makeText(baseContext, "Any denied", Toast.LENGTH_SHORT).show()
                }
            }

            onDeniedPermanent {
                Toast.makeText(baseContext, "Denied permanent", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showRationale(permissions: Array<out String>, onAccept: () -> Unit) {
        val message = permissions.joinToString()
        MaterialAlertDialogBuilder(this)
            .setMessage(message)
            .setPositiveButton("Accept") { _, _ -> onAccept() }
            .show()
    }
}
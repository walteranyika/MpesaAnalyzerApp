package com.walter.mbogo.screens
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun SmsPermissionRequester(
    onPermissionGranted: () -> Unit, // Callback when permission is granted
    onPermissionDenied: () -> Unit // Callback when permission is denied or rationale needed
) {
    val context = LocalContext.current
    var permissionAlreadyRequested by remember { mutableStateOf(false) }

    // Prepare the permission launcher
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(context, "READ_SMS Permission Granted", Toast.LENGTH_SHORT).show()
                onPermissionGranted()
            } else {
                Toast.makeText(context, "READ_SMS Permission Denied", Toast.LENGTH_SHORT).show()
                onPermissionDenied() // You might want to show a more specific message or guide the user
            }
        }
    )

    fun requestSmsPermission() {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted
                Toast.makeText(context, "Permission already granted", Toast.LENGTH_SHORT).show()
                onPermissionGranted()
            }
            shouldShowRequestPermissionRationale(context as android.app.Activity, Manifest.permission.READ_SMS) -> {
                // Explain to the user why you need the permission, then request again
                // For this example, we'll directly call onPermissionDenied to indicate rationale is needed
                // In a real app, you'd show a dialog here.
                Toast.makeText(context, "Please grant SMS permission to read MPESA messages.", Toast.LENGTH_LONG).show()
                onPermissionDenied() // Or trigger a dialog explaining why you need it
                // and then provide a button to try requesting again.
                // For simplicity, we directly call denied here. If the user wants to try again,
                // they'd have to re-trigger the action that leads to this Composable.
            }
            else -> {
                // Directly request the permission
                permissionAlreadyRequested = true
                requestPermissionLauncher.launch(Manifest.permission.READ_SMS)
            }
        }
    }

    // UI to trigger the permission request
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("We need SMS permission to read MPESA transaction messages and help you track your finances.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Button(onClick = { requestSmsPermission() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(5.dp)) {
            Text("Grant SMS Permission")
        }
    }
}

// Helper function (if not using an Activity directly, you need to pass it)
// This is often handled within an Activity context, but if you're in a pure Composable
// without easy access to the current Activity instance, you might need to find it.
// However, the 'context as android.app.Activity' cast in requestSmsPermission
// implies that the Composable is hosted within an Activity.
private fun shouldShowRequestPermissionRationale(activity: android.app.Activity, permission: String): Boolean {
    return activity.shouldShowRequestPermissionRationale(permission)
}
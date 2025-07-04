package com.walter.mbogo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.walter.mbogo.nav.Destinations
import com.walter.mbogo.nav.bottomNavItems
import com.walter.mbogo.screens.ChartsScreen
import com.walter.mbogo.screens.ExpensesScreen
import com.walter.mbogo.screens.IncomeScreen
import com.walter.mbogo.screens.SmsPermissionRequester
import com.walter.mbogo.ui.theme.MbogoTheme
import com.walter.mbogo.workers.MpesaSmsWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MbogoTheme {
                NavigationBarMotherScreen()
            }
        }
    }
}


@Composable
fun NavigationBarMotherScreen() { // Renamed from your original example for clarity
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { AppBottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.Income.route, // Your starting screen
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destinations.Income.route) {
                IncomeScreen()
            }
            composable(Destinations.Expenses.route) {
                ExpensesScreen()
            }
            composable(Destinations.Graphs.route) {
                ChartsScreen()
            }
        }

        var hasSmsPermission by remember { mutableStateOf(false) } // Manage state if needed higher up
        val context = LocalContext.current
        var initialSmsProcessingStarted by remember { mutableStateOf(false) }
         //Check initial permission status (optional, if you want to update UI immediately)
         LaunchedEffect(Unit) {
             hasSmsPermission = ContextCompat.checkSelfPermission(
                 context,
                 Manifest.permission.READ_SMS
             ) == PackageManager.PERMISSION_GRANTED
         }
        if (!hasSmsPermission) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_SMS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is already granted, proceed with your logic
                LaunchedEffect(Unit) { // Ensure this runs once when condition is met
                    // startMpesaProcessing()
                }
                // You might want to show the main content of your screen here
                Text("SMS Permission Granted. Processing MPESA messages...")
            } else {
                // Permission not yet granted, show the requester UI
                SmsPermissionRequester(
                    onPermissionGranted = {
                        hasSmsPermission = true // Update state if needed
                        //startMpesaProcessing()
                        // Navigate to the next screen or update UI
                    },
                    onPermissionDenied = {
                        // Handle the case where permission is denied
                        // You might show an error, disable features, or guide the user to settings
                        Toast.makeText(
                            context,
                            "SMS Permission is required for MPESA tracking.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            }
        }else{
            if (!initialSmsProcessingStarted) {
                // If permission is granted and we haven't started the worker yet,
                // start it now.
                LaunchedEffect(Unit) { // Use LaunchedEffect to run this once when conditions are met
                    Toast.makeText(context, "SMS Permission granted. Starting MPESA SMS processing...", Toast.LENGTH_SHORT).show()
                    enqueueMpesaSmsWorker(context)
                    initialSmsProcessingStarted = true // Mark as started to avoid re-enqueueing on recomposition
                }
                Text("MPESA SMS Processing has been initiated. Your data will appear as it's processed.")
            }
        }
    }
}






@Composable
fun AppBottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        bottomNavItems.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                },
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                alwaysShowLabel = true // Or false, depending on your design preference
            )
        }
    }
}


/**
 * Enqueues the MpesaSmsWorker as a one-time work request.
 */
fun enqueueMpesaSmsWorker(context: android.content.Context) {
    // Create a OneTimeWorkRequest for your MpesaSmsWorker
    val mpesaSmsWorkRequest = OneTimeWorkRequestBuilder<MpesaSmsWorker>()
        // You can add constraints here if needed, e.g.:
        // .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        // You can also pass input data to your worker if necessary:
        // .setInputData(workDataOf("KEY_START_TIMESTAMP" to System.currentTimeMillis()))
        .build()

    // Enqueue the work request
    WorkManager.getInstance(context).enqueue(mpesaSmsWorkRequest)

    Log.d("WorkManager", "MpesaSmsWorker enqueued.")
    // You can also observe the WorkInfo of this request if you want to update the UI
    // based on the worker's status (e.g., show a progress bar or completion message).
    // WorkManager.getInstance(context).getWorkInfoByIdLiveData(mpesaSmsWorkRequest.id)
    //    .observe(LocalLifecycleOwner.current) { workInfo ->
    //        if (workInfo != null) {
    //            when (workInfo.state) {
    //                WorkInfo.State.SUCCEEDED -> Log.d("WorkManager", "MpesaSmsWorker Succeeded")
    //                WorkInfo.State.FAILED -> Log.d("WorkManager", "MpesaSmsWorker Failed")
    //                WorkInfo.State.RUNNING -> Log.d("WorkManager", "MpesaSmsWorker Running")
    //                else -> Log.d("WorkManager", "MpesaSmsWorker Status: ${workInfo.state}")
    //            }
    //        }
    //    }
}
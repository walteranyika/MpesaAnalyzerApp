package com.walter.mbogo

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.fragment.app.FragmentActivity
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
import com.walter.mbogo.security.BiometricHelper
import com.walter.mbogo.ui.theme.MbogoTheme
import com.walter.mbogo.workers.MpesaSmsWorker

class MainActivity : FragmentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
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


@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun NavigationBarMotherScreen() { // Renamed from your original example for clarity
    val context = LocalContext.current
    val activity = context as FragmentActivity
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isAuthenticated by remember { mutableStateOf(false) }


    val biometricHelper = remember {
        BiometricHelper(
            activity = activity,
            onAuthSuccess = {
                isAuthenticated = true
                errorMessage = null
            },
            onAuthError = {
                errorMessage = it
            }
        )
    }


    LaunchedEffect(Unit) {
        if (isBiometricAvailable(context)) {
            biometricHelper.authenticate()
        }
    }

    val navController = rememberNavController()
    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            bottomBar = { AppBottomNavigationBar(navController = navController) }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Destinations.Income.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Destinations.Income.route) { IncomeScreen() }
                composable(Destinations.Expenses.route) { ExpensesScreen() }
                composable(Destinations.Graphs.route) { ChartsScreen() }
            }

            // Your SMS permission handling here as before...
        }

        // 🔒 Overlay if not authenticated
        if (!isAuthenticated) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .blur(20.dp) // Requires Android 12+
                    .zIndex(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Please authenticate to continue", color = Color.White)
                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = it, color = Color.Red)
                    }
                }
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

fun isBiometricAvailable(context: Context): Boolean {
    val biometricManager = BiometricManager.from(context)
    return biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
}

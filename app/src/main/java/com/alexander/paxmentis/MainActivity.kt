package com.alexander.paxmentis

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.*
import com.alexander.paxmentis.screens.*
import com.alexander.paxmentis.security.BiometricPromptManager
import com.alexander.paxmentis.ui.theme.PaxMentisTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var hasCallPermission by mutableStateOf(false)
    private var isUnlocked by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCallPermission = isGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.loadLibrary("sqlcipher")
        checkPermissions()
        
        setContent {
            PaxMentisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isUnlocked) {
                        AppNavigation(hasCallPermission)
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer))
                        
                        LaunchedEffect(Unit) {
                            delay(1500)
                            if (!isUnlocked) {
                                isUnlocked = true
                            }
                        }
                    }
                }
            }
        }

        authenticate()
    }

    private fun authenticate() {
        val biometricManager = BiometricPromptManager(this)
        biometricManager.showBiometricPrompt(
            title = "Pax Mentis Entsperren",
            description = "Bitte authentifiziere Dich.",
            onSuccess = { isUnlocked = true },
            onError = { /* Fallback */ }
        )
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            hasCallPermission = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(hasCallPermission: Boolean) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                Text("Pax Mentis", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                
                NavigationItem(
                    icon = Icons.Default.Home,
                    label = "Dashboard",
                    isSelected = currentRoute == "home",
                    onClick = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationItem(
                    icon = Icons.Default.Explore,
                    label = "Krisen-Kompass",
                    isSelected = currentRoute == "crisis_compass",
                    onClick = {
                        navController.navigate("crisis_compass") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                NavigationItem(
                    icon = Icons.AutoMirrored.Filled.Chat,
                    label = "Neutral-Messaging",
                    isSelected = currentRoute == "neutral_messaging",
                    onClick = {
                        navController.navigate("neutral_messaging") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationItem(
                    icon = Icons.Default.Security,
                    label = "Shared Reality",
                    isSelected = currentRoute == "shared_reality",
                    onClick = {
                        navController.navigate("shared_reality") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationItem(
                    icon = Icons.Default.Air,
                    label = "Atem-Übung",
                    isSelected = currentRoute == "breathing",
                    onClick = {
                        navController.navigate("breathing") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationItem(
                    icon = Icons.AutoMirrored.Filled.EventNote,
                    label = "Sorgenstuhl",
                    isSelected = currentRoute == "worry_chair",
                    onClick = {
                        navController.navigate("worry_chair") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationItem(
                    icon = Icons.Default.NightsStay,
                    label = "Schlafhilfe",
                    isSelected = currentRoute == "sleep_aid",
                    onClick = {
                        navController.navigate("sleep_aid") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationItem(
                    icon = Icons.Default.Settings,
                    label = "Einstellungen",
                    isSelected = currentRoute == "settings",
                    onClick = {
                        navController.navigate("settings") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Pax Mentis") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menü")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") { HomeScreen(navController) }
                composable("crisis_compass") { CrisisCompassScreen(navController) }
                composable("neutral_messaging") { NeutralMessagingScreen() }
                composable("shared_reality") { SharedRealityScreen() }
                composable("panic") { PanicScreen(hasCallPermission) }
                composable("grounding_54321") { GroundingScreen() }
                composable("depression_help") { DepressionHelpScreen() }
                composable("safe_space") { SafeSpaceScreen() }
                composable("breathing") { BreathingScreen() }
                composable("worry_chair") { WorryChairScreen() }
                composable("sleep_aid") { SleepAidScreen() }
                composable("settings") { SettingsScreen() }
                composable("ambivalence") { AmbivalenceScreen() }
                composable("trigger_block") { TriggerBlockScreen(onNavigateBack = { navController.popBackStack() }) }
                composable("night_watch") { NightWatchScreen() }
            }
        }
    }
}

@Composable
fun NavigationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label) },
        selected = isSelected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}

package com.alexander.paxmentis

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alexander.paxmentis.data.UserPreferences
import com.alexander.paxmentis.screens.AmbivalenceScreen
import com.alexander.paxmentis.screens.BreathingScreen
import com.alexander.paxmentis.screens.CrisisCompassScreen
import com.alexander.paxmentis.screens.DepressionHelpScreen
import com.alexander.paxmentis.screens.EducationScreen
import com.alexander.paxmentis.screens.GroundingScreen
import com.alexander.paxmentis.screens.HomeScreen
import com.alexander.paxmentis.screens.NeutralMessagingScreen
import com.alexander.paxmentis.screens.NightWatchScreen
import com.alexander.paxmentis.screens.PanicScreen
import com.alexander.paxmentis.screens.SafeSpaceScreen
import com.alexander.paxmentis.screens.SettingsScreen
import com.alexander.paxmentis.screens.SharedRealityScreen
import com.alexander.paxmentis.screens.SleepAidScreen
import com.alexander.paxmentis.screens.TriggerBlockScreen
import com.alexander.paxmentis.screens.WorryChairScreen
import com.alexander.paxmentis.security.BiometricPromptManager
import com.alexander.paxmentis.ui.theme.PaxMentisTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Screen("home", "Dashboard", Icons.Default.Home)
    data object CrisisCompass : Screen("crisis_compass", "Krisen-Kompass", Icons.Default.Explore)
    data object NeutralMessaging : Screen("neutral_messaging", "Neutral-Messaging", Icons.AutoMirrored.Filled.Chat)
    data object SharedReality : Screen("shared_reality", "Shared Reality", Icons.Default.Security)
    data object Breathing : Screen("breathing", "Atem-Übung", Icons.Default.Air)
    data object WorryChair : Screen("worry_chair", "Sorgenstuhl", Icons.AutoMirrored.Filled.EventNote)
    data object SleepAid : Screen("sleep_aid", "Schlafhilfe", Icons.Default.NightsStay)
    data object Education : Screen("education", "Wissens-Hub", Icons.AutoMirrored.Filled.MenuBook)
    data object Settings : Screen("settings", "Einstellungen", Icons.Default.Settings)

    data object Panic : Screen("panic", "Panik", Icons.Default.Warning)
    data object Grounding54321 : Screen("grounding_54321", "Grounding", Icons.Default.SelfImprovement)
    data object DepressionHelp : Screen("depression_help", "Depression", Icons.Default.Favorite)
    data object SafeSpace : Screen("safe_space", "Safe Space", Icons.Default.Shield)
    data object Ambivalence : Screen("ambivalence", "Ambivalenz", Icons.AutoMirrored.Filled.Help)
    data object TriggerBlock : Screen("trigger_block", "Trigger Block", Icons.Default.Block)
    data object NightWatch : Screen("night_watch", "Night Watch", Icons.Default.Nightlight)
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var userPreferences: UserPreferences

    private var hasCallPermission by mutableStateOf(false)
    private var isUnlocked by mutableStateOf(false)

    private var pendingNavigateRoute: String? = null
    
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
                        AppNavigation(hasCallPermission, userPreferences)
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Pax Mentis ist geschützt",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
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
            onError = { /* Log or show error */ }
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
fun AppNavigation(hasCallPermission: Boolean, userPreferences: UserPreferences) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val drawerItems = remember {
        listOf(
            Screen.Home,
            Screen.CrisisCompass,
            null,
            Screen.NeutralMessaging,
            Screen.SharedReality,
            Screen.Breathing,
            Screen.WorryChair,
            Screen.SleepAid,
            Screen.Education,
            null,
            Screen.Settings
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Pax Mentis",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                drawerItems.forEach { screen ->
                    if (screen == null) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    } else {
                        NavigationItem(
                            icon = screen.icon,
                            label = screen.label,
                            isSelected = currentRoute == screen.route,
                            onClick = {
                                navigateTo(navController, screen.route, drawerState, scope)
                            }
                        )
                    }
                }
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
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) { HomeScreen(navController) }
                composable(Screen.CrisisCompass.route) { CrisisCompassScreen(navController) }
                composable(Screen.NeutralMessaging.route) { NeutralMessagingScreen() }
                composable(Screen.SharedReality.route) { SharedRealityScreen() }
                composable(Screen.Panic.route) { PanicScreen(hasCallPermission, userPreferences) }
                composable(Screen.Grounding54321.route) { GroundingScreen(userPreferences) }
                composable(Screen.DepressionHelp.route) { DepressionHelpScreen() }
                composable(Screen.SafeSpace.route) { SafeSpaceScreen(userPreferences) }
                composable(Screen.Breathing.route) { BreathingScreen(userPreferences) }
                composable(Screen.WorryChair.route) { WorryChairScreen(userPreferences) }
                composable(Screen.SleepAid.route) { SleepAidScreen(userPreferences) }
                composable(Screen.Settings.route) { SettingsScreen() }
                composable(Screen.Education.route) { EducationScreen() }
                composable(Screen.Ambivalence.route) { AmbivalenceScreen() }
                composable(Screen.TriggerBlock.route) { TriggerBlockScreen(onNavigateBack = { navController.popBackStack() }) }
                composable(Screen.NightWatch.route) { NightWatchScreen(userPreferences) }
            }
        }
    }
}

private fun navigateTo(
    navController: NavHostController,
    route: String,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
    scope.launch { drawerState.close() }
}

@Composable
fun NavigationItem(
    icon: ImageVector,
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

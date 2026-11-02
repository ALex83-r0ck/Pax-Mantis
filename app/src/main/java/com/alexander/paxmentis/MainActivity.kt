package com.alexander.paxmentis

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.alexander.paxmentis.screens.*
import com.alexander.paxmentis.security.BiometricPromptManager
import com.alexander.paxmentis.security.LockedContent
import com.alexander.paxmentis.security.SessionLock
import com.alexander.paxmentis.data.UserPreferences
import com.alexander.paxmentis.ui.theme.PaxMentisTheme
import com.alexander.paxmentis.ui.theme.ThemeMood
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var sessionLock: SessionLock

    @Inject
    lateinit var userPreferences: UserPreferences

    private lateinit var biometricPromptManager: BiometricPromptManager
    private var authMessage by mutableStateOf<String?>(null)
    private var pendingRoute by mutableStateOf<String?>(null)

    // Der PIN-/Muster-Dialog kann die Activity kurz stoppen; dann nicht mitten im Entsperren sperren.
    private var authInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        biometricPromptManager = BiometricPromptManager(this) { result ->
            authInProgress = false
            when (result) {
                BiometricPromptManager.AuthResult.Success -> {
                    authMessage = null
                    sessionLock.unlock()
                }
                is BiometricPromptManager.AuthResult.Error -> authMessage = result.message
                is BiometricPromptManager.AuthResult.Unavailable -> authMessage = result.message
            }
        }

        // Nach einer Drehung / Wiederherstellung nicht erneut zum Widget-Ziel springen.
        if (savedInstanceState == null) {
            pendingRoute = routeFromIntent(intent)
        }

        setContent {
            val mood by userPreferences.themeMoodFlow.collectAsState()
            PaxMentisTheme(mood = ThemeMood.fromStored(mood)) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isUnlocked by sessionLock.isUnlocked.collectAsState()
                    AppNavigation(
                        isUnlocked = isUnlocked,
                        authMessage = authMessage,
                        onRequestUnlock = ::requestUnlock,
                        pendingRoute = pendingRoute,
                        onPendingRouteHandled = { pendingRoute = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        routeFromIntent(intent)?.let { pendingRoute = it }
    }

    override fun onStop() {
        super.onStop()
        // App im Hintergrund -> geschützte Bereiche wieder sperren (nicht bei Drehung).
        if (!isChangingConfigurations && !authInProgress) {
            sessionLock.lock()
        }
    }

    private fun requestUnlock() {
        if (authInProgress) return
        authMessage = null
        authInProgress = true
        biometricPromptManager.authenticate(
            title = "Pax Mentis entsperren",
            description = "Bitte bestätige, dass du es bist."
        )
    }

    private fun routeFromIntent(intent: Intent?): String? {
        if (intent == null) return null
        if ((intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) return null
        // Die Activity ist exportiert: nur bekannte, ungeschützte Ziele zulassen.
        return intent.getStringExtra(EXTRA_NAVIGATE_TO)?.takeIf { it in EXTERNAL_ROUTES }
    }

    companion object {
        const val EXTRA_NAVIGATE_TO = "navigate_to"
        const val ROUTE_PANIC = "panic"
        private val EXTERNAL_ROUTES = setOf(ROUTE_PANIC)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    isUnlocked: Boolean,
    authMessage: String?,
    onRequestUnlock: () -> Unit,
    pendingRoute: String?,
    onPendingRouteHandled: () -> Unit
) {
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
                NavigationItem(
                    icon = Icons.Default.Favorite,
                    label = "Mein Krisenplan",
                    isSelected = currentRoute == "crisis_plan",
                    onClick = {
                        navController.navigate("crisis_plan") {
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
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                composable(MainActivity.ROUTE_PANIC) {
                    PanicScreen(onOpenCrisisPlan = { navController.navigate("crisis_plan") })
                }
                // Bewusst ohne Sperre – siehe CrisisPlanScreen.
                composable("crisis_plan") { CrisisPlanScreen() }
                composable("grounding_54321") { GroundingScreen() }
                composable("follow_dot") { FollowDotScreen() }
                composable("find_shapes") { FindShapesScreen() }
                composable("depression_help") { DepressionHelpScreen() }
                composable("safe_space") { SafeSpaceScreen() }
                composable("breathing") { BreathingScreen() }
                composable("worry_chair") {
                    LockedContent(isUnlocked, authMessage, onRequestUnlock) { WorryChairScreen() }
                }
                composable("sleep_aid") { SleepAidScreen() }
                composable("settings") {
                    LockedContent(isUnlocked, authMessage, onRequestUnlock) { SettingsScreen() }
                }
                composable("ambivalence") { AmbivalenceScreen() }
                composable("trigger_block") {
                    TriggerBlockScreen(
                        onStartBreathing = {
                            navController.navigate("breathing") {
                                popUpTo("trigger_block") { inclusive = true }
                            }
                        },
                        onDismiss = { navController.popBackStack() }
                    )
                }
                composable("night_watch") { NightWatchScreen() }
            }

            LaunchedEffect(pendingRoute) {
                val route = pendingRoute ?: return@LaunchedEffect
                navController.navigate(route) { launchSingleTop = true }
                onPendingRouteHandled()
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

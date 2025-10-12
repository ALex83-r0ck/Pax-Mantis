package com.alexander.paxmentis

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alexander.paxmentis.data.PaxMentisDatabase
import com.alexander.paxmentis.data.WorryEntry
import com.alexander.paxmentis.screens.BreathingScreen
import com.alexander.paxmentis.screens.PanicScreen
import com.alexander.paxmentis.screens.SleepAidScreen
import com.alexander.paxmentis.screens.WorryChairScreen
import com.alexander.paxmentis.screens.SettingsScreen
import com.alexander.paxmentis.ui.theme.PaxMentisTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var hasCallPermission by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCallPermission = isGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Prüfe und fordere Berechtigung bei Start
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED -> {
                hasCallPermission = true
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CALL_PHONE) -> {
                // Erkläre dem Benutzer, warum die Berechtigung benötigt wird (optional)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
            }
        }
        setContent {
            PaxMentisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(hasCallPermission)
                }
            }
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
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentRoute == "home",
                    onClick = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Atem-Übung") },
                    label = { Text("Atem-Übung") },
                    selected = currentRoute == "breathing",
                    onClick = {
                        navController.navigate("breathing") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "Sorgenstuhl") },
                    label = { Text("Sorgenstuhl") },
                    selected = currentRoute == "worry_chair",
                    onClick = {
                        navController.navigate("worry_chair") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = "Schlafhilfe") },
                    label = { Text("Schlafhilfe") },
                    selected = currentRoute == "sleep_aid",
                    onClick = {
                        navController.navigate("sleep_aid") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Warning, contentDescription = "Notfall") },
                    label = { Text("Notfall") },
                    selected = currentRoute == "panic",
                    onClick = {
                        navController.navigate("panic") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Einstellungen") },
                    label = { Text("Einstellungen") },
                    selected = currentRoute == "settings",
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
                composable("breathing") { BreathingScreen() }
                composable("worry_chair") { WorryChairScreen() }
                composable("sleep_aid") { SleepAidScreen() }
                composable("panic") { PanicScreen(hasCallPermission) }
                composable("settings") { SettingsScreen() }
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val db = PaxMentisDatabase.getInstance(context)
    val worryEntries by db.worryEntryDao().getAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                    )
                )
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Dashboard", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Letzte Panikattacke:")
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(worryEntries) { entry ->
                Text(
                    text = "${android.text.format.DateFormat.format("dd.MM.yyyy HH:mm", entry.date)} - ${entry.worry}",
                    fontSize = 16.sp
                )
            }
        }
        Button(onClick = {
            scope.launch {
                val newEntry = WorryEntry(
                    date = System.currentTimeMillis(),
                    worry = "Neuer Vorfall",
                    reason = null,
                    worstCase = null,
                    solution = null
                )
                db.worryEntryDao().insert(newEntry)
            }
        }) {
            Text("Vorfall melden", fontSize = 18.sp)
        }
        Text("Wochenübersicht: ${worryEntries.size} Vorfälle", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { navController.navigate("breathing") }) { Text("Atem-Übung") }
        Button(onClick = { navController.navigate("worry_chair") }) { Text("Sorgenstuhl") }
        Button(onClick = { navController.navigate("sleep_aid") }) { Text("Schlafhilfe") }
        Button(onClick = { navController.navigate("panic") }) { Text("Notfall") }
        Button(onClick = { navController.navigate("setting") } ) { Text("Setting") }
    }
}
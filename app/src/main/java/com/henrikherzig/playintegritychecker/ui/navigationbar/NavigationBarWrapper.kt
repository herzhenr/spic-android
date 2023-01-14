package com.henrikherzig.playintegritychecker.ui.navigationbar

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.henrikherzig.playintegritychecker.R
import com.henrikherzig.playintegritychecker.attestation.PlayIntegrityStatement
import com.henrikherzig.playintegritychecker.attestation.safetynet.SafetyNetStatement
import com.henrikherzig.playintegritychecker.dataStore
import com.henrikherzig.playintegritychecker.ui.about.AboutPage
import com.henrikherzig.playintegritychecker.ui.playintegrity.PlayIntegrity
import com.henrikherzig.playintegritychecker.ui.safetynet.SafetyNet
import com.henrikherzig.playintegritychecker.ui.ResponseType
import com.henrikherzig.playintegritychecker.ui.settings.Settings
import com.henrikherzig.playintegritychecker.ui.CustomViewModel

@Composable
fun BottomNavigationBar(
    safetyNetResult: State<ResponseType<SafetyNetStatement>>,
    onSafetyNetRequest: (String, String, String?) -> Unit,
    playIntegrityResult: State<ResponseType<PlayIntegrityStatement>>,
    onPlayIntegrityRequest: (String, String, String?) -> Unit,
    playServiceVersion: String?
) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(color = MaterialTheme.colors.primarySurface)
    systemUiController.setNavigationBarColor(color = MaterialTheme.colors.primarySurface)
    val navController = rememberNavController()
    val appPages = listOf(
        BottomNavItem.PlayIntegrity,
        BottomNavItem.SafetyNet,
        BottomNavItem.Settings,
        BottomNavItem.About,
    )

    // TODO: very ugly better solution in the future
    // check
    var selectedIndexCheckPlayIntegrity by remember { mutableStateOf("local") }
    var selectedIndexCheckSafetyNet by remember { mutableStateOf("local") }

    val local: String = stringResource(id = R.string.requestSettings_local)
    val server: String = stringResource(id = R.string.requestSettings_server)
    val google: String = stringResource(id = R.string.requestSettings_google)
    val itemsCheck: List<List<String>> = listOf(listOf("local", local), listOf("server", server))
    val changedCheckPlayIntegrity: (idx: String) -> Unit = {
        selectedIndexCheckPlayIntegrity = it
    }
    val changedCheckSafetyNet: (idx: String) -> Unit = {
        selectedIndexCheckSafetyNet = it
    }
    var selectedIndexNoncePlayIntegrity by remember { mutableStateOf("local") }
    var selectedIndexNonceSafetyNet by remember { mutableStateOf("local") }

    val itemsNonce: List<List<String>> =
        listOf(listOf("local", local), listOf("server", server), listOf("google", google))
    val changedNoncePlayIntegrity: (idx: String) -> Unit = {
        selectedIndexNoncePlayIntegrity = it
    }
    val changedNonceSafetyNet: (idx: String) -> Unit = {
        selectedIndexNonceSafetyNet = it
    }

    // Better state handling: Use ViewModels
    val context = LocalContext.current

    val viewModel = remember {
        CustomViewModel(context.dataStore)
    }
    // url
    LaunchedEffect(viewModel) {
        viewModel.requestURL()
    }
    val urlValue = viewModel.stateURL.observeAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                /*
                * app bar title of the app
                * first line: short app name variant in bigger font
                * second line: full app name variant in smaller font
                */
                title = {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = stringResource(id = R.string.app_name_short),
                            style = MaterialTheme.typography.subtitle2,
                            fontSize = 18.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        bottomBar = {
            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                appPages.forEach { screen ->
                    BottomNavigationItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(stringResource(screen.title)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.screen_route } == true,
                        onClick = {
                            navController.navigate(screen.screen_route) {
                                // Pop up to the first page to avoid large stack
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple versions of same page
                                launchSingleTop = true
                                // Restore state when selecting a previous page again
                                restoreState = true
                            }
                        }
                    )
                }
            }
        },

        ) { innerPadding ->
        NavHost(
            navController,
            startDestination = BottomNavItem.PlayIntegrity.screen_route,
            Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.PlayIntegrity.screen_route) {
                PlayIntegrity(
                    playIntegrityResult,
                    {
                        onPlayIntegrityRequest(
                            selectedIndexCheckPlayIntegrity,
                            selectedIndexNoncePlayIntegrity,
                            urlValue
                        )
                    },
                    selectedIndexCheckPlayIntegrity,
                    itemsCheck,
                    changedCheckPlayIntegrity,
                    selectedIndexNoncePlayIntegrity,
                    itemsNonce,
                    changedNoncePlayIntegrity
                )
            }
            composable(BottomNavItem.SafetyNet.screen_route) {
                SafetyNet(
                    safetyNetResult,
                    {
                        onSafetyNetRequest(
                            selectedIndexCheckSafetyNet,
                            selectedIndexNonceSafetyNet,
                            urlValue
                        )
                    },
                    selectedIndexCheckSafetyNet,
                    itemsCheck,
                    changedCheckSafetyNet,
                    selectedIndexNonceSafetyNet,
                    itemsNonce.subList(0, 2),
                    changedNonceSafetyNet
                )
            }
            composable(BottomNavItem.Settings.screen_route) {
                Settings(playServiceVersion)
            }
            composable(BottomNavItem.About.screen_route) {
                AboutPage(playServiceVersion)
            }
        }
    }
}
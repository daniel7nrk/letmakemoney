package com.example.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.ui.platform.LocalContext
import com.example.notification.NotificationHelper
import com.example.ui.screens.*
import com.example.ui.theme.RoyalBlue

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    object Welcome : Screen("welcome", "Welcome")
    object CountrySelection : Screen("country_selection", "Country")
    object SmsExplainer : Screen("sms_explainer", "SMS Privacy")
    object Login : Screen("login", "Login")
    object ProfilePhoto : Screen("profile_photo", "Profile Photo")
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Transactions : Screen("transactions", "Transactions", Icons.Default.ReceiptLong)
    object Reports : Screen("reports", "Reports", Icons.Default.BarChart)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object PendingReview : Screen("pending_review", "Pending Review")
    object TransactionDetail : Screen("transaction_detail/{id}", "Detail") {
        fun createRoute(id: Long) = "transaction_detail/$id"
    }
    object Notifications : Screen("notifications", "Notifications")
    object AddTransaction : Screen("add_transaction", "Add Transaction")
    object CategoryManagement : Screen("category_management", "Categories")
    object PinSetup : Screen("pin_setup", "Set Up PIN")
    object PinUnlock : Screen("pin_unlock", "Unlock App")
}

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    navController: NavHostController = rememberNavController()
) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val pendingTransactions by viewModel.pendingTransactions.collectAsState()
    val scamAlerts by viewModel.activeScamAlerts.collectAsState()
    val allNotifications by viewModel.allNotifications.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val isLoadingOlder by viewModel.isLoadingOlder.collectAsState()
    val deepLinkEvent by viewModel.deepLinkEvent.collectAsState()

    var initialLaunchHandled by remember { mutableStateOf(false) }

    LaunchedEffect(userProfile) {
        if (!initialLaunchHandled && userProfile != null) {
            initialLaunchHandled = true
            val hasPin = !userProfile?.pinHash.isNullOrBlank()
            val lockMode = userProfile?.lockMode ?: "PIN"

            if (userProfile?.email.isNullOrBlank()) {
                // Stay on Welcome
            } else if (!hasPin) {
                navController.navigate(Screen.PinSetup.route) {
                    popUpTo(0)
                }
            } else if (lockMode != "NONE") {
                navController.navigate(Screen.PinUnlock.route) {
                    popUpTo(0)
                }
            } else {
                navController.navigate(Screen.Home.route) {
                    popUpTo(0)
                }
            }
        }
    }

    val context = LocalContext.current

    LaunchedEffect(deepLinkEvent) {
        deepLinkEvent?.let { event ->
            when (event.destination) {
                "transaction_detail" -> {
                    if (event.transactionId > 0) {
                        val tx = viewModel.getTransactionById(event.transactionId)
                        if (tx != null && tx.status == "PENDING") {
                            navController.navigate(Screen.TransactionDetail.createRoute(event.transactionId))
                        } else {
                            if (tx != null) {
                                NotificationHelper.cancelTransactionNotification(context, tx.id)
                            }
                        }
                    } else {
                        val pending = viewModel.getPendingTransactionsList()
                        if (pending.isNotEmpty()) {
                            navController.navigate(Screen.PendingReview.route)
                        } else {
                            NotificationHelper.cancelPendingReviewNotification(context)
                        }
                    }
                }
                "pending_review" -> {
                    val pending = viewModel.getPendingTransactionsList()
                    if (pending.isNotEmpty()) {
                        navController.navigate(Screen.PendingReview.route)
                    } else {
                        NotificationHelper.cancelPendingReviewNotification(context)
                    }
                }
                "scam_alerts" -> {
                    navController.navigate(Screen.Notifications.route)
                }
            }
            viewModel.clearDeepLink()
        }
    }

    var showSimulateSmsDialog by remember { mutableStateOf(false) }
    var smsSender by remember { mutableStateOf("MTN MoMo") }
    var smsBody by remember { mutableStateOf("TxId: 981273. You have paid 4,250 RWF to Unknown Merchant on 25/07/2026. Bal: 18,200 RWF.") }

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Transactions,
        Screen.Reports,
        Screen.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isBottomBarVisible = currentRoute in listOf(
        Screen.Home.route,
        Screen.Transactions.route,
        Screen.Reports.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { item.icon?.let { Icon(it, contentDescription = item.title) } },
                            label = { Text(item.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = RoyalBlue,
                                selectedTextColor = RoyalBlue,
                                indicatorColor = RoyalBlue.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Welcome.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onGetStarted = { navController.navigate(Screen.CountrySelection.route) }
                )
            }

            composable(Screen.CountrySelection.route) {
                CountrySelectionScreen(
                    currentCountry = selectedCountry,
                    onCountrySelected = { viewModel.selectCountry(it) },
                    onContinue = { navController.navigate(Screen.SmsExplainer.route) }
                )
            }

            composable(Screen.SmsExplainer.route) {
                SmsExplainerScreen(
                    onPermissionGrantedOrSkipped = { navController.navigate(Screen.Login.route) }
                )
            }

            composable(Screen.Login.route) {
                LoginSignUpScreen(
                    authManager = viewModel.authManager,
                    onLoginSuccess = {
                        if (userProfile?.pinHash.isNullOrBlank()) {
                            navController.navigate(Screen.PinSetup.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        } else if (userProfile?.lockMode != "NONE") {
                            navController.navigate(Screen.PinUnlock.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Welcome.route) { inclusive = true }
                            }
                        }
                    },
                    onSignUpSuccess = {
                        if (userProfile?.pinHash.isNullOrBlank()) {
                            navController.navigate(Screen.PinSetup.route)
                        } else {
                            navController.navigate(Screen.ProfilePhoto.route)
                        }
                    }
                )
            }

            composable(Screen.PinSetup.route) {
                PinSetupScreen(
                    onPinSaved = { pin ->
                        viewModel.savePin(pin)
                        navController.navigate(Screen.ProfilePhoto.route)
                    }
                )
            }

            composable(Screen.PinUnlock.route) {
                PinUnlockScreen(
                    userProfile = userProfile,
                    onVerifyPin = { pin -> viewModel.verifyPin(pin) },
                    onUnlockSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0)
                        }
                    }
                )
            }

            composable(Screen.ProfilePhoto.route) {
                ProfilePhotoScreen(
                    authManager = viewModel.authManager,
                    onComplete = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeDashboardScreen(
                    userProfile = userProfile,
                    selectedCountry = selectedCountry,
                    transactions = allTransactions,
                    pendingTransactions = pendingTransactions,
                    onNavigateToPendingReview = { navController.navigate(Screen.PendingReview.route) },
                    onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                    onNavigateToAddTransaction = { navController.navigate(Screen.AddTransaction.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                    onTransactionClick = { id -> navController.navigate(Screen.TransactionDetail.createRoute(id)) },
                    onSimulateSmsClick = { showSimulateSmsDialog = true }
                )
            }

            composable(Screen.PendingReview.route) {
                PendingReviewScreen(
                    pendingTransactions = pendingTransactions,
                    categories = categories,
                    selectedCountry = selectedCountry,
                    onConfirmCategorization = { id, cat, desc, notes ->
                        viewModel.categorizePendingTransaction(id, cat, desc, notes)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.TransactionDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                val tx = allTransactions.find { it.id == id }
                TransactionDetailScreen(
                    transaction = tx,
                    categories = categories,
                    selectedCountry = selectedCountry,
                    onUpdateTransaction = { viewModel.updateTransaction(it) },
                    onDeleteTransaction = { viewModel.deleteTransaction(it) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Transactions.route) {
                TransactionsPageScreen(
                    transactions = allTransactions,
                    categories = categories,
                    selectedCountry = selectedCountry,
                    isLoadingOlder = isLoadingOlder,
                    onLoadOlderTransactions = { viewModel.loadOlderTransactions() },
                    onTransactionClick = { id -> navController.navigate(Screen.TransactionDetail.createRoute(id)) },
                    onAddTransactionClick = { navController.navigate(Screen.AddTransaction.route) }
                )
            }

            composable(Screen.Reports.route) {
                ReportsPageScreen(
                    transactions = allTransactions,
                    categories = categories,
                    selectedCountry = selectedCountry
                )
            }

            composable(Screen.Profile.route) {
                ProfileSettingsScreen(
                    userProfile = userProfile,
                    selectedCountry = selectedCountry,
                    onSelectCountry = { viewModel.selectCountry(it) },
                    onUpdateLockMode = { mode -> viewModel.updateLockMode(mode) },
                    onSavePin = { pin -> viewModel.savePin(pin) },
                    onVerifyPin = { pin -> viewModel.verifyPin(pin) },
                    onNavigateToCategoryManagement = { navController.navigate(Screen.CategoryManagement.route) },
                    onSignOut = {
                        viewModel.authManager.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    allNotifications = allNotifications,
                    scamAlerts = scamAlerts,
                    pendingTransactions = pendingTransactions,
                    onNavigateToTransactionDetail = { txId ->
                        navController.navigate(Screen.TransactionDetail.createRoute(txId))
                    },
                    onNavigateToPendingReview = {
                        navController.navigate(Screen.PendingReview.route)
                    },
                    onNavigateToReports = {
                        navController.navigate(Screen.Reports.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onOverrideScamAlert = { scamAlert ->
                        viewModel.overrideScamAlert(scamAlert)
                    },
                    onDeleteScamAlert = { scamAlert ->
                        viewModel.deleteScamAlert(scamAlert)
                    },
                    onDeleteNotification = { notif ->
                        viewModel.deleteNotification(notif)
                    },
                    onClearAllNotifications = {
                        viewModel.clearAllNotifications()
                    },
                    onTriggerPendingReminder = {
                        viewModel.triggerPendingReviewReminder()
                    },
                    onTriggerReportReady = {
                        viewModel.triggerMonthlyReportNotification()
                    },
                    onTriggerNewLogin = {
                        viewModel.triggerNewLoginNotification()
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AddTransaction.route) {
                AddTransactionScreen(
                    categories = categories,
                    selectedCountry = selectedCountry,
                    onSaveTransaction = { amount, cat, desc ->
                        viewModel.addManualTransaction(amount, cat, desc)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CategoryManagement.route) {
                CategoryManagementScreen(
                    categories = categories,
                    onAddCategory = { name, tag, kw, icon, color ->
                        viewModel.addCategory(name, tag, kw, icon, color)
                    },
                    onUpdateCategory = { cat, oldName ->
                        viewModel.updateCategory(cat, oldName)
                    },
                    onDeleteCategory = { cat ->
                        viewModel.deleteCategory(cat)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    if (showSimulateSmsDialog) {
        AlertDialog(
            onDismissRequest = { showSimulateSmsDialog = false },
            title = { Text("Simulate Incoming SMS Alert") },
            text = {
                Column {
                    Text("Test PromptPay's local BroadcastReceiver and pattern matching engine in-browser:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = smsSender,
                        onValueChange = { smsSender = it },
                        label = { Text("Sender") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = smsBody,
                        onValueChange = { smsBody = it },
                        label = { Text("SMS Message Body") },
                        modifier = Modifier.height(100.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.simulateSms(smsSender, smsBody)
                        showSimulateSmsDialog = false
                    }
                ) {
                    Text("Process SMS")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSimulateSmsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

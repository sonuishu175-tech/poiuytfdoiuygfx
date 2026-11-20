package com.example.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.data.AiRepository
import com.example.data.AuthHelper
import com.example.data.FirestoreRepository
import com.example.data.LocalAppStrings
import com.example.data.PreferencesRepository
import com.example.data.authStateFlow
import com.example.model.AiToolType
import com.example.model.UiState
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BusinessAiScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CodingAssistantScreen
import com.example.ui.screens.ContentWriterScreen
import com.example.ui.screens.CreditsScreen
import com.example.ui.screens.DailyPlannerScreen
import com.example.ui.screens.EmailWriterScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.HomeworkHelperScreen
import com.example.ui.screens.ImageAnalyzerScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.PdfAssistantScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.ResumeMakerScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StoryGeneratorScreen
import com.example.ui.screens.SubscriptionScreen
import com.example.ui.screens.TextSummarizerScreen
import com.example.ui.screens.ToolsHubScreen
import com.example.ui.screens.TranslatorScreen
import com.example.ui.screens.VoiceAssistantScreen
import com.example.ui.screens.YouTubeCreatorScreen
import com.example.ui.theme.AuroraBackground
import com.example.ui.theme.DarkPurple
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GoldGlowButton
import com.example.ui.theme.PremiumGold
import com.example.viewmodel.ChatViewModel
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.PlannerViewModel
import com.example.viewmodel.ToolViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class TopLevelTab(
    val route: AppRoute,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

private fun AiToolType.toAppRoute(): AppRoute = when (this) {
    AiToolType.AI_CHAT -> AppRoute.Chat()
    AiToolType.IMAGE_ANALYZER -> AppRoute.ImageAnalyzer
    AiToolType.VOICE_ASSISTANT -> AppRoute.VoiceAssistant
    AiToolType.TRANSLATOR -> AppRoute.Translator
    AiToolType.CONTENT_WRITER -> AppRoute.ContentWriter
    AiToolType.HOMEWORK_HELPER -> AppRoute.HomeworkHelper
    AiToolType.EMAIL_WRITER -> AppRoute.EmailWriter
    AiToolType.STORY_GENERATOR -> AppRoute.StoryGenerator
    AiToolType.CODING_ASSISTANT -> AppRoute.CodingAssistant
    AiToolType.RESUME_MAKER -> AppRoute.ResumeMaker
    AiToolType.PDF_ASSISTANT -> AppRoute.PdfAssistant
    AiToolType.YOUTUBE_CREATOR -> AppRoute.YouTubeCreator
    AiToolType.BUSINESS_AI -> AppRoute.BusinessAi
    AiToolType.TEXT_SUMMARIZER -> AppRoute.TextSummarizer
    AiToolType.DAILY_PLANNER -> AppRoute.DailyPlanner
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AppNavigation(
    preferencesRepository: PreferencesRepository,
    isDarkMode: Boolean,
    languageCode: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val authState by remember { Firebase.auth.authStateFlow() }
        .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)
    var isSplashFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1300L)
        AuthHelper.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { isSplashFinished = true },
            onUnauthenticated = { isSplashFinished = true },
            scope = scope
        )
    }

    if (!isSplashFinished) {
        SplashScreen()
        return
    }

    val currentUser = authState
    if (currentUser == null) {
        AuthScreen(onAuthSuccess = { /* authStateFlow transitions automatically */ })
        return
    }

    key(currentUser.uid) {
        val uid = currentUser.uid
        val appContext = context.applicationContext
        val firestoreRepository = remember(appContext) { FirestoreRepository(appContext) }
        val aiRepository = remember { AiRepository() }

        val mainViewModel: MainViewModel = viewModel(
            key = "main_$uid",
            factory = viewModelFactory {
                initializer { MainViewModel(firestoreRepository, preferencesRepository, uid) }
            }
        )
        val chatViewModel: ChatViewModel = viewModel(
            key = "chat_$uid",
            factory = viewModelFactory {
                initializer { ChatViewModel(firestoreRepository, aiRepository, uid) }
            }
        )
        val toolViewModel: ToolViewModel = viewModel(
            key = "tool_$uid",
            factory = viewModelFactory {
                initializer { ToolViewModel(firestoreRepository, aiRepository) }
            }
        )
        val plannerViewModel: PlannerViewModel = viewModel(
            key = "planner_$uid",
            factory = viewModelFactory {
                initializer { PlannerViewModel(firestoreRepository, aiRepository, uid) }
            }
        )

        val userProfileState by mainViewModel.userProfileState.collectAsStateWithLifecycle()
        val recentChats by mainViewModel.recentChats.collectAsStateWithLifecycle()
        val creditTransactions by mainViewModel.creditTransactions.collectAsStateWithLifecycle()
        val notifications by mainViewModel.notifications.collectAsStateWithLifecycle()
        val userFiles by mainViewModel.userFiles.collectAsStateWithLifecycle()
        val appConfig by mainViewModel.appConfig.collectAsStateWithLifecycle()
        val actionBanner by mainViewModel.actionBanner.collectAsStateWithLifecycle()

        val userProfile = (userProfileState as? UiState.Success)?.data
        val userCredits = userProfile?.credits ?: 20L
        val isFreePlan = (userProfile?.membership ?: "free") == "free"
        val unreadNotifications = remember(notifications) { notifications.count { !it.read } }

        val strings = LocalAppStrings.current
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(actionBanner) {
            val message = actionBanner
            if (!message.isNullOrBlank()) {
                snackbarHostState.showSnackbar(message)
                mainViewModel.clearBanner()
            }
        }

        if (appConfig.maintenanceMode) {
            AuroraBackground {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(
                        highlightGold = true,
                        modifier = Modifier.widthIn(max = 480.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PremiumGold
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Scheduled AI Upgrade",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "AI Assistant India is undergoing scheduled backend maintenance. Please try again in a few minutes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        GoldGlowButton(
                            text = strings.logout,
                            onClick = { AuthHelper.signOut(credentialManager, scope) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {

        val tabs = remember(strings) {
            listOf(
                TopLevelTab(AppRoute.Home, strings.navHome, Icons.Default.Home, "nav_home"),
                TopLevelTab(AppRoute.Tools, strings.navTools, Icons.Default.Build, "nav_tools"),
                TopLevelTab(AppRoute.Chat(), strings.navChat, Icons.AutoMirrored.Filled.Chat, "nav_chat"),
                TopLevelTab(AppRoute.Credits, strings.navCredits, Icons.Default.Stars, "nav_credits"),
                TopLevelTab(AppRoute.Profile, strings.navProfile, Icons.Default.Person, "nav_profile")
            )
        }

        val isTopLevelRoute = currentDestination?.hierarchy?.any { dest ->
            dest.hasRoute<AppRoute.Home>() ||
                dest.hasRoute<AppRoute.Tools>() ||
                dest.hasRoute<AppRoute.Chat>() ||
                dest.hasRoute<AppRoute.Credits>() ||
                dest.hasRoute<AppRoute.Profile>()
        } ?: true

        fun navigateTopLevel(route: AppRoute) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = route !is AppRoute.Chat
            }
        }

        AuroraBackground {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useRail = maxWidth >= 600.dp

                Row(modifier = Modifier.fillMaxSize()) {
                    if (useRail && isTopLevelRoute) {
                        NavigationRail(
                            containerColor = DarkPurple.copy(alpha = 0.92f),
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Spacer(Modifier.height(16.dp))
                            tabs.forEach { tab ->
                                val selected = when (tab.route) {
                                    is AppRoute.Home -> currentDestination?.hasRoute<AppRoute.Home>() == true
                                    is AppRoute.Tools -> currentDestination?.hasRoute<AppRoute.Tools>() == true
                                    is AppRoute.Chat -> currentDestination?.hasRoute<AppRoute.Chat>() == true
                                    is AppRoute.Credits -> currentDestination?.hasRoute<AppRoute.Credits>() == true
                                    is AppRoute.Profile -> currentDestination?.hasRoute<AppRoute.Profile>() == true
                                    else -> false
                                }
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTopLevel(tab.route) },
                                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                                    label = { Text(tab.label, maxLines = 1) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = DarkPurple,
                                        selectedTextColor = PremiumGold,
                                        indicatorColor = PremiumGold,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }

                    Scaffold(
                        modifier = Modifier.weight(1f),
                        containerColor = Color.Transparent,
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                            if (!useRail && isTopLevelRoute) {
                                NavigationBar(
                                    containerColor = DarkPurple.copy(alpha = 0.95f),
                                    windowInsets = WindowInsets.navigationBars
                                ) {
                                    tabs.forEach { tab ->
                                        val selected = when (tab.route) {
                                            is AppRoute.Home -> currentDestination?.hasRoute<AppRoute.Home>() == true
                                            is AppRoute.Tools -> currentDestination?.hasRoute<AppRoute.Tools>() == true
                                            is AppRoute.Chat -> currentDestination?.hasRoute<AppRoute.Chat>() == true
                                            is AppRoute.Credits -> currentDestination?.hasRoute<AppRoute.Credits>() == true
                                            is AppRoute.Profile -> currentDestination?.hasRoute<AppRoute.Profile>() == true
                                            else -> false
                                        }
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { navigateTopLevel(tab.route) },
                                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                                            label = { Text(tab.label, maxLines = 1) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = DarkPurple,
                                                selectedTextColor = PremiumGold,
                                                indicatorColor = PremiumGold,
                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            modifier = Modifier.testTag(tab.testTag)
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = AppRoute.Home,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            composable<AppRoute.Home> {
                                HomeDashboardScreen(
                                    userProfile = userProfile,
                                    recentChats = recentChats,
                                    unreadNotifications = unreadNotifications,
                                    languageCode = languageCode,
                                    onLanguageToggle = {
                                        val nextLang = if (languageCode == "hi") "en" else "hi"
                                        mainViewModel.updateLanguage(nextLang)
                                    },
                                    onAskAiSubmit = { prompt ->
                                        navController.navigate(AppRoute.Chat(initialPrompt = prompt))
                                    },
                                    onOpenTool = { tool ->
                                        navController.navigate(tool.toAppRoute())
                                    },
                                    onOpenChatSession = { chatId ->
                                        navController.navigate(AppRoute.Chat(chatId = chatId))
                                    },
                                    onOpenCredits = { navigateTopLevel(AppRoute.Credits) },
                                    onOpenSubscriptions = { navController.navigate(AppRoute.Subscriptions) },
                                    onOpenNotifications = { navController.navigate(AppRoute.Notifications) },
                                    onOpenProfile = { navigateTopLevel(AppRoute.Profile) }
                                )
                            }

                            composable<AppRoute.Tools> {
                                ToolsHubScreen(
                                    languageCode = languageCode,
                                    isFreePlan = isFreePlan,
                                    onOpenTool = { tool ->
                                        navController.navigate(tool.toAppRoute())
                                    }
                                )
                            }

                            composable<AppRoute.Chat> { backStackEntry ->
                                val route = backStackEntry.toRoute<AppRoute.Chat>()
                                ChatScreen(
                                    chatViewModel = chatViewModel,
                                    recentChats = recentChats,
                                    userCredits = userCredits,
                                    defaultLanguage = languageCode,
                                    initialPrompt = route.initialPrompt,
                                    initialChatId = route.chatId,
                                    onOpenCredits = { navigateTopLevel(AppRoute.Credits) }
                                )
                            }

                            composable<AppRoute.Credits> {
                                CreditsScreen(
                                    userProfile = userProfile,
                                    transactions = creditTransactions,
                                    actionBanner = actionBanner,
                                    onClaimDailyCredits = { mainViewModel.claimDailyFreeCredits() },
                                    onRewardEarned = { mainViewModel.onRewardedAdEarned() },
                                    onOpenSubscriptions = { navController.navigate(AppRoute.Subscriptions) },
                                    onClearBanner = { mainViewModel.clearBanner() }
                                )
                            }

                            composable<AppRoute.Profile> {
                                ProfileSettingsScreen(
                                    userProfile = userProfile,
                                    isDarkMode = isDarkMode,
                                    languageCode = languageCode,
                                    onToggleDarkMode = { mainViewModel.toggleDarkMode(it) },
                                    onSelectLanguage = { mainViewModel.updateLanguage(it) },
                                    onToggleNotifications = { mainViewModel.toggleNotifications(it) },
                                    onUpdateName = { mainViewModel.updateName(it) },
                                    onOpenSubscriptions = { navController.navigate(AppRoute.Subscriptions) },
                                    onOpenChatHistory = { navigateTopLevel(AppRoute.Chat()) },
                                    onLogout = { AuthHelper.signOut(credentialManager, scope) },
                                    onDeleteAccount = {
                                        scope.launch {
                                            val res = AuthHelper.deleteAccount(
                                                credentialManager = credentialManager,
                                                firestoreRepository = firestoreRepository
                                            )
                                            if (res.isFailure) {
                                                snackbarHostState.showSnackbar(
                                                    res.exceptionOrNull()?.localizedMessage
                                                        ?: "Please sign out and sign back in recently before deleting your account."
                                                )
                                            }
                                        }
                                    }
                                )
                            }

                            composable<AppRoute.Subscriptions> {
                                SubscriptionScreen(
                                    userProfile = userProfile,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<AppRoute.Notifications> {
                                NotificationsScreen(
                                    notifications = notifications,
                                    onMarkRead = { mainViewModel.markNotificationRead(it) },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<AppRoute.ImageAnalyzer> {
                                LaunchedEffect(Unit) { toolViewModel.reset() }
                                ImageAnalyzerScreen(
                                    toolViewModel = toolViewModel,
                                    userCredits = userCredits,
                                    onOpenCredits = { navigateTopLevel(AppRoute.Credits) },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<AppRoute.VoiceAssistant> {
                                LaunchedEffect(Unit) { toolViewModel.reset() }
                                VoiceAssistantScreen(
                                    toolViewModel = toolViewModel,
                                    userCredits = userCredits,
                                    defaultLanguage = languageCode,
                                    onOpenCredits = { navigateTopLevel(AppRoute.Credits) },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<AppRoute.Translator> {
                                LaunchedEffect(Unit) { toolViewModel.reset() }
                                TranslatorScreen(
                                    toolViewModel = toolViewModel,
                                    userCredits = userCredits,
                                    onOpenCredits = { navigateTopLevel(AppRoute.Credits) },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<AppRoute.ContentWriter> {
                                LaunchedEffect(Unit) { toolViewModel.reset() }
                                ContentWriterScreen(
                                    toolViewModel = toolViewModel,
                                    userCredits = userCredits,
                                    defaultLanguage = languageCode,
                                    onOpenCredits = { navigateTopLevel(AppRoute.Credits) },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<AppRoute.HomeworkHelper> {
                                LaunchedEffect(Unit) { toolViewModel.reset() }
                                HomeworkHelperScreen(
                                    toolViewModel = toolViewModel,
                                    userCredits = userCredits,
                                    onOpenCredits = { navigateTopLevel(AppRoute.Credits) },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<AppRoute.EmailWriter> {
                                LaunchedEffect(Unit) { toolViewModel.reset() }
                                EmailWriterScreen(
                                    toolViewModel = toolViewModel,
                                    userCredits = userCredits,
                                    defaultLanguage = languageCode,
                                    onOpenCredits = { navigateTopLevel(AppRoute.Credits) },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<AppRoute.StoryGenerator> {
                                LaunchedEffect(Unit) { toolViewModel.reset() }
                                StoryGeneratorScreen(
                                    toolViewModel = toolViewModel,
                                    userCredits = userCredits,
                                    defaultLanguage = languageCode,
                                    onOpenCredits = { navigateTopLevel(AppRoute.Credits) },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<AppRoute.CodingAssistant> {
                                LaunchedEffect(Unit) { toolViewModel.reset() }
                                CodingAssistantScreen(
                                    toolViewModel = toolViewModel,
                                    userCredits = userCredits,
                                    onOpenCredits = { navigateTopLevel(AppRoute.Credits) },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<AppRoute.ResumeMaker> {
                                LaunchedEffect(Unit) { toolViewModel.reset() }
                                ResumeMakerScreen(
                                    toolViewModel = toolViewModel,
                                    userCredits = userCredits,
                                    onOpenCredits = { navigateTopLevel(AppRoute.Credits) },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<AppRoute.PdfAssistant> {
                                LaunchedEffect(Unit) { toolViewModel.reset() }
                                PdfAssistantScreen(
                                    toolViewModel = toolViewModel,
                                    userCredits = userCredits,
                                    savedFiles = userFiles,
                                    onSaveFileRecord = { name, path, size, summary ->
                                        mainViewModel.saveFileSummary(name, path, size, summary)
                                    },
                                    onDeleteFileRecord = { mainViewModel.deleteFileRecord(it) },
                                    onOpenCredits = { navigateTopLevel(AppRoute.Credits) },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<AppRoute.YouTubeCreator> {
                                LaunchedEffect(Unit) { toolViewModel.reset() }
                                YouTubeCreatorScreen(
                                    toolViewModel = toolViewModel,
                                    userCredits = userCredits,
                                    defaultLanguage = languageCode,
                                    onOpenCredits = { navigateTopLevel(AppRoute.Credits) },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<AppRoute.BusinessAi> {
                                LaunchedEffect(Unit) { toolViewModel.reset() }
                                BusinessAiScreen(
                                    toolViewModel = toolViewModel,
                                    userCredits = userCredits,
                                    defaultLanguage = languageCode,
                                    onOpenCredits = { navigateTopLevel(AppRoute.Credits) },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<AppRoute.TextSummarizer> {
                                LaunchedEffect(Unit) { toolViewModel.reset() }
                                TextSummarizerScreen(
                                    toolViewModel = toolViewModel,
                                    userCredits = userCredits,
                                    onOpenCredits = { navigateTopLevel(AppRoute.Credits) },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<AppRoute.DailyPlanner> {
                                DailyPlannerScreen(
                                    plannerViewModel = plannerViewModel,
                                    userCredits = userCredits,
                                    defaultLanguage = languageCode,
                                    onOpenCredits = { navigateTopLevel(AppRoute.Credits) },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.data.SupabaseClientProvider
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.Screen
import kotlinx.coroutines.launch

class DashboardActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SupabaseClientProvider.client.auth.currentSessionOrNull() == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        lifecycleScope.launch {
            viewModel.refreshCloudState()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                SupabaseClientProvider.client.auth.sessionStatus.collect { status ->
                    if (status is io.github.jan.supabase.auth.status.SessionStatus.NotAuthenticated) {
                        startActivity(Intent(this@DashboardActivity, MainActivity::class.java))
                        finish()
                    }
                }
            }
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val feedbackMessage by viewModel.userFeedbackMessage.collectAsStateWithLifecycle()
                val isErrorMessage by viewModel.isErrorMessage.collectAsStateWithLifecycle()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }

                // Show feedback messages
                LaunchedEffect(feedbackMessage) {
                    feedbackMessage?.let {
                        snackbarHostState.showSnackbar(
                            message = it,
                            duration = SnackbarDuration.Short
                        )
                        viewModel.clearFeedback()
                    }
                }

                // Handle back button
                BackHandler(enabled = currentScreen != Screen.Dashboard || drawerState.isOpen) {
                    if (drawerState.isOpen) {
                        scope.launch { drawerState.close() }
                    } else {
                        viewModel.navigateBack()
                    }
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            modifier = Modifier.width(320.dp),
                            drawerContainerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(vertical = 12.dp)
                            ) {
                                // Drawer Header
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = NavyPrimary)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "DevProperti SAK EP",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(
                                            text = "PT Gema Abadi Nugraha",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AccentGoldLight
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Sistem Manajemen Developer Terpadu",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                DrawerSectionHeader(title = "EKSEKUTIF & AUDIT")
                                DrawerItem("Dashboard Owner", Icons.Default.Dashboard, currentScreen == Screen.Dashboard) {
                                    viewModel.navigateTo(Screen.Dashboard)
                                    scope.launch { drawerState.close() }
                                }
                                DrawerItem("Audit & Kontrol Sistem", Icons.AutoMirrored.Filled.FactCheck, currentScreen == Screen.AuditKontrol) {
                                    viewModel.navigateTo(Screen.AuditKontrol)
                                    scope.launch { drawerState.close() }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                DrawerSectionHeader(title = "OPERASIONAL & ANGGARAN")
                                DrawerItem("Buku Kas & Bank", Icons.Default.AccountBalanceWallet, currentScreen == Screen.Transaksi) {
                                    viewModel.navigateTo(Screen.Transaksi)
                                    scope.launch { drawerState.close() }
                                }
                                DrawerItem("Kontrol Anggaran (RAB)", Icons.Default.PieChart, currentScreen == Screen.Anggaran) {
                                    viewModel.navigateTo(Screen.Anggaran)
                                    scope.launch { drawerState.close() }
                                }
                                DrawerItem("Kas Kecil (Petty Cash)", Icons.Default.Payments, currentScreen == Screen.PettyCash) {
                                    viewModel.navigateTo(Screen.PettyCash)
                                    scope.launch { drawerState.close() }
                                }
                                DrawerItem("Payroll & Absensi", Icons.Default.Badge, currentScreen == Screen.Payroll) {
                                    viewModel.navigateTo(Screen.Payroll)
                                    scope.launch { drawerState.close() }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                DrawerSectionHeader(title = "PROPERTI & KONSTRUKSI")
                                DrawerItem("Penjualan & Stok Unit", Icons.Default.Home, currentScreen == Screen.PenjualanUnit) {
                                    viewModel.navigateTo(Screen.PenjualanUnit)
                                    scope.launch { drawerState.close() }
                                }
                                DrawerItem("SPK Kontraktor", Icons.Default.Engineering, currentScreen == Screen.Kontraktor) {
                                    viewModel.navigateTo(Screen.Kontraktor)
                                    scope.launch { drawerState.close() }
                                }
                                DrawerItem("Intercompany Induk-Anak", Icons.Default.SwapHoriz, currentScreen == Screen.Intercompany) {
                                    viewModel.navigateTo(Screen.Intercompany)
                                    scope.launch { drawerState.close() }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                DrawerSectionHeader(title = "KEUANGAN, LEGAL & AI")
                                DrawerItem("Jurnal Otomatis", Icons.AutoMirrored.Filled.MenuBook, currentScreen == Screen.Jurnal) {
                                    viewModel.navigateTo(Screen.Jurnal)
                                    scope.launch { drawerState.close() }
                                }
                                DrawerItem("Kepatuhan Pajak", Icons.Default.AccountBalance, currentScreen == Screen.Pajak) {
                                    viewModel.navigateTo(Screen.Pajak)
                                    scope.launch { drawerState.close() }
                                }
                                DrawerItem("Surat & Kwitansi Resmi", Icons.Default.Receipt, currentScreen == Screen.DokumenKwitansi) {
                                    viewModel.navigateTo(Screen.DokumenKwitansi)
                                    scope.launch { drawerState.close() }
                                }
                                DrawerItem("Asisten AI & Scan", Icons.Default.Psychology, currentScreen == Screen.AiAdvisor) {
                                    viewModel.navigateTo(Screen.AiAdvisor)
                                    scope.launch { drawerState.close() }
                                }
                            }
                        }
                    }
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = {
                                    Column {
                                        Text(
                                            text = currentScreen.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "Gunung Padang, Ciamis • PT Gema Abadi Nugraha",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = { scope.launch { drawerState.open() } },
                                        modifier = Modifier.testTag("btn_drawer_open")
                                    ) {
                                        Icon(Icons.Default.Menu, contentDescription = "Buka Menu")
                                    }
                                },
                                actions = {
                                    IconButton(
                                        onClick = { viewModel.navigateTo(Screen.AuditKontrol) },
                                        modifier = Modifier.testTag("btn_top_audit")
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.FactCheck, contentDescription = "Audit Status", tint = NavyPrimary)
                                    }
                                    IconButton(
                                        onClick = {
                                            lifecycleScope.launch {
                                                SupabaseClientProvider.client.auth.signOut()
                                                startActivity(Intent(this@DashboardActivity, MainActivity::class.java))
                                                finish()
                                            }
                                        },
                                        modifier = Modifier.testTag("btn_logout")
                                    ) {
                                        Icon(Icons.Default.Logout, contentDescription = "Keluar")
                                    }

                                    IconButton(
                                        onClick = { viewModel.navigateTo(Screen.AiAdvisor) },
                                        modifier = Modifier.testTag("btn_top_ai")
                                    ) {
                                        Icon(Icons.Default.Psychology, contentDescription = "AI Advisor", tint = AccentGold)
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 3.dp
                            ) {
                                NavigationBarItem(
                                    selected = currentScreen == Screen.Dashboard,
                                    onClick = { viewModel.navigateTo(Screen.Dashboard) },
                                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                                    label = { Text("Dashboard") },
                                    modifier = Modifier.testTag("nav_bottom_dashboard")
                                )
                                NavigationBarItem(
                                    selected = currentScreen == Screen.Transaksi,
                                    onClick = { viewModel.navigateTo(Screen.Transaksi) },
                                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Kas/Bank") },
                                    label = { Text("Kas/Bank") },
                                    modifier = Modifier.testTag("nav_bottom_transaksi")
                                )
                                NavigationBarItem(
                                    selected = currentScreen == Screen.Anggaran,
                                    onClick = { viewModel.navigateTo(Screen.Anggaran) },
                                    icon = { Icon(Icons.Default.PieChart, contentDescription = "RAB") },
                                    label = { Text("RAB") },
                                    modifier = Modifier.testTag("nav_bottom_anggaran")
                                )
                                NavigationBarItem(
                                    selected = currentScreen == Screen.Payroll,
                                    onClick = { viewModel.navigateTo(Screen.Payroll) },
                                    icon = { Icon(Icons.Default.Badge, contentDescription = "Payroll") },
                                    label = { Text("Payroll") },
                                    modifier = Modifier.testTag("nav_bottom_payroll")
                                )
                                NavigationBarItem(
                                    selected = currentScreen == Screen.DokumenKwitansi,
                                    onClick = { viewModel.navigateTo(Screen.DokumenKwitansi) },
                                    icon = { Icon(Icons.Default.Receipt, contentDescription = "Kwitansi") },
                                    label = { Text("Kwitansi") },
                                    modifier = Modifier.testTag("nav_bottom_kwitansi")
                                )
                            }
                        },
                        snackbarHost = {
                            SnackbarHost(hostState = snackbarHostState) { data ->
                                Snackbar(
                                    snackbarData = data,
                                    containerColor = if (isErrorMessage) StatusRed else NavyPrimary,
                                    contentColor = Color.White
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentScreen) {
                                Screen.Dashboard -> DashboardOwnerScreen(viewModel = viewModel)
                                Screen.Transaksi -> TransaksiScreen(viewModel = viewModel)
                                Screen.Anggaran -> AnggaranScreen(viewModel = viewModel)
                                Screen.PenjualanUnit -> PenjualanUnitScreen(viewModel = viewModel)
                                Screen.Payroll -> PayrollScreen(viewModel = viewModel)
                                Screen.DokumenKwitansi -> DokumenKwitansiScreen(viewModel = viewModel)
                                Screen.Jurnal -> JurnalScreen(viewModel = viewModel)
                                Screen.AuditKontrol -> AuditKontrolScreen(viewModel = viewModel)
                                Screen.AiAdvisor -> AiAdvisorScreen(viewModel = viewModel)
                                Screen.Intercompany -> IntercompanyScreen(viewModel = viewModel)
                                Screen.PettyCash -> PettyCashScreen(viewModel = viewModel)
                                Screen.Pajak -> PajakScreen(viewModel = viewModel)
                                Screen.Kontraktor -> KontraktorScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
    )
}

@Composable
private fun DrawerItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
        icon = { Icon(icon, contentDescription = title) },
        selected = isSelected,
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = NavyLight,
            selectedIconColor = NavyPrimary,
            selectedTextColor = NavyPrimary
        )
    )
}

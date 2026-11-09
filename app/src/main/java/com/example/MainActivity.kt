package com.example

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.example.ui.McKaysViewModel
import com.example.ui.components.*
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.McKaysTheme
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {

    private val viewModel: McKaysViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Background authentication check
        val auth = Firebase.auth
        if (auth.currentUser == null) {
            // Attempt silent background sign-in if credentials exist
            lifecycleScope.launch {
                try {
                    val clientId = getString(R.string.default_web_client_id)
                    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
                    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()
                    // Silent check
                } catch (e: Exception) {
                    Log.d("MainActivity", "Initial silent check: ${e.message}")
                }
            }
        }

        setContent {
            val darkMode by viewModel.darkMode.collectAsState()
            val currentUser by viewModel.currentUser.collectAsState()

            McKaysTheme(darkTheme = darkMode) {
                if (currentUser == null) {
                    LoginScreen(
                        viewModel = viewModel,
                        onGoogleSignInClick = { performGoogleSignIn() }
                    )
                } else {
                    MainAppContent(
                        viewModel = viewModel,
                        currentUser = currentUser!!
                    )
                }

                // Global Toast display
                GlobalToastHost(viewModel = viewModel)
            }
        }
    }

    private fun performGoogleSignIn() {
        val context = this
        val credentialManager = CredentialManager.create(context)
        lifecycleScope.launch {
            try {
                val clientId = getString(R.string.default_web_client_id)
                val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
                val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

                val result = credentialManager.getCredential(context as Activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    Firebase.auth.signInWithCredential(authCredential).await()
                    viewModel.showToast("Google Sign-In berhasil! Data cloud tersinkronisasi.")
                } else {
                    viewModel.showToast("Tipe kredensial Google tidak dikenali.")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("MainActivity", "Google Sign-In flow cancelled: ${e.message}")
            } catch (e: Exception) {
                Log.e("MainActivity", "Google Sign-In failed", e)
                viewModel.showToast("Google Sign-In: ${e.localizedMessage ?: "Gagal masuk"}")
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: McKaysViewModel,
    currentUser: com.example.data.model.UserSession
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val darkMode by viewModel.darkMode.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var isSalesModalOpen by remember { mutableStateOf(false) }

    // Intercept back button to return to home tab if not on home
    BackHandler(enabled = currentTab != "home") {
        viewModel.setTab("home")
    }

    val surfaceHeader = if (darkMode) Color(0xFF090D16).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.95f)
    val borderColor = if (darkMode) Color(0xFF1F2937) else Color(0xFFE2E8F0)
    val textPrimary = if (darkMode) Color.White else Color(0xFF0F172A)
    val textMuted = Color(0xFF94A3B8)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = if (darkMode) Color(0xFF090D16) else Color(0xFFF8FAFC),
        topBar = {
            // Sticky Top Header with Safe Insets
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceHeader)
                    .statusBarsPadding()
                    .border(1.dp, borderColor)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo + App Name + Role
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BluePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!settings.appLogo.isNullOrBlank()) {
                                AsyncImage(
                                    model = settings.appLogo,
                                    contentDescription = "Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                androidx.compose.foundation.Image(
                                    painter = painterResource(id = R.drawable.mckays_logo_icon),
                                    contentDescription = "McKays Logo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "McKays",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = textPrimary
                            )
                            Text(
                                text = if (currentUser.role == "admin") "Admin Pusat" else "Cabang: ${currentUser.branch}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textMuted
                            )
                        }
                    }

                    // Header Right Actions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Dark mode toggle
                        IconButton(
                            onClick = { viewModel.setDarkMode(!darkMode) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (darkMode) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                        ) {
                            Icon(
                                imageVector = if (darkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Theme",
                                tint = if (darkMode) Color(0xFFFBBF24) else Color(0xFF475569),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // User profile avatar thumbnail
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BluePrimary.copy(alpha = 0.2f))
                                .border(1.dp, BluePrimary.copy(alpha = 0.4f), CircleShape)
                                .clickable { viewModel.setTab("profile") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!currentUser.profilePic.isNullOrBlank()) {
                                AsyncImage(
                                    model = currentUser.profilePic,
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = currentUser.username.take(2).uppercase(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BluePrimary
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Floating Bottom Navigation Bar (Glass effect)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 420.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(if (darkMode) Color(0xFF111827).copy(alpha = 0.92f) else Color.White.copy(alpha = 0.95f))
                        .border(1.dp, if (darkMode) Color(0xFF374151) else Color(0xFFE2E8F0), RoundedCornerShape(32.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FloatingNavItem(
                        selected = currentTab == "home",
                        onClick = { viewModel.setTab("home") },
                        icon = Icons.Default.TrendingUp,
                        label = "Home",
                        darkMode = darkMode
                    )
                    FloatingNavItem(
                        selected = currentTab == "stock",
                        onClick = { viewModel.setTab("stock") },
                        icon = Icons.Default.Inventory2,
                        label = "Stok",
                        darkMode = darkMode
                    )
                    FloatingNavItem(
                        selected = currentTab == "history",
                        onClick = { viewModel.setTab("history") },
                        icon = Icons.Default.Description,
                        label = "Riwayat",
                        darkMode = darkMode
                    )
                    FloatingNavItem(
                        selected = currentTab == "member",
                        onClick = { viewModel.setTab("member") },
                        icon = Icons.Default.Group,
                        label = "Member",
                        darkMode = darkMode
                    )
                    FloatingNavItem(
                        selected = currentTab == "profile",
                        onClick = { viewModel.setTab("profile") },
                        icon = Icons.Default.Settings,
                        label = "Akun",
                        darkMode = darkMode
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "home" -> HomeTab(
                    viewModel = viewModel,
                    currentUser = currentUser,
                    onOpenSalesModal = { isSalesModalOpen = true }
                )
                "stock" -> StockTab(
                    viewModel = viewModel,
                    currentUser = currentUser
                )
                "history" -> HistoryTab(
                    viewModel = viewModel,
                    currentUser = currentUser
                )
                "member" -> MemberTab(
                    viewModel = viewModel,
                    currentUser = currentUser
                )
                "profile" -> ProfileTab(
                    viewModel = viewModel,
                    currentUser = currentUser
                )
            }

            // Sales modal dialog
            if (isSalesModalOpen) {
                SalesModal(
                    viewModel = viewModel,
                    branch = currentUser.branch,
                    onDismiss = { isSalesModalOpen = false }
                )
            }
        }
    }
}

@Composable
fun FloatingNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    darkMode: Boolean
) {
    val bgColor = if (selected) BluePrimary else Color.Transparent
    val contentColor = if (selected) Color.White else Color(0xFF94A3B8)

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("nav_$label"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
            color = contentColor
        )
    }
}

@Composable
fun GlobalToastHost(viewModel: McKaysViewModel) {
    val toastMsg by viewModel.toastMessage.collectAsState()

    LaunchedEffect(toastMsg) {
        if (toastMsg != null) {
            delay(3000)
            viewModel.clearToast()
        }
    }

    AnimatedVisibility(
        visible = toastMsg != null,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            toastMsg?.let { msg ->
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = msg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

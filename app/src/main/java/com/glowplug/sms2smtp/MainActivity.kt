package com.glowplug.sms2smtp

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val OpenSauceFont = FontFamily(
    Font(R.font.open_sauce_regular, FontWeight.Normal),
    Font(R.font.open_sauce_medium, FontWeight.Medium),
    Font(R.font.open_sauce_semibold, FontWeight.SemiBold),
    Font(R.font.open_sauce_bold, FontWeight.Bold)
)

val AppTypography = Typography(
    displayLarge = androidx.compose.ui.text.TextStyle(fontFamily = OpenSauceFont, fontWeight = FontWeight.Normal, fontSize = 57.sp),
    displayMedium = androidx.compose.ui.text.TextStyle(fontFamily = OpenSauceFont, fontWeight = FontWeight.Normal, fontSize = 45.sp),
    displaySmall = androidx.compose.ui.text.TextStyle(fontFamily = OpenSauceFont, fontWeight = FontWeight.Normal, fontSize = 36.sp),
    headlineLarge = androidx.compose.ui.text.TextStyle(fontFamily = OpenSauceFont, fontWeight = FontWeight.Normal, fontSize = 32.sp),
    headlineMedium = androidx.compose.ui.text.TextStyle(fontFamily = OpenSauceFont, fontWeight = FontWeight.Normal, fontSize = 28.sp),
    headlineSmall = androidx.compose.ui.text.TextStyle(fontFamily = OpenSauceFont, fontWeight = FontWeight.Normal, fontSize = 24.sp),
    titleLarge = androidx.compose.ui.text.TextStyle(fontFamily = OpenSauceFont, fontWeight = FontWeight.Normal, fontSize = 22.sp),
    titleMedium = androidx.compose.ui.text.TextStyle(fontFamily = OpenSauceFont, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    titleSmall = androidx.compose.ui.text.TextStyle(fontFamily = OpenSauceFont, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    bodyLarge = androidx.compose.ui.text.TextStyle(fontFamily = OpenSauceFont, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = androidx.compose.ui.text.TextStyle(fontFamily = OpenSauceFont, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = androidx.compose.ui.text.TextStyle(fontFamily = OpenSauceFont, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge = androidx.compose.ui.text.TextStyle(fontFamily = OpenSauceFont, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = androidx.compose.ui.text.TextStyle(fontFamily = OpenSauceFont, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = androidx.compose.ui.text.TextStyle(fontFamily = OpenSauceFont, fontWeight = FontWeight.Medium, fontSize = 11.sp)
)

val AppColorScheme = darkColorScheme(
    primary = Color(0xFFFF6B00),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFF6B00),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFFF6B00),
    onSecondary = Color.White,
    background = Color(0xFF2F2F2F),
    onBackground = Color.White,
    surface = Color(0xFF2F2F2F),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF454545),
    onSurfaceVariant = Color.White
)

class MainActivity : ComponentActivity() {
    private val requestSmsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // No-op: app behavior degrades gracefully when permission is denied.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestSmsPermissionIfNeeded()

        val app = application as SmsApp
        
        setContent {
            MaterialTheme(colorScheme = AppColorScheme, typography = AppTypography) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SmsAppNavHost(app)
                }
            }
        }
    }

    private fun requestSmsPermissionIfNeeded() {
        val hasReceiveSmsPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasReceiveSmsPermission) {
            requestSmsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
        }
    }
}

@Composable
fun SmsAppNavHost(app: SmsApp) {
    var currentScreen by remember { mutableStateOf("main") }

    when (currentScreen) {
        "main" -> MainScreen(app, onNavigateToSettings = { currentScreen = "settings" })
        "settings" -> SettingsScreen(app, onNavigateBack = { currentScreen = "main" })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(app: SmsApp, onNavigateToSettings: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val isEnabled by app.settingsManager.isEnabledFlow.collectAsState(initial = false)
    val logs by app.database.smsLogDao().getAllLogs().collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Easy SMS 2 SMTP for Android") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Switch(
                checked = isEnabled,
                onCheckedChange = { 
                    coroutineScope.launch { app.settingsManager.setEnabled(it) }
                },
                modifier = Modifier.scale(1.5f)
            )
            Text(
                text = if (isEnabled) "Service is ON" else "Service is OFF",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 32.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Message Logs (${logs.size})", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = { showDialog = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Clear Logs", tint = MaterialTheme.colorScheme.error)
                }
            }
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(logs) { log ->
                    LogItem(log)
                }
            }

            val uriHandler = LocalUriHandler.current
            val annotatedString = buildAnnotatedString {
                append("Developed by ")
                pushStringAnnotation(tag = "URL", annotation = "https://glowplug.studio")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
                    append("Glowplug")
                }
                pop()
                append(" in Thailand.")
            }

            ClickableText(
                text = annotatedString,
                onClick = { offset ->
                    annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset).firstOrNull()?.let { annotation ->
                        uriHandler.openUri(annotation.item)
                    }
                },
                style = androidx.compose.ui.text.TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Clear Log") },
                text = { Text("Are you sure you want to delete all logs?") },
                confirmButton = {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            app.database.smsLogDao().deleteAllLogs()
                        }
                        showDialog = false
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogItem(log: SmsLogEntity) {
    var showErrorDialog by remember { mutableStateOf(false) }
    var showMessageDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    val formattedDate = remember(log.timestamp) {
        val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        formatter.format(Date(log.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = { showMessageDialog = true }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("From: ${log.sender}", fontWeight = FontWeight.Bold)
                val isPending = !log.isForwarded && log.errorMessage.isNullOrBlank()
                if (log.isForwarded) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Forwarded", tint = Color.Green)
                } else if (isPending) {
                    Icon(Icons.Filled.Schedule, contentDescription = "Pending", tint = Color(0xFFFFC107))
                } else {
                    IconButton(
                        onClick = { showErrorDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Filled.Error, contentDescription = "Failed", tint = Color.Red)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("To: ${log.forwardedToEmail}", style = MaterialTheme.typography.bodySmall)
            Text("Subj: ${log.subject}", style = MaterialTheme.typography.bodySmall)
            Text("Time: $formattedDate", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(log.messageBody, style = MaterialTheme.typography.bodyMedium)
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error Details") },
            text = { Text(log.errorMessage ?: "Unknown error") },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("Close")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Error Log", log.errorMessage ?: "Unknown error")
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy")
                    }
                }
            }
        )
    }

    if (showMessageDialog) {
        AlertDialog(
            onDismissRequest = { showMessageDialog = false },
            title = { Text("Message Body") },
            text = { Text(log.messageBody) },
            confirmButton = {
                TextButton(onClick = { showMessageDialog = false }) {
                    Text("Close")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Message Body", log.messageBody)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy")
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(app: SmsApp, onNavigateBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var host by rememberSaveable { mutableStateOf("") }
    var port by rememberSaveable { mutableStateOf("") }
    var user by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }
    var from by rememberSaveable { mutableStateOf("") }
    var to by rememberSaveable { mutableStateOf("") }
    var subject by rememberSaveable { mutableStateOf("") }
    var loadedInitialValues by rememberSaveable { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<Pair<Boolean, String?>?>(null) }
    
    val initialHost by app.settingsManager.smtpHostFlow.collectAsState(initial = "")
    val initialPort by app.settingsManager.smtpPortFlow.collectAsState(initial = "")
    val initialUser by app.settingsManager.smtpUserFlow.collectAsState(initial = "")
    val initialPass by app.settingsManager.smtpPassFlow.collectAsState(initial = "")
    val initialFrom by app.settingsManager.fromEmailFlow.collectAsState(initial = "")
    val initialTo by app.settingsManager.toEmailFlow.collectAsState(initial = "")
    val initialSubject by app.settingsManager.subjectTemplateFlow.collectAsState(initial = "")

    LaunchedEffect(initialHost, initialPort, initialUser, initialPass, initialFrom, initialTo, initialSubject) {
        if (!loadedInitialValues) {
            host = initialHost
            port = initialPort
            user = initialUser
            pass = initialPass
            from = initialFrom
            to = initialTo
            subject = initialSubject
            loadedInitialValues = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SMTP Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { OutlinedTextField(value = host, onValueChange = { host = it }, label = { Text("SMTP Host") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(value = port, onValueChange = { port = it }, label = { Text("SMTP Port") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(value = user, onValueChange = { user = it }, label = { Text("SMTP Username") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
            item { 
                OutlinedTextField(
                    value = pass, 
                    onValueChange = { pass = it }, 
                    label = { Text("SMTP Password") }, 
                    singleLine = true, 
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = "Toggle password visibility")
                        }
                    }
                ) 
            }
            item { OutlinedTextField(value = from, onValueChange = { from = it }, label = { Text("From Email") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(value = to, onValueChange = { to = it }, label = { Text("To Email") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject Template (Use {SENDER})") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
            
            if (testResult?.first == true) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Check, contentDescription = "Success", tint = Color.Green)
                        Spacer(Modifier.width(8.dp))
                        Text("Connected Ok, dont forget to save", color = Color.Green)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            isTesting = true
                            testResult = null
                            coroutineScope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    val testSubject = subject.replace("{SENDER}", "Test Sender")
                                    val body = "This is a Hello World test email from Easy SMS 2 SMTP for Android app to verify your settings."
                                    EmailSender.sendEmailSync(host, port, user, pass, from, to, testSubject, body)
                                }
                                testResult = result
                                isTesting = false
                            }
                        },
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        enabled = !isTesting
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
                        } else {
                            Text("Test")
                        }
                    }
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                app.settingsManager.saveSmtpSettings(host, port, user, pass, from, to, subject)
                                Toast.makeText(context, "Settings saved successfully", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
        
        testResult?.let { result ->
            if (!result.first) {
                AlertDialog(
                    onDismissRequest = { testResult = null },
                    title = { Text("Connection Failed") },
                    text = { Text(result.second ?: "Unknown error") },
                    confirmButton = {
                        TextButton(onClick = { testResult = null }) {
                            Text("Close")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Error Log", result.second)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy")
                            }
                        }
                    }
                )
            }
        }
    }
}
// Modifier helper for scale
fun Modifier.scale(scale: Float) = this.then(
    Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
)

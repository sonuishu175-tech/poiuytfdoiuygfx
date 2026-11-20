package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.data.AuthHelper
import com.example.data.LocalAppStrings
import com.example.ui.theme.AuroraBackground
import com.example.ui.theme.DarkPurple
import com.example.ui.theme.ElectricIris
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.GlassCard
import com.example.ui.theme.GoldGlowButton
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.WarmGold
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val strings = LocalAppStrings.current

    var isGoogleLoading by remember { mutableStateOf(false) }
    var showPhoneLogin by remember { mutableStateOf(false) }
    var phoneInput by remember { mutableStateOf("+91") }
    var otpInput by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var isPhoneLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AuroraBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .border(
                            2.5.dp,
                            Brush.linearGradient(listOf(PremiumGold, ElectricIris)),
                            CircleShape
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_robot_emblem),
                        contentDescription = "AI Assistant India Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = strings.appTitle,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PremiumGold
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "15+ Intelligent AI Tools • Hindi & English • 20 Daily Free Credits",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                GlassCard(modifier = Modifier.fillMaxWidth(), highlightGold = true) {
                    Text(
                        text = "Sign in to sync your AI chats, credits & daily planner",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(16.dp))

                    // Primary Google Sign-In via Jetpack Credential Manager
                    GoldGlowButton(
                        text = strings.signInGoogle,
                        onClick = {
                            errorMessage = null
                            isGoogleLoading = true
                            AuthHelper.onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isGoogleLoading = false
                                    onAuthSuccess()
                                },
                                onAuthError = { msg ->
                                    isGoogleLoading = false
                                    errorMessage = msg
                                },
                                scope = scope,
                                onAuthCancelled = {
                                    isGoogleLoading = false
                                }
                            )
                        },
                        isLoading = isGoogleLoading,
                        enabled = !isGoogleLoading && !isPhoneLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("google_sign_in_button")
                    )

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            text = "  OR  ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            showPhoneLogin = !showPhoneLogin
                            errorMessage = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("toggle_phone_login_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = "Phone login",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(strings.signInPhone)
                    }

                    AnimatedVisibility(visible = showPhoneLogin) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it.take(16) },
                                label = { Text("Phone Number (with country code, e.g. +91...)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("phone_number_input")
                            )

                            Spacer(Modifier.height(10.dp))

                            if (verificationId == null) {
                                Button(
                                    onClick = {
                                        val activity = context as? Activity ?: return@Button
                                        val cleanPhone = phoneInput.trim()
                                        if (cleanPhone.length < 10 || !cleanPhone.startsWith("+")) {
                                            errorMessage = "Enter valid phone number with country code (e.g. +919876543210)"
                                            return@Button
                                        }
                                        errorMessage = null
                                        isPhoneLoading = true
                                        AuthHelper.sendPhoneOtp(
                                            activity = activity,
                                            phoneNumberE164 = cleanPhone,
                                            onCodeSent = { verId ->
                                                isPhoneLoading = false
                                                verificationId = verId
                                            },
                                            onAutoVerified = {
                                                isPhoneLoading = false
                                                onAuthSuccess()
                                            },
                                            onError = { msg ->
                                                isPhoneLoading = false
                                                errorMessage = msg
                                            },
                                            scope = scope
                                        )
                                    },
                                    enabled = !isPhoneLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("send_otp_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    if (isPhoneLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                    } else {
                                        Text("Send OTP")
                                    }
                                }
                            } else {
                                OutlinedTextField(
                                    value = otpInput,
                                    onValueChange = { otpInput = it.filter(Char::isDigit).take(6) },
                                    label = { Text("Enter 6-digit OTP") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("otp_code_input")
                                )
                                Spacer(Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        val verId = verificationId ?: return@Button
                                        if (otpInput.length < 6) {
                                            errorMessage = "Enter the 6-digit OTP sent to your phone."
                                            return@Button
                                        }
                                        isPhoneLoading = true
                                        errorMessage = null
                                        scope.launch {
                                            val res = AuthHelper.verifyPhoneOtp(verId, otpInput)
                                            isPhoneLoading = false
                                            if (res.isSuccess) {
                                                onAuthSuccess()
                                            } else {
                                                errorMessage = res.exceptionOrNull()?.localizedMessage
                                                    ?: "Invalid OTP"
                                            }
                                        }
                                    },
                                    enabled = !isPhoneLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("verify_otp_button")
                                ) {
                                    Text(if (isPhoneLoading) "Verifying..." else "Verify OTP")
                                }
                                TextButton(
                                    onClick = {
                                        verificationId = null
                                        otpInput = ""
                                    },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Change Phone Number")
                                }
                            }
                        }
                    }

                    if (!errorMessage.isNullOrBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = errorMessage!!,
                            color = ErrorCoral,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = WarmGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Secured by Firebase Auth & App Check • Zero keys on device",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

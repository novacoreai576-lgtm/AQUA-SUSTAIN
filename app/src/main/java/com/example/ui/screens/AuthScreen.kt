package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AuthUiState
import com.example.ui.AquaSustainViewModel
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertRedContainer
import com.example.ui.theme.AquaPrimary
import com.example.ui.theme.EcoGreen
import com.example.ui.theme.NeutralDark
import com.example.ui.theme.NeutralMedium

enum class AuthScreenMode {
    LOGIN,
    REGISTER
}

@Composable
fun AuthScreen(
    viewModel: AquaSustainViewModel,
    authUiState: AuthUiState,
    modifier: Modifier = Modifier
) {
    var screenMode by remember { mutableStateOf(AuthScreenMode.LOGIN) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    // Login Form State
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Register Form State
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Prevent Android Back button from doing anything invalid or returning to dashboard
    BackHandler(enabled = true) {
        if (screenMode == AuthScreenMode.REGISTER) {
            viewModel.clearAuthError()
            screenMode = AuthScreenMode.LOGIN
        }
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        var resetEmail by remember { mutableStateOf(loginEmail) }
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Text("Reset Password", fontWeight = FontWeight.Bold, color = AquaPrimary)
            },
            text = {
                Column {
                    Text(
                        "Enter your account email to receive a password reset link from Firebase:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NeutralDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_password_email_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AquaPrimary,
                            focusedLabelColor = AquaPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sendPasswordReset(resetEmail)
                        showForgotPasswordDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary),
                    modifier = Modifier.testTag("confirm_reset_password_button")
                ) {
                    Text("Send Reset Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel", color = NeutralMedium)
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 28.dp)
                .widthIn(max = 460.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // AquaSustain Brand Header
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(AquaPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = "AquaSustain Logo",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "AquaSustain",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = AquaPrimary
            )

            Text(
                text = "Smart Water Management for a Sustainable Future",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = EcoGreen,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Authentication Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                        .padding(22.dp)
                ) {
                    // Header inside card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (screenMode == AuthScreenMode.REGISTER) {
                            IconButton(
                                onClick = {
                                    viewModel.clearAuthError()
                                    screenMode = AuthScreenMode.LOGIN
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back to Login", tint = AquaPrimary)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = if (screenMode == AuthScreenMode.LOGIN) "Sign In to AquaSustain" else "Create Account",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeutralDark
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Error Message Banner
                    AnimatedVisibility(
                        visible = authUiState is AuthUiState.Error,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        if (authUiState is AuthUiState.Error) {
                            val isUserCollision = authUiState.message.contains("already exists", ignoreCase = true) ||
                                    authUiState.message.contains("already in use", ignoreCase = true)

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AlertRedContainer)
                                    .border(1.dp, AlertRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = "Error",
                                        tint = AlertRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = authUiState.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AlertRed,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.clearAuthError() },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Dismiss",
                                            tint = AlertRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                if (isUserCollision && screenMode == AuthScreenMode.REGISTER) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            if (regEmail.isNotBlank()) {
                                                loginEmail = regEmail.trim()
                                            }
                                            if (regPassword.isNotBlank()) {
                                                loginPassword = regPassword
                                            }
                                            viewModel.clearAuthError()
                                            screenMode = AuthScreenMode.LOGIN
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("switch_to_login_button")
                                    ) {
                                        Text("Switch to Sign In", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    // Success Message Banner (e.g. for Password Reset)
                    AnimatedVisibility(
                        visible = authUiState is AuthUiState.Success,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        if (authUiState is AuthUiState.Success) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = authUiState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF166534),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    if (screenMode == AuthScreenMode.LOGIN) {
                        // ==========================================
                        // LOGIN SCREEN
                        // Fields: Email, Password
                        // Buttons: LOGIN, CREATE ACCOUNT
                        // ==========================================
                        OutlinedTextField(
                            value = loginEmail,
                            onValueChange = { loginEmail = it },
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = AquaPrimary)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_email_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AquaPrimary,
                                focusedLabelColor = AquaPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = AquaPrimary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                    Icon(
                                        imageVector = if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (loginPasswordVisible) "Hide password" else "Show password",
                                        tint = NeutralMedium
                                    )
                                }
                            },
                            visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                viewModel.signIn(loginEmail, loginPassword)
                            }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AquaPrimary,
                                focusedLabelColor = AquaPrimary
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showForgotPasswordDialog = true },
                                modifier = Modifier.testTag("forgot_password_button")
                            ) {
                                Text("Forgot Password?", color = AquaPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // LOGIN BUTTON
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.signIn(loginEmail, loginPassword)
                            },
                            enabled = authUiState !is AuthUiState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("auth_sign_in_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AquaPrimary)
                        ) {
                            if (authUiState is AuthUiState.Loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text("LOGIN", fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 0.5.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // CREATE ACCOUNT BUTTON
                        OutlinedButton(
                            onClick = {
                                viewModel.clearAuthError()
                                screenMode = AuthScreenMode.REGISTER
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("auth_go_to_register_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EcoGreen)
                        ) {
                            Text("CREATE ACCOUNT", fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 0.5.sp)
                        }

                    } else {
                        // ==========================================
                        // REGISTER SCREEN
                        // Fields: Name, Email, Password, Confirm Password
                        // Button: CREATE ACCOUNT
                        // ==========================================
                        OutlinedTextField(
                            value = regName,
                            onValueChange = { regName = it },
                            label = { Text("Name") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = AquaPrimary)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AquaPrimary,
                                focusedLabelColor = AquaPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = AquaPrimary)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_reg_email_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AquaPrimary,
                                focusedLabelColor = AquaPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = AquaPrimary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                    Icon(
                                        imageVector = if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = NeutralMedium
                                    )
                                }
                            },
                            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_reg_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AquaPrimary,
                                focusedLabelColor = AquaPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regConfirmPassword,
                            onValueChange = { regConfirmPassword = it },
                            label = { Text("Confirm Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = AquaPrimary)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                viewModel.signUp(regName, regEmail, regPassword, regConfirmPassword)
                            }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_confirm_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AquaPrimary,
                                focusedLabelColor = AquaPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.signUp(regName, regEmail, regPassword, regConfirmPassword)
                            },
                            enabled = authUiState !is AuthUiState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("auth_sign_up_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EcoGreen)
                        ) {
                            if (authUiState is AuthUiState.Loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Text("CREATE ACCOUNT", fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 0.5.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TextButton(
                            onClick = {
                                viewModel.clearAuthError()
                                screenMode = AuthScreenMode.LOGIN
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Already have an account? Sign In", color = AquaPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Secured with Firebase Authentication",
                style = MaterialTheme.typography.labelSmall,
                color = NeutralMedium,
                fontSize = 11.sp
            )
        }
    }
}

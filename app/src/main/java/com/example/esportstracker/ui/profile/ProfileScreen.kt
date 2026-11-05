package com.example.esportstracker.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    vm: ProfileViewModel = viewModel()
) {
    val user by vm.user.collectAsState()
    val message by vm.message.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        val u = user
        if (u == null) {
            Box(Modifier.padding(padding).fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            var name by remember(u.displayName) { mutableStateOf(u.displayName) }
            var oldPw by remember { mutableStateOf("") }
            var newPw by remember { mutableStateOf("") }
            var confirmPw by remember { mutableStateOf("") }

            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Tên đăng nhập: ${u.username}", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên hiển thị") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = { vm.updateDisplayName(name) }) { Text("Lưu tên hiển thị") }

                HorizontalDivider()

                Text("Đổi mật khẩu", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = oldPw,
                    onValueChange = { oldPw = it },
                    label = { Text("Mật khẩu hiện tại") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPw,
                    onValueChange = { newPw = it },
                    label = { Text("Mật khẩu mới") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPw,
                    onValueChange = { confirmPw = it },
                    label = { Text("Nhập lại mật khẩu mới") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(onClick = {
                    vm.changePassword(oldPw, newPw, confirmPw) {
                        oldPw = ""
                        newPw = ""
                        confirmPw = ""
                    }
                }) { Text("Đổi mật khẩu") }

                HorizontalDivider()

                OutlinedButton(onClick = onLogout) { Text("Đăng xuất") }
                OutlinedButton(
                    onClick = { showDelete = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Xóa tài khoản") }
            }
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Xóa tài khoản?") },
            text = {
                Text("Toàn bộ đội yêu thích, dự đoán và nhắc lịch của bạn sẽ bị xóa. Không thể hoàn tác.")
            },
            confirmButton = {
                TextButton(onClick = { showDelete = false; vm.deleteAccount() }) { Text("Xóa") }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Hủy") } }
        )
    }
}
package com.example.esportstracker.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
private fun AuthLayout(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        content()
    }
}

@Composable
private fun AuthField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation =
            if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
fun LoginScreen(vm: AuthViewModel, onGoRegister: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val error by vm.error.collectAsState()
    val busy by vm.busy.collectAsState()

    LaunchedEffect(Unit) { vm.clearError() }

    AuthLayout("Đăng nhập") {
        AuthField(username, { username = it }, "Tên đăng nhập")
        AuthField(password, { password = it }, "Mật khẩu", isPassword = true)
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }
        Button(
            onClick = { vm.login(username, password) },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (busy) "Đang xử lý..." else "Đăng nhập") }
        TextButton(onClick = onGoRegister) { Text("Chưa có tài khoản? Đăng ký") }
    }
}

@Composable
fun RegisterScreen(vm: AuthViewModel, onGoLogin: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val error by vm.error.collectAsState()
    val busy by vm.busy.collectAsState()

    LaunchedEffect(Unit) { vm.clearError() }

    AuthLayout("Tạo tài khoản") {
        AuthField(username, { username = it }, "Tên đăng nhập")
        AuthField(displayName, { displayName = it }, "Tên hiển thị")
        AuthField(password, { password = it }, "Mật khẩu", isPassword = true)
        AuthField(confirm, { confirm = it }, "Nhập lại mật khẩu", isPassword = true)
        (localError ?: error)?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }
        Button(
            onClick = {
                if (password != confirm) {
                    localError = "Mật khẩu nhập lại không khớp"
                } else {
                    localError = null
                    vm.register(username, password, displayName)
                }
            },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (busy) "Đang xử lý..." else "Đăng ký") }
        TextButton(onClick = onGoLogin) { Text("Đã có tài khoản? Đăng nhập") }
    }
}
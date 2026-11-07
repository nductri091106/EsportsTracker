package com.example.esportstracker.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.esportstracker.ui.components.AppPrimaryButton
import com.example.esportstracker.ui.components.AppTextField
import com.example.esportstracker.ui.theme.AppColors

@Composable
private fun AuthScaffold(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        content = content
    )
}

@Composable
private fun AuthHeader() {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(AppColors.PrimaryTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.SportsEsports,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "ESPORTS TRACKER",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Mọi trận đấu. Một điểm hẹn.",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextMuted
        )
    }
}

@Composable
private fun ErrorRow(message: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Outlined.ErrorOutline,
            contentDescription = null,
            tint = AppColors.Danger,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = AppColors.Danger)
    }
}

@Composable
private fun AuthSwitchLink(prefix: String, action: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(prefix, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextMuted)
        Text(
            action,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable(onClick = onClick)
                .padding(4.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.Primary
        )
    }
}

@Composable
fun LoginScreen(vm: AuthViewModel, onGoRegister: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val error by vm.error.collectAsState()
    val busy by vm.busy.collectAsState()

    LaunchedEffect(Unit) { vm.clearError() }

    AuthScaffold {
        AuthHeader()
        Spacer(Modifier.height(32.dp))
        Text(
            "Chào mừng trở lại",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
        Spacer(Modifier.height(20.dp))
        AppTextField(
            value = username,
            onValueChange = { username = it; if (error != null) vm.clearError() },
            label = "Tên đăng nhập",
            placeholder = "Nhập tên đăng nhập",
            error = error != null
        )
        Spacer(Modifier.height(12.dp))
        AppTextField(
            value = password,
            onValueChange = { password = it; if (error != null) vm.clearError() },
            label = "Mật khẩu",
            placeholder = "Nhập mật khẩu",
            isPassword = true,
            error = error != null
        )
        error?.let {
            Spacer(Modifier.height(12.dp))
            ErrorRow(it)
        }
        Spacer(Modifier.height(20.dp))
        AppPrimaryButton("Đăng nhập", onClick = { vm.login(username, password) }, loading = busy)
        Spacer(Modifier.height(16.dp))
        AuthSwitchLink("Chưa có tài khoản? ", "Đăng ký", onGoRegister)
        Spacer(Modifier.height(32.dp))
        Text(
            "Theo dõi Liên Minh Huyền Thoại và Valorant",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextMuted,
            textAlign = TextAlign.Center
        )
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

    AuthScaffold {
        AuthHeader()
        Spacer(Modifier.height(28.dp))
        Text(
            "Tạo tài khoản",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
        Spacer(Modifier.height(20.dp))
        AppTextField(
            value = username,
            onValueChange = { username = it; vm.clearError() },
            label = "Tên đăng nhập",
            placeholder = "Nhập tên đăng nhập"
        )
        Spacer(Modifier.height(12.dp))
        AppTextField(
            value = displayName,
            onValueChange = { displayName = it; vm.clearError() },
            label = "Tên hiển thị",
            placeholder = "Bạn muốn được gọi là gì?"
        )
        Spacer(Modifier.height(12.dp))
        AppTextField(
            value = password,
            onValueChange = { password = it; vm.clearError() },
            label = "Mật khẩu",
            placeholder = "Tối thiểu 8 ký tự",
            isPassword = true
        )
        Spacer(Modifier.height(12.dp))
        AppTextField(
            value = confirm,
            onValueChange = { confirm = it; localError = null },
            label = "Nhập lại mật khẩu",
            placeholder = "Nhập lại mật khẩu",
            isPassword = true,
            error = localError != null
        )
        (localError ?: error)?.let {
            Spacer(Modifier.height(12.dp))
            ErrorRow(it)
        }
        Spacer(Modifier.height(20.dp))
        AppPrimaryButton(
            "Đăng ký",
            onClick = {
                if (password != confirm) {
                    localError = "Mật khẩu nhập lại không khớp."
                } else {
                    localError = null
                    vm.register(username, password, displayName)
                }
            },
            loading = busy
        )
        Spacer(Modifier.height(16.dp))
        AuthSwitchLink("Đã có tài khoản? ", "Đăng nhập", onGoLogin)
    }
}
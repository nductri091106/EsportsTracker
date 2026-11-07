package com.example.esportstracker.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esportstracker.data.auth.UserEntity
import com.example.esportstracker.ui.components.AppPrimaryButton
import com.example.esportstracker.ui.components.AppTextField
import com.example.esportstracker.ui.components.ScreenHeader
import com.example.esportstracker.ui.theme.AppColors

@Composable
fun ProfileScreen(
    onOpenAccountOptions: () -> Unit,
    vm: ProfileViewModel = viewModel()
) {
    val user by vm.user.collectAsState()
    val message by vm.message.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader("Hồ sơ")
            val u = user
            if (u == null) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
            } else {
                ProfileForm(u, vm, onOpenAccountOptions)
            }
        }
        SnackbarHost(
            snackbar,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }
}

@Composable
private fun ProfileForm(
    u: UserEntity,
    vm: ProfileViewModel,
    onOpenAccountOptions: () -> Unit
) {
    var name by remember(u.displayName) { mutableStateOf(u.displayName) }
    var oldPw by remember { mutableStateOf("") }
    var newPw by remember { mutableStateOf("") }
    var confirmPw by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.Person,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Tên đăng nhập: ${u.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextMuted
            )
        }

        AppTextField(
            value = name,
            onValueChange = { name = it },
            label = "Tên hiển thị",
            placeholder = "Bạn muốn được gọi là gì?"
        )
        AppPrimaryButton("Lưu tên hiển thị", onClick = { vm.updateDisplayName(name) })

        Text(
            "Đổi mật khẩu",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
        AppTextField(
            value = oldPw,
            onValueChange = { oldPw = it },
            label = "Mật khẩu hiện tại",
            placeholder = "Nhập mật khẩu hiện tại",
            isPassword = true
        )
        AppTextField(
            value = newPw,
            onValueChange = { newPw = it },
            label = "Mật khẩu mới",
            placeholder = "Tối thiểu 8 ký tự",
            isPassword = true
        )
        AppTextField(
            value = confirmPw,
            onValueChange = { confirmPw = it },
            label = "Nhập lại mật khẩu mới",
            placeholder = "Nhập lại mật khẩu mới",
            isPassword = true
        )
        AppPrimaryButton(
            "Đổi mật khẩu",
            onClick = {
                vm.changePassword(oldPw, newPw, confirmPw) {
                    oldPw = ""
                    newPw = ""
                    confirmPw = ""
                }
            }
        )

        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onOpenAccountOptions)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Tùy chọn tài khoản",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Primary
            )
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}
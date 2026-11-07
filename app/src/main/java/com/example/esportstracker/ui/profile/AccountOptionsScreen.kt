package com.example.esportstracker.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esportstracker.ui.components.*
import com.example.esportstracker.ui.theme.AppColors

@Composable
fun AccountOptionsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    vm: ProfileViewModel = viewModel()
) {
    val user by vm.user.collectAsState()
    var showDelete by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Hồ sơ")
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            AppTextAction("Về thông tin cá nhân", onBack, Icons.AutoMirrored.Filled.ArrowBack)
            Spacer(Modifier.height(8.dp))

            user?.let { u ->
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TeamBadge(u.displayName.take(1), null, size = 48.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                u.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )
                            Text(
                                "Tên đăng nhập: ${u.username}",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Tùy chọn tài khoản",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Bạn có thể đăng xuất khỏi thiết bị này hoặc xóa vĩnh viễn tài khoản.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextMuted
            )

            Spacer(Modifier.weight(1f))

            AppOutlinedButton("Đăng xuất", onClick = onLogout, icon = Icons.AutoMirrored.Outlined.Logout)
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = AppColors.Border)
            Spacer(Modifier.height(12.dp))
            Text(
                "Xóa tài khoản sẽ xóa các đội yêu thích, dự đoán và lời nhắc của bạn. Không thể hoàn tác.",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextMuted
            )
            Spacer(Modifier.height(12.dp))
            AppDangerButton("Xóa tài khoản", onClick = { showDelete = true }, icon = Icons.Outlined.Delete)
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDelete) {
        AppDialog(
            title = "Xóa tài khoản?",
            onDismiss = { showDelete = false },
            confirmText = "Xóa",
            onConfirm = { showDelete = false; vm.deleteAccount() }
        ) {
            Text(
                "Toàn bộ đội yêu thích, dự đoán và nhắc lịch của bạn sẽ bị xóa. Không thể hoàn tác.",
                color = AppColors.TextMuted
            )
        }
    }
}
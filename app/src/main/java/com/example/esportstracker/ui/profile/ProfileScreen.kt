package com.example.esportstracker.ui.profile

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.esportstracker.data.auth.UserEntity
import com.example.esportstracker.ui.components.AppDialog
import com.example.esportstracker.ui.components.AppTextField
import com.example.esportstracker.ui.theme.AppColors

private enum class ProfileDialog { Name, Password, Delete }

@Composable
fun ProfileScreen(
    onOpenTab: (String) -> Unit,
    onLogout: () -> Unit,
    vm: ProfileViewModel = viewModel()
) {
    val user by vm.user.collectAsState()
    val stats by vm.stats.collectAsState()
    val message by vm.message.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var dialog by remember { mutableStateOf<ProfileDialog?>(null) }
    val context = LocalContext.current

    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }

    Box(Modifier.fillMaxSize()) {
        val u = user
        if (u == null) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = AppColors.Primary)
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(24.dp))
                ProfileHeader(u, onEdit = { dialog = ProfileDialog.Name })
                Spacer(Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(stats.favorites, "Yêu thích", Modifier.weight(1f)) { onOpenTab("favorites") }
                    StatTile(stats.predictions, "Dự đoán", Modifier.weight(1f)) { onOpenTab("predictions") }
                    StatTile(stats.reminders, "Nhắc lịch", Modifier.weight(1f)) { onOpenTab("reminders") }
                }
                Spacer(Modifier.height(24.dp))

                SettingsGroup("Tài khoản") {
                    SettingsRow(
                        icon = Icons.Outlined.Badge,
                        title = "Tên hiển thị",
                        subtitle = u.displayName,
                        onClick = { dialog = ProfileDialog.Name }
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Outlined.Lock,
                        title = "Đổi mật khẩu",
                        onClick = { dialog = ProfileDialog.Password }
                    )
                }

                SettingsGroup("Ứng dụng") {
                    SettingsRow(
                        icon = Icons.Outlined.Notifications,
                        title = "Cài đặt thông báo",
                        subtitle = "Quản lý quyền nhận lời nhắc trận đấu",
                        onClick = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            context.startActivity(intent)
                        }
                    )
                }

                SettingsGroup {
                    SettingsRow(
                        icon = Icons.AutoMirrored.Outlined.Logout,
                        title = "Đăng xuất",
                        onClick = onLogout
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Outlined.Delete,
                        title = "Xóa tài khoản",
                        subtitle = "Xóa vĩnh viễn dữ liệu của bạn",
                        danger = true,
                        onClick = { dialog = ProfileDialog.Delete }
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        SnackbarHost(
            snackbar,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
    }

    when (dialog) {
        ProfileDialog.Name -> EditNameDialog(
            current = user?.displayName ?: "",
            onDismiss = { dialog = null },
            onSave = { name, done -> vm.updateDisplayName(name, done) }
        )
        ProfileDialog.Password -> ChangePasswordDialog(
            onDismiss = { dialog = null },
            onSubmit = { old, new, confirm, done -> vm.changePassword(old, new, confirm, done) }
        )
        ProfileDialog.Delete -> AppDialog(
            title = "Xóa tài khoản?",
            onDismiss = { dialog = null },
            confirmText = "Xóa",
            onConfirm = { dialog = null; vm.deleteAccount() }
        ) {
            Text(
                "Toàn bộ đội yêu thích, dự đoán và nhắc lịch của bạn sẽ bị xóa. Không thể hoàn tác.",
                color = AppColors.TextMuted
            )
        }
        null -> {}
    }
}

// ====================== Thành phần ======================

@Composable
private fun ProfileHeader(u: UserEntity, onEdit: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            Box(
                Modifier
                    .size(104.dp)
                    .clip(CircleShape)
                    .background(AppColors.PrimaryTint)
                    .border(2.dp, AppColors.Primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    u.displayName.take(1).uppercase(),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary
                )
            }
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AppColors.Primary)
                    .border(2.dp, AppColors.Background, CircleShape)
                    .clickable(onClick = onEdit),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Edit,
                    contentDescription = "Sửa tên hiển thị",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            u.displayName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
        Text(
            "@${u.username}",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextMuted
        )
    }
}

@Composable
private fun StatTile(value: Int, label: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = AppColors.Surface,
        border = BorderStroke(1.dp, AppColors.Border)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "$value",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AppColors.Primary
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = AppColors.TextMuted
            )
        }
    }
}

@Composable
private fun SettingsGroup(title: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
        if (title != null) {
            Text(
                title,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = AppColors.TextMuted
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = AppColors.Surface,
            border = BorderStroke(1.dp, AppColors.Border)
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(modifier = Modifier.padding(start = 64.dp), color = AppColors.Border)
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    subtitle: String? = null,
    danger: Boolean = false
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (danger) AppColors.DangerTint else AppColors.PrimaryTint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (danger) AppColors.Danger else AppColors.Primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (danger) AppColors.Danger else AppColors.TextPrimary
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (!danger) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = AppColors.TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ====================== Hộp thoại ======================

@Composable
private fun EditNameDialog(
    current: String,
    onDismiss: () -> Unit,
    onSave: (String, (String?) -> Unit) -> Unit
) {
    var name by remember { mutableStateOf(current) }
    var error by remember { mutableStateOf<String?>(null) }

    AppDialog(
        title = "Đổi tên hiển thị",
        onDismiss = onDismiss,
        confirmText = "Lưu",
        onConfirm = {
            onSave(name) { err -> if (err == null) onDismiss() else error = err }
        }
    ) {
        AppTextField(
            value = name,
            onValueChange = { name = it; error = null },
            label = "Tên hiển thị",
            placeholder = "Bạn muốn được gọi là gì?",
            error = error != null,
            helper = error
        )
    }
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, (String?) -> Unit) -> Unit
) {
    var old by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AppDialog(
        title = "Đổi mật khẩu",
        onDismiss = onDismiss,
        confirmText = "Lưu",
        onConfirm = {
            onSubmit(old, new, confirm) { err -> if (err == null) onDismiss() else error = err }
        }
    ) {
        Column(
            Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppTextField(
                value = old,
                onValueChange = { old = it; error = null },
                label = "Mật khẩu hiện tại",
                placeholder = "Nhập mật khẩu hiện tại",
                isPassword = true
            )
            AppTextField(
                value = new,
                onValueChange = { new = it; error = null },
                label = "Mật khẩu mới",
                placeholder = "Tối thiểu 8 ký tự",
                isPassword = true
            )
            AppTextField(
                value = confirm,
                onValueChange = { confirm = it; error = null },
                label = "Nhập lại mật khẩu mới",
                placeholder = "Nhập lại mật khẩu mới",
                isPassword = true
            )
            error?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = AppColors.Danger)
            }
        }
    }
}
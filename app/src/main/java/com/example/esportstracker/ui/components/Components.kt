package com.example.esportstracker.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.esportstracker.ui.theme.AppColors

// ---------- Thẻ ----------

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    borderColor: Color = AppColors.Border,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val border = BorderStroke(1.dp, borderColor)
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = AppColors.Surface,
            border = border
        ) { Column(Modifier.padding(16.dp), content = content) }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = AppColors.Surface,
            border = border
        ) { Column(Modifier.padding(16.dp), content = content) }
    }
}

// ---------- Nút ----------

@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().height(48.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.Primary,
            contentColor = Color.White,
            disabledContainerColor = AppColors.Primary.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        )
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
        } else {
            Text(text, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun AppOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = AppColors.Primary,
    borderColor: Color = AppColors.Border,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(48.dp),
        shape = CircleShape,
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun AppDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) = AppOutlinedButton(
    text = text,
    onClick = onClick,
    modifier = modifier,
    color = AppColors.Danger,
    borderColor = AppColors.Danger,
    icon = icon
)

@Composable
fun AppTextAction(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    color: Color = AppColors.Primary
) {
    TextButton(onClick = onClick, colors = ButtonDefaults.textButtonColors(contentColor = color)) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

// ---------- Ô nhập ----------

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    error: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    helper: String? = null
) {
    var visible by remember { mutableStateOf(false) }
    val trailing: (@Composable () -> Unit)? = if (isPassword) {
        @Composable {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    if (visible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    contentDescription = "Hiện/ẩn mật khẩu",
                    tint = AppColors.TextMuted
                )
            }
        }
    } else null

    Column(modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (error) AppColors.Danger else AppColors.TextMuted
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = AppColors.TextMuted.copy(alpha = 0.7f)) },
            singleLine = singleLine,
            minLines = minLines,
            isError = error,
            shape = RoundedCornerShape(12.dp),
            visualTransformation =
                if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = trailing,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AppColors.Surface,
                unfocusedContainerColor = AppColors.Surface,
                errorContainerColor = AppColors.Surface,
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = AppColors.Border,
                errorBorderColor = AppColors.Danger,
                cursorColor = AppColors.Primary,
                focusedTextColor = AppColors.TextPrimary,
                unfocusedTextColor = AppColors.TextPrimary,
                errorTextColor = AppColors.TextPrimary
            )
        )
        if (helper != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                helper,
                style = MaterialTheme.typography.labelSmall,
                color = if (error) AppColors.Danger else AppColors.TextMuted
            )
        }
    }
}

// ---------- Chip ----------

@Composable
fun AppChip(
    text: String,
    textColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    dot: Boolean = false
) {
    Surface(modifier = modifier, shape = CircleShape, color = containerColor) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (dot) {
                Box(Modifier.size(6.dp).background(textColor, CircleShape))
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text,
                color = textColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LiveChip(modifier: Modifier = Modifier) =
    AppChip("LIVE", AppColors.Danger, AppColors.DangerTint, modifier, dot = true)

@Composable
fun TimeChip(text: String, modifier: Modifier = Modifier) =
    AppChip(text, AppColors.Cyan, AppColors.CyanTint, modifier)

@Composable
fun FinishedChip(modifier: Modifier = Modifier) =
    AppChip("KẾT THÚC", AppColors.TextMuted, AppColors.SurfaceHigh, modifier)

@Composable
fun AppFilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) AppColors.PrimaryTint else AppColors.SurfaceHigh
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = if (selected) AppColors.Primary else AppColors.TextMuted,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ---------- Huy hiệu đội ----------

@Composable
fun TeamBadge(
    label: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    dimmed: Boolean = false
) {
    Box(
        modifier
            .size(size)
            .alpha(if (dimmed) 0.45f else 1f)
            .clip(CircleShape)
            .background(AppColors.SurfaceHigh)
            .border(1.dp, AppColors.Border, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = label,
                modifier = Modifier.fillMaxSize().padding(size / 6)
            )
        } else {
            Text(
                label.take(4).uppercase(),
                color = AppColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.3f).sp
            )
        }
    }
}

// ---------- Tiêu đề ----------

@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
        if (actionText != null && onAction != null) {
            AppTextAction(actionText, onAction)
        }
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextPrimary
        )
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.bodySmall, color = AppColors.TextMuted)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = AppColors.TextPrimary,
            navigationIconContentColor = AppColors.TextPrimary,
            actionIconContentColor = AppColors.Primary
        )
    )
}

// ---------- Trạng thái ----------

@Composable
fun StateMessage(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    iconTint: Color = AppColors.Primary,
    actionText: String? = null,
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(64.dp).clip(CircleShape).background(AppColors.SurfaceHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextMuted,
            textAlign = TextAlign.Center
        )
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onAction,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary,
                    contentColor = Color.White
                )
            ) {
                if (actionIcon != null) {
                    Icon(actionIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(actionText, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun SkeletonBox(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(8.dp)) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "skeletonAlpha"
    )
    Box(modifier.clip(shape).background(AppColors.SurfaceHigh.copy(alpha = alpha)))
}

// ---------- Hộp thoại ----------

@Composable
fun AppDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String = "Hủy",
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = AppColors.SurfaceHigh,
        title = { Text(title, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary) },
        text = { Column(content = content) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Primary,
                    contentColor = Color.White
                )
            ) { Text(confirmText, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = { AppTextAction(dismissText, onDismiss) }
    )
}
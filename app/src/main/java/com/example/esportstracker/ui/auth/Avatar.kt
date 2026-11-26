package com.example.esportstracker.ui.components

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.esportstracker.data.auth.AvatarStore
import com.example.esportstracker.data.auth.SessionManager
import com.example.esportstracker.data.local.AppDatabase
import com.example.esportstracker.ui.theme.AppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.io.File

/** Hàm mở màn Hồ sơ, được cung cấp từ thanh điều hướng chính. */
val LocalOpenProfile = compositionLocalOf<() -> Unit> { {} }

data class AvatarUi(
    val initial: String = "?",
    val file: File? = null,
    val version: Long = 0L
)

class AvatarViewModel(app: Application) : AndroidViewModel(app) {
    private val db = AppDatabase.get(app)
    private val userId = SessionManager(app).userId.filterNotNull()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<AvatarUi> = userId
        .flatMapLatest { uid ->
            combine(db.userDao().observeById(uid), AvatarStore.version) { user, version ->
                AvatarUi(
                    initial = user?.displayName?.trim()?.take(1)?.uppercase().orEmpty().ifEmpty { "?" },
                    file = AvatarStore.fileIfExists(app, uid),
                    version = version
                )
            }.flowOn(Dispatchers.IO)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AvatarUi())
}

@Composable
fun UserAvatar(
    ui: AvatarUi,
    size: Dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val borderWidth = if (size >= 80.dp) 2.dp else 1.5.dp
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(AppColors.PrimaryTint)
            .border(borderWidth, AppColors.Primary, CircleShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (ui.file != null) {
            AsyncImage(
                model = ui.file,
                contentDescription = "Ảnh đại diện",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(borderWidth)
                    .clip(CircleShape)
            )
        } else {
            Text(
                ui.initial,
                fontSize = (size.value * 0.42f).sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.Primary
            )
        }
    }
}

/** Ô tròn avatar ở góc trên bên phải; bấm để mở Hồ sơ. */
@Composable
fun HeaderAvatar(vm: AvatarViewModel = viewModel()) {
    val ui by vm.state.collectAsState()
    UserAvatar(ui, size = 40.dp, onClick = LocalOpenProfile.current)
}
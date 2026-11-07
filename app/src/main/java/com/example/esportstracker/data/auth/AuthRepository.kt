package com.example.esportstracker.data.auth

sealed interface AuthResult {
    data class Success(val user: UserEntity) : AuthResult
    data class Failure(val message: String) : AuthResult
}

class AuthRepository(
    private val userDao: UserDao,
    private val session: SessionManager
) {
    val currentUserId = session.userId

    suspend fun register(username: String, password: String, displayName: String): AuthResult {
        val u = username.trim().lowercase()
        if (u.length < 3) return AuthResult.Failure("Tên đăng nhập tối thiểu 3 ký tự")
        if (password.length < 8) return AuthResult.Failure("Mật khẩu tối thiểu 8 ký tự")
        if (displayName.isBlank()) return AuthResult.Failure("Vui lòng nhập tên hiển thị")
        if (userDao.findByUsername(u) != null) {
            return AuthResult.Failure("Tên đăng nhập đã tồn tại")
        }

        val salt = PasswordUtil.newSalt()
        val user = UserEntity(
            username = u,
            passwordHash = PasswordUtil.hash(password, salt),
            salt = salt,
            displayName = displayName.trim()
        )
        val id = userDao.insert(user)
        session.save(id)
        return AuthResult.Success(user.copy(id = id))
    }

    suspend fun login(username: String, password: String): AuthResult {
        val wrong = AuthResult.Failure("Tên đăng nhập hoặc mật khẩu không đúng.")
        val user = userDao.findByUsername(username.trim().lowercase()) ?: return wrong
        if (PasswordUtil.hash(password, user.salt) != user.passwordHash) return wrong
        session.save(user.id)
        return AuthResult.Success(user)
    }

    suspend fun logout() = session.clear()

    suspend fun getUser(id: Long): UserEntity? = userDao.findById(id)
}
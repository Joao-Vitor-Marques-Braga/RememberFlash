package com.rememberflash.app.data.mapper

import com.rememberflash.app.data.local.database.entity.UserEntity
import com.rememberflash.app.domain.model.User

fun UserEntity.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    cpf = cpf,
    createdAt = createdAt
)

fun User.toEntity(passwordHash: String? = null): UserEntity = UserEntity(
    id = id,
    name = name,
    email = email,
    cpf = cpf,
    passwordHash = passwordHash,
    createdAt = createdAt
)

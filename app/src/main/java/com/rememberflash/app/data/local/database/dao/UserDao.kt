package com.rememberflash.app.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rememberflash.app.data.local.database.entity.UserEntity

@Dao
interface UserDao {

    @Query("SELECT COUNT(*) FROM users WHERE cpf = :cpf")
    suspend fun countByCpf(cpf: String): Int

    @Query("SELECT * FROM users WHERE cpf = :cpf LIMIT 1")
    suspend fun findByCpf(cpf: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("UPDATE users SET password_hash = :passwordHash WHERE email = :email")
    suspend fun updatePassword(email: String, passwordHash: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM users")
    suspend fun getAll(): List<UserEntity>
}

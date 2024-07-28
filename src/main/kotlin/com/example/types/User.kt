package com.example.types

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.DataClasses.*
import com.example.PostgresDb
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.util.*
import kotlinx.datetime.*
import kotlinx.serialization.Serializable
import java.sql.ResultSet
import java.time.ZoneOffset


class User private constructor(
    val id:Int,
    var name:String,
    var password:String,
    val login:String,
    val createdAt: Instant,
    var lastLogin: Instant,
    val email: String,
    val groups: MutableList<Int>,
    //val imageId:Int?,
) : Principal {
    var isOnline = GlobalInfo.users[this.id] != null
    fun serializeShort(): UserInfoShort {
        return UserInfoShort(
            this.id,
            this.name,
            isOnline,
            this.lastLogin,
        )
    }
    fun serializeFull(): UserInfoFull {
        return UserInfoFull(
            this.id,
            this.name,
            this.createdAt,
            this.lastLogin,
            this.email,
            this.groups
        )
    }
    fun getSettings(): UserSettingsGet {
        return UserSettingsGet(
            this.name,
        )
    }
    fun changeName(newName:String){
        PostgresDb.transactionNoReturn(
            """
                UPDATE users
                SET name = '$newName'
                WHERE users.id = '$id'
            """.trimIndent()
        )
        this.name = newName
    }
    fun updateLoginDate(){
        val currentDate = Clock.System.now()
        PostgresDb.transactionNoReturn(
            """
                UPDATE users
                SET loginedat = '$currentDate'
                WHERE users.id = '$id'
            """.trimIndent()
        )
        lastLogin = currentDate
    }
    fun unblockUser(id:Int){
        val user = User(id)
        PostgresDb.transactionNoReturn(
            """
                DELETE FROM usersblocked 
                WHERE 
	                usersblocked.userid = '${this.id}'
	                AND
	                usersblocked.blockedid = '${user.id}'
            """.trimIndent()
        )
    }
    fun blockUser(id:Int){
        val user = User(id)
        PostgresDb.transactionNoReturn(
            """
                INSERT INTO usersblocked (userid,blockedid)
                VALUES ('${this.id}','${user.id}')
                ON CONFLICT DO NOTHING
            """.trimIndent()
        )
    }
    fun changePassword(passwordChange: PasswordChange){
        if (Auth.verifyPassword(password,passwordChange.oldPassword)){
            passwordChange.newPassword = Auth.bcryptPassword(passwordChange.newPassword)
            password = passwordChange.newPassword
        } else {
            throw  BadRequestException("Password mismatch")
        }
        PostgresDb.transactionNoReturn(
            """
                UPDATE users
                SET password = '${passwordChange.newPassword}'
                WHERE users.id = '$id'
            """.trimIndent()
        )
    }

    companion object {
        operator fun invoke(rs:ResultSet): User {
            var groups:MutableList<Int?>? = (rs.getArray("groups").array as Array<Int>).toMutableList()
            if (groups != null && groups[0] == null){
                groups = mutableListOf()
            }
            //val imageId = if(rs.getObject("image") != null) rs.getInt("image") else  null
             return User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("password"),
                rs.getString("login"),
                rs.getTimestamp("createdAt").toInstant().toKotlinInstant(),
                rs.getTimestamp("loginedat").toInstant().toKotlinInstant(),
                rs.getString("email"),
                groups as MutableList<Int>,
            )
        }
        operator fun invoke(id:Int): User {
            val user = PostgresDb.getUser(
                """
                    SELECT 
	                    users.*,
	                    ARRAY_AGG(DISTINCT members.groupid) AS GROUPS
                    FROM users
                    LEFT JOIN members
                    ON members.userid = '$id'
                    WHERE 
                        users.id = '$id'
                    GROUP BY users.id
                    LIMIT 1
                """.trimMargin())
            return user?:throw NotFoundException("User not found");
        }
        //creation via login can be used ONLY via jwt authorization(i hope)
        operator fun invoke(login:String): User{
            val user = PostgresDb.getUser(
                """
                    SELECT 
	                    users.*,
	                    ARRAY_AGG(DISTINCT members.groupid) AS GROUPS
                    FROM users
                    LEFT JOIN members
                    ON members.userid = users.id
                    WHERE 
                        users.login = '$login'
                    GROUP BY users.id
                    LIMIT 1
                """.trimIndent())
            if(user != null) {
                user.updateLoginDate()
                return user
            }
            else {
                throw NotFoundException("User not found");
            }
        }
        operator fun invoke(login:String,email: String): User{
            val user = PostgresDb.getUser(
                """
                    SELECT 
	                    users.*,
	                    ARRAY_AGG(DISTINCT members.groupid) AS GROUPS
                    FROM users
                    LEFT JOIN members
                    ON members.userid = users.id
                    WHERE 
                        users.login = '$login'
                        OR
                        users.email = '$email'
                    GROUP BY users.id
                    LIMIT 1
                """.trimIndent())
            if(user != null) {
                user.updateLoginDate()
                return user
            }
            else {
                throw NotFoundException("User not found");
            }
        }
        operator fun invoke(userRegister: UserRegister): User {
            val existingUser = try { User(userRegister.login,userRegister.email)} catch (e:NotFoundException){ null }
            if (existingUser != null){
                throw BadRequestException("User already exists")
            }
            val currentTime = Clock.System.now()
            userRegister.password = Auth.bcryptPassword(userRegister.password)
            val user = PostgresDb.getUser(
                """
                    WITH newUser AS (
                    INSERT INTO users (name,password,login,createdat,loginedat,email) 
                    VALUES ('${userRegister.name}', '${userRegister.password}', '${userRegister.login}','${currentTime}','${currentTime}','${userRegister.email}')
                    RETURNING *
                    )
                    SELECT 
                        newUser.*,
                        ARRAY_AGG(DISTINCT members.groupid) AS GROUPS
                    FROM newUser
                    LEFT JOIN members
                    ON members.userid = newUser.id
                    GROUP BY newUser.id,newUser.name,newUser.PASSWORD,newUser.login,newUser.createdat,newUser.loginedat,newUser.email
                """.trimIndent()
            )
            return user!!
        }
        operator fun invoke(loginUser: UserLogin):User {
            val user = User(loginUser.login)
            if (Auth.verifyPassword(user.password,loginUser.password)){
                user.updateLoginDate()
                return user
            } else {
                throw NotFoundException("User not found")
            }
        }
    }
}
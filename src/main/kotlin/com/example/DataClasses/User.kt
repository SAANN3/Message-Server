package com.example.DataClasses

import com.example.types.User
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class UserInfoShort(
    val id:Int,
    val name:String,
    val online: Boolean,
    val lastLogin: Instant,
    val createdAt: Instant
)
@Serializable
data class UserInfoFull(
    val id:Int,
    var name:String,
    val createdAt: Instant,
    var lastLogin: Instant,
    val groups: MutableList<Int>,
    val friends: MutableList<Int>,
    val friendsInvites: MutableList<Int>,
    val myInvites: MutableList<Int>
)
@Serializable
data class UserRegister(
    val name: String,
    var password: String,
    val login: String,
)
@Serializable
data class UserLogin(
    val password: String,
    val login: String,
)
@Serializable
data class PasswordChange(
    val oldPassword: String,
    var newPassword: String
)
@Serializable
data class UserSettingsPost(
    val password: PasswordChange?,
    val name: String?,
    val nonFriendsGroupsInvites: Boolean?
    //val image: Int,
)
@Serializable
data class UserSettingsGet(
    val name: String,
    val settings: User.UserSettings
)
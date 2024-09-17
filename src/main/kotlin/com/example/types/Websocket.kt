package com.example.types


import WebSocketRequestType
import WebSocketResponses
import WebsocketRequests
import WebsocketResponse
import WsRequest
import WsResponse
import com.example.DataClasses.UserInfoShort
import com.example.types.User
import io.ktor.server.plugins.*
import io.ktor.server.websocket.*
import io.ktor.util.*
import io.ktor.websocket.*
import io.ktor.websocket.serialization.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.reflect.full.findAnnotation


object GlobalInfo {
    val users: HashMap<Int, UserWebsocket> = HashMap<Int, UserWebsocket>()
    val groups: HashMap<Int, Group> = HashMap<Int,Group>()
    val Json = Json{
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
}

class UserWebsocket(
    val session: WebSocketServerSession,
    val user:User,
) {
    init {
        user.isOnline = true
        user.groups.let { it ->
            it.forEach {
                if(GlobalInfo.groups[it] == null){
                    GlobalInfo.groups[it] = Group(it);
                }
                runBlocking {
                    GlobalInfo.groups[it]?.changeOnline(user,true)
                }
            }
        }
    }
    private fun accessGroup(id:Int):Group{
        if(!user.groups.contains(id)){
            throw BadRequestException("You are not in group")
        }
        return GlobalInfo.groups[id]!!
    }
    suspend fun readMessage(text:String){
        val typeName:String = try {
            GlobalInfo.Json.decodeFromString<WebSocketRequestType>(text).type
        } catch(e:SerializationException){
            sendErr(WebSocketResponses.BadRequest(),"Cant get [type] parameter")
            return
        }
        try{
            when(typeName) {
                WebsocketRequests.getAnnotation(WebsocketRequests.BlockUser::class) -> {
                    val type = GlobalInfo.Json.decodeFromString<WebsocketRequests.BlockUser>(text)
                    user.blockUser(type.userId)
                    sendOk(WebSocketResponses.BlockedUser(type.userId))
                }
                WebsocketRequests.getAnnotation(WebsocketRequests.GetGroupsInfo::class) -> {
                    val type = GlobalInfo.Json.decodeFromString<WebsocketRequests.GetGroupsInfo>(text)
                    val groups: MutableList<WebSocketResponses.GroupInfo> = mutableListOf()
                    type.groupsId.forEach {
                        groups += accessGroup(it).getInfo(this.user)
                    }
                    sendOk(WebSocketResponses.GroupsInfo(groups))
                }
                WebsocketRequests.getAnnotation(WebsocketRequests.SendMessage::class) -> {
                    val type = GlobalInfo.Json.decodeFromString<WebsocketRequests.SendMessage>(text)
                    val message = accessGroup(type.groupId).sendMessage(user,type.message)
                    sendOk(WebSocketResponses.MessageSent(message.id))
                }
                WebsocketRequests.getAnnotation(WebsocketRequests.UnblockUser::class) -> {
                    val type = GlobalInfo.Json.decodeFromString<WebsocketRequests.UnblockUser>(text)
                    user.unblockUser(type.userId)
                    sendOk(WebSocketResponses.UnblockedUser(type.userId))
                }
                WebsocketRequests.getAnnotation(WebsocketRequests.CreateGroup::class) -> {
                    val type = GlobalInfo.Json.decodeFromString<WebsocketRequests.CreateGroup>(text)
                    val group = Group(user,type.name)
                    GlobalInfo.groups[group.id] = group
                    type.users?.forEach {
                        group.inviteUser(user, User(it))
                    }
                    sendOk(WebSocketResponses.CreateGroup(group.id))
                }
                WebsocketRequests.getAnnotation(WebsocketRequests.GetLastReadMessages::class) -> {
                    val type = GlobalInfo.Json.decodeFromString<WebsocketRequests.GetLastReadMessages>(text)
                    val unreadMessages:HashMap<Int,Int?> = hashMapOf()
                    val unreadAmount: HashMap<Int,Int> = hashMapOf()
                    user.groups.forEach {
                        unreadMessages[it] = accessGroup(it).getLastReadMessage(user)?.id
                        unreadAmount[it] = accessGroup(it).getUnreadAmount(user)
                    }
                    sendOk(WebSocketResponses.ResAllUnreadMessages(unreadMessages,unreadAmount))
                }
                WebsocketRequests.getAnnotation(WebsocketRequests.InviteUser::class) -> {
                    val type = GlobalInfo.Json.decodeFromString<WebsocketRequests.InviteUser>(text)
                    accessGroup(type.groupId).inviteUser(user,User(type.userId))
                }
                WebsocketRequests.getAnnotation(WebsocketRequests.LeaveChat::class) -> {
                    val type = GlobalInfo.Json.decodeFromString<WebsocketRequests.LeaveChat>(text)
                    accessGroup(type.groupId).kickUser(user,user.id)
                    sendOk(WebSocketResponses.LeaveGroup(type.groupId))
                }
                WebsocketRequests.getAnnotation(WebsocketRequests.KickUser::class) -> {
                    val type = GlobalInfo.Json.decodeFromString<WebsocketRequests.KickUser>(text)
                    accessGroup(type.groupId).kickUser(user,type.userId)
                    sendOk(WebSocketResponses.UserKicked(user.id,type.userId,type.groupId))
                }
                WebsocketRequests.getAnnotation(WebsocketRequests.LoadMessages::class) -> {
                    val type = GlobalInfo.Json.decodeFromString<WebsocketRequests.LoadMessages>(text)
                    val messages = accessGroup(type.groupId).getLastMessages(30,type.offset)
                    sendOk(WebSocketResponses.LoadMessages(
                        Message.castToResponseMessages(messages),
                        type.groupId
                    ))
                }
                WebsocketRequests.getAnnotation(WebsocketRequests.LoadMessagesAfter::class) -> {
                    val type = GlobalInfo.Json.decodeFromString<WebsocketRequests.LoadMessagesAfter>(text)
                    val messages = accessGroup(type.groupId).getMessagesAfter(type.messageId,30)
                    sendOk(WebSocketResponses.LoadMessages(
                        Message.castToResponseMessages(messages),
                        type.groupId
                    ))
                }
                WebsocketRequests.getAnnotation(WebsocketRequests.DeleteGroup::class) -> {
                    val type = GlobalInfo.Json.decodeFromString<WebsocketRequests.DeleteGroup>(text)
                    val group = accessGroup(type.groupId)
                    group.deleteGroup()
                    GlobalInfo.groups.remove(group.id)
                }
                WebsocketRequests.getAnnotation(WebsocketRequests.ChangeGroupName::class) -> {
                    val type = GlobalInfo.Json.decodeFromString<WebsocketRequests.ChangeGroupName>(text)
                    accessGroup(type.groupId).changeName(user,type.name)
                }
                WebsocketRequests.getAnnotation(WebsocketRequests.RemoveMessage::class) -> {
                    val type = GlobalInfo.Json.decodeFromString<WebsocketRequests.RemoveMessage>(text)
                    accessGroup(type.groupId).removeMessage(user,type.messageId)
                }
                WebsocketRequests.getAnnotation(WebsocketRequests.EditMessage::class) -> {
                    val type = GlobalInfo.Json.decodeFromString<WebsocketRequests.EditMessage>(text)
                    accessGroup(type.groupId).editMessage(user,type.messageId,type.text)
                }
                WebsocketRequests.getAnnotation(WebsocketRequests.ReadMessage::class) -> {
                    val type = GlobalInfo.Json.decodeFromString<WebsocketRequests.ReadMessage>(text)
                    accessGroup(type.groupId).readMessages(user,type.messageId)
                }
                WebsocketRequests.getAnnotation(WebsocketRequests.GetUsersInfo::class) -> {
                    val type = GlobalInfo.Json.decodeFromString<WebsocketRequests.GetUsersInfo>(text)
                    val usersInfo:HashMap<Int,UserInfoShort> = hashMapOf()
                    type.usersId.forEach {
                        usersInfo[it] = User(it).serializeShort()
                    }
                    sendOk(WebSocketResponses.UsersInfo(usersInfo))
                }
                WebsocketRequests.getAnnotation(WebsocketRequests.AddFriend::class) -> {
                    val type = GlobalInfo.Json.decodeFromString<WebsocketRequests.AddFriend>(text)
                    val friend = User(type.userId)
                    user.friendsClass.addFriend(friend)
                    val change = WebSocketResponses.FriendChanged(type.userId,FriendStatus.SENT,user.id)
                    sendOk(change)
                    GlobalInfo.users[type.userId]?.friendChanged(change)
                }
                WebsocketRequests.getAnnotation(WebsocketRequests.RemoveMyFriendInvite::class) -> {
                    val type = GlobalInfo.Json.decodeFromString<WebsocketRequests.RemoveMyFriendInvite>(text)
                    user.friendsClass.removeInvite(User(type.userId))
                    val change = WebSocketResponses.FriendChanged(type.userId,FriendStatus.REVOKED,user.id)
                    sendOk(change)
                    GlobalInfo.users[type.userId]?.friendChanged(change)
                }
                WebsocketRequests.getAnnotation(WebsocketRequests.RemoveFriend::class) -> {
                    val type = GlobalInfo.Json.decodeFromString<WebsocketRequests.RemoveFriend>(text)
                    val friend = User(type.userId)
                    user.friendsClass.removeFriend(friend)
                    val change = WebSocketResponses.FriendChanged(type.userId,FriendStatus.DELETED,user.id)
                    sendOk(change)
                    GlobalInfo.users[type.userId]?.friendChanged(change)
                }
                WebsocketRequests.getAnnotation(WebsocketRequests.AcceptFriendRequest::class) -> {
                    val type = GlobalInfo.Json.decodeFromString<WebsocketRequests.AcceptFriendRequest>(text)
                    val friend = User(type.userId)
                    user.friendsClass.acceptFriend(friend)
                    val change = WebSocketResponses.FriendChanged(type.userId,FriendStatus.ACCEPTED,user.id)
                    sendOk(change)
                    GlobalInfo.users[type.userId]?.friendChanged(change)
                }
                WebsocketRequests.getAnnotation(WebsocketRequests.DeclineFriendInvite::class) -> {
                    val type = GlobalInfo.Json.decodeFromString<WebsocketRequests.DeclineFriendInvite>(text)
                    val friend = User(type.userId)
                    user.friendsClass.declineInvite(friend)
                    val change = WebSocketResponses.FriendChanged(type.userId,FriendStatus.DECLINED,user.id)
                    sendOk(change)
                    GlobalInfo.users[type.userId]?.friendChanged(change)
                }
                else -> {
                    sendErr(WebSocketResponses.BadRequest(),"Type [$typeName] not recognized")
                }
            }
        } catch (e:SerializationException) {
            sendErr(WebSocketResponses.BadRequest(),"Failed parse json to [$typeName] message ")
        } catch (e: Exception) {
            sendErr(WebSocketResponses.BadRequest(),e.message)
        }
    }
    suspend fun friendChanged(change:WebSocketResponses.FriendChanged){
        sendOk(change)
    }
    suspend fun newMessage(message:Message){
        sendOk(message.serialize())
    }
    suspend fun messageRemoved(message: Message){
        sendOk(WebSocketResponses.MessageDeleted(message.serialize()))
    }
    suspend fun groupNameChanged(group: Group){
        sendOk(WebSocketResponses.GroupNameChanged(group.name,group.id))
    }
    suspend fun messageEdited(message: Message){
        sendOk(WebSocketResponses.MessageEdited(message.serialize()))
    }
    suspend fun userJoined(senderId:User,userId: Int,group: Group){
        sendOk(WebSocketResponses.UserJoined(senderId.id,userId,group.id))
    }
    suspend fun userLeft(senderId: Int,kickedId:Int,group: Group){
        sendOk(WebSocketResponses.UserKicked(senderId,kickedId,group.id))
    }
    suspend fun userOnlineChanged(user:User,state:Boolean){
        sendOk(WebSocketResponses.UserOnlineChanged(user.id,state))
    }
    suspend fun groupDeleted(group: Group){
        user.groups -= group.id
        sendOk(WebSocketResponses.GroupDeleted(group.id))
    }
    suspend fun messagesRead(messageId: Int,group: Group,userid:Int) {
        sendOk(WebSocketResponses.MessagesRead(messageId,group.id,userid))
    }
    private suspend inline fun <reified T: Any> sendOk(data:T){
        _send(data,WebSocketResponses.getAnnotation(data::class),"OK",null)
    }
    private suspend fun <T : Any> sendErr(data:T,error: String){
        _send(WebSocketResponses.EmptyResponse(),WebSocketResponses.getAnnotation(data::class),"ERR",error)
    }
    private suspend inline fun <reified T:Any>_send(data:T, type:String, status:String, error:String?) {
        session.sendSerialized(
            WebsocketResponse<T>(
                status = status,
                type = type,
                data = data,
                errorMessage = error
            )
        )

    }

}
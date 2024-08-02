import com.example.DataClasses.UserInfoShort
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation


annotation class WsResponse(val type: String)

@Serializable
data class WebsocketResponse<T:Any>(
    val status: String,
    val type: String,
    val errorMessage: String?,
    val data:T
)
class WebSocketResponses(){
    companion object {
        fun <T : Any>getAnnotation(data: KClass<T>):String{
            return data.findAnnotation<WsResponse>()!!.type
        }
    }
    @Serializable
    class EmptyResponse()
    @Serializable
    @WsResponse("NEW_MESSAGE")
    data class Message(
        val id:Int,
        val sender: Int,
        val groupId: Int,
        val text: String,
        val date: Instant,
        val edited: Boolean,
        val readBy: MutableList<Int>
    )
    @Serializable
    data class UnreadMessages(
        val unreadAmount: Int,
    )
    @Serializable
    @WsResponse("ALL_UNREAD_MESSAGES")
    data class ResAllUnreadMessages(
        val messages: HashMap<Int,Int?>
    )
    @Serializable
    @WsResponse("USER_BLOCKED")
    data class BlockedUser(
        val blockedId: Int
    )
    @Serializable
    @WsResponse("USER_KICKED")
    data class UserKicked(
        val senderId:Int,
        val groupId: Int,
        val kickedId:Int
    )
    @Serializable
    @WsResponse("USER_JOINED")
    data class UserJoined(
        val groupId: Int,
        val senderId:Int,
        val userId: Int
    )
    @Serializable
    @WsResponse("LEFT_GROUP")
    data class LeaveGroup(
        val groupId: Int
    )
    @Serializable
    @WsResponse("CREATED_GROUP")
    data class CreateGroup(
        val groupId: Int
    )
    @Serializable
    @WsResponse("MESSAGE_SENT")
    data class MessageSent(
        val messageId: Int
    )
    @Serializable
    @WsResponse("USER_UNBLOCKED")
    data class UnblockedUser(
        val blockedId: Int
    )
    @Serializable
    @WsResponse("GROUP_INFO")
    data class GroupInfo(
        val users: List<Int>,
        val name: String,
        val creationDate: Instant,
    )
    @Serializable
    @WsResponse("LOADED_MESSAGES")
    data class LoadMessages(
        val messages: MutableList<Message>,
        val groupId: Int
    )
    @Serializable
    @WsResponse("BAD_REQUEST")
    class BadRequest()
    @Serializable
    @WsResponse("GROUP_NAME_CHANGED")
    data class  GroupNameChanged(
        val name:String,
        val groupId: Int
    )
    @Serializable
    @WsResponse("MESSAGE_DELETED")
    data class MessageDeleted(
        val message: Message
    )
    @Serializable
    @WsResponse("MESSAGE_EDITED")
    data class MessageEdited(
        val message: Message
    )
    @Serializable
    @WsResponse("USER_ONLINE_CHANGED")
    data class UserOnlineChanged(
        val userId: Int,
        val state: Boolean
    )
    @Serializable
    @WsResponse("GROUP_DELETED")
    data class GroupDeleted(
        val groupId: Int
    )
    @Serializable
    @WsResponse("MESSAGES_READ")
    data class MessagesRead(
        val lastMessageId: Int,
        val groupId:Int,
        val userId: Int
    )
}

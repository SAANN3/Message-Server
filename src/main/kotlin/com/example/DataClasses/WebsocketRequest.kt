import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation


annotation class WsRequest(val type:String)

@Serializable
data class WebsocketRequest<T:WsRequest>(
    val type: String,
    val data:T
)
class WebsocketRequests(){
    companion object {
        fun <T : Any>getAnnotation(data: KClass<T>):String{
            return data.findAnnotation<WsRequest>()!!.type
        }
    }
    @Serializable
    @WsRequest("SEND_MESSAGE")
    data class SendMessage(
        val groupId: Int,
        val message: String,
    )
    @Serializable
    @WsRequest("CREATE_GROUP")
    data class CreateGroup(
        val users:MutableList<Int>?,
        val name:String,
    )
    @Serializable
    @WsRequest("LEAVE_GROUP")
    data class LeaveChat(
        val groupId: Int
    )
    @Serializable
    @WsRequest("LOAD_MESSAGES")
    data class LoadMessages(
        val groupId: Int,
        val offset: Int,
    )
    @Serializable
    @WsRequest("GET_UNREAD_MESSAGES")
    class GetUnreadMessages()

    @Serializable
    @WsRequest("GET_GROUP_INFO")
    data class GetGroupInfo(
        val groupId:Int,
        val offset: Int,
    )
    @Serializable
    @WsRequest("BLOCK_USER")
    data class BlockUser(
        val userId:Int
    )
    @Serializable
    @WsRequest("UNBLOCK_USER")
    data class UnblockUser(
        val userId:Int
    )
    @Serializable
    @WsRequest("KICK_USER")
    data class KickUser(
        val userId: Int,
        val groupId: Int,
    )
    @Serializable
    @WsRequest("DELETE_GROUP")
    data class DeleteGroup(
        val groupId: Int
    )
    @Serializable
    @WsRequest("CHANGE_GROUP_NAME")
    data class ChangeGroupName(
        val groupId: Int,
        val name: String
    )
    @Serializable
    @WsRequest("REMOVE_MESSAGE")
    data class RemoveMessage(
        val messageId:Int,
        val groupId: Int
    )
    @Serializable
    @WsRequest("EDIT_MESSAGE")
    data class EditMessage(
        val messageId: Int,
        val text: String,
        val groupId: Int
    )
}
@Serializable
data class WebSocketRequestType(
    val type:String
)

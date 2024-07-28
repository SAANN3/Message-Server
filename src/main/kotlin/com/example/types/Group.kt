package com.example.types

import WebsocketResponse
import WsResponse
import com.example.PostgresDb
import io.ktor.server.plugins.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import org.postgresql.util.PSQLException
import java.sql.ResultSet

class Group private  constructor(
    val id: Int,
    var name: String,
    val members: MutableList<Int>,
    val creationDate: Instant,
){
    fun getLastMessages(amount:Int,offset:Int):MutableList<Message> {
        val messages = PostgresDb.getMessages(
            """
                SELECT * FROM MESSAGES
                ORDER BY id DESC
                LIMIT $amount
                OFFSET $offset
            """.trimIndent()
        )
        return messages
    }
    fun howMuchOnline(): Int {
        var n = 0
        members.forEach {
            if(GlobalInfo.users[it] != null){
                n++
            }
        }
        return n
    }
    suspend fun sendMessage(sender:User, text:String): Message{
        val message = Message(this,sender,text)
        forEachMember {
            it.newMessage(message)
        }
        return message
    }
    suspend fun removeMessage(sender: User, messageId:Int){
        val message = Message(messageId,this)
        if(message.sender != sender.id){
            throw BadRequestException("You cant remove not yours message")
        }
        message.delete()
        members.forEach {
            GlobalInfo.users[it]?.messageRemoved(message)
        }
    }
    suspend fun editMessage(sender: User,messageId: Int,text: String){
        val message = Message(messageId,this)
        if(message.sender != sender.id){
            throw BadRequestException("You cant edit not yours message")
        }
        message.edit(text)
        forEachMember {
            it.messageEdited(message)
        }
    }
    suspend fun inviteUser(sender:User,invitedID:Int){
        if(isInGroup(invitedID)){
            throw BadRequestException("User already in group")
        }
        PostgresDb.transactionNoReturn(
            """
                INSERT INTO members (userid,groupid)
                VALUES (${invitedID},${this.id})
            """.trimIndent()
        )
        GlobalInfo.users[invitedID]?.user?.groups?.plusAssign(invitedID)
        members += invitedID
        forEachMember {
            it.userJoined(sender,invitedID,this)
        }
    }
    suspend fun kickUser(sender: User, kickedID:Int){
        if(!isInGroup(kickedID)){
            throw BadRequestException("User not in group")
        }
        PostgresDb.transactionNoReturn(
            """
                DELETE FROM members
                WHERE
                    members.userid = ${kickedID}
                    AND
                    members.groupid = ${this.id}
            """.trimIndent()
        )
        GlobalInfo.users[kickedID]?.user?.groups?.remove(kickedID)
        members -= kickedID
        forEachMember {
            it.userLeft(sender.id,kickedID,this)
        }
    }
    suspend fun changeOnline(user:User, state:Boolean){
        forEachMember {
            it.userOnlineChanged(user,state)
        }
    }
    private fun isInGroup(userID:Int):Boolean{
        val exists = members.contains(userID) ?: false
        return exists
    }
    suspend fun changeName(user: User, newName:String) {
        """
            UPDATE GROUPS
            SET (name) = ('$newName')
            WHERE groups.id = $id
        """.trimIndent()
        name = newName
        forEachMember {
            it.groupNameChanged(this)
        }
    }
    suspend fun deleteGroup(){
        """
            DELETE FROM groups
            WHERE groups.id = $id
        """.trimIndent()
        forEachMember {
            it.groupDeleted(this)
        }
        GlobalInfo.groups.remove(this.id)
    }
    private suspend fun forEachMember(func:suspend (it:UserWebsocket) -> Unit){
        members.forEach {
            GlobalInfo.users[it]?.let {
                func(it)
            }
        }
    }
   companion object {
       operator fun invoke(rs:ResultSet):Group {
           var members:MutableList<Int?>? = try {
               (rs.getArray("members").array as Array<Int>).toMutableList()
           } catch (e:PSQLException) {
               null
           }
           if (members == null || members[0] == null){
               members = mutableListOf()
           }
           return Group(
               rs.getInt("id"),
               rs.getString("name"),
               members as MutableList<Int>,
               rs.getTimestamp("createdat").toInstant().toKotlinInstant()
           )
       }
       operator fun invoke(id: Int):Group{
            val group = PostgresDb.getGroup(
                """
                    SELECT 
	                    groups.*,
	                    ARRAY_AGG(DISTINCT members.userid) AS members
                    FROM groups 
                    LEFT JOIN members
                    ON members.groupid = ${id}
                       WHERE groups.id = ${id}
                    GROUP BY groups.id
                """.trimIndent()
            )
           return group?:throw NotFoundException("Group not found")
       }
       operator fun invoke(creator:User,name:String):Group{
            val group = PostgresDb.getGroup(
                """
                    INSERT INTO groups (name,createdat)
                    VALUES ('$name','${Clock.System.now()}')
                    RETURNING *
                """.trimIndent()
            )!!
           runBlocking {
               group.inviteUser(creator,creator.id)
           }
           return group
       }
   }
}
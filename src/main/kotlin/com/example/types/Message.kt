package com.example.types

import WebSocketResponses
import WebsocketResponse
import com.example.PostgresDb
import io.ktor.server.plugins.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import java.sql.ResultSet

class Message private constructor(
    val id:Int,
    val groupId: Int,
    val sender: Int,
    var createdAt: Instant,
    var edited: Boolean,
    var text: String,
) {

    fun edit(text:String) {
       edited = true
       this.text = text
       PostgresDb.transactionNoReturn(
           """
               UPDATE messages
               SET (text,edited) = ('$text',$edited)
               WHERE messages.id = ${id}
           """.trimIndent()
       )
    }
    fun delete() {
        PostgresDb.transactionNoReturn(
            """
                DELETE FROM messages
                WHERE messages.id = $id
            """.trimIndent()
        )
    }
    fun serialize(): WebSocketResponses.Message {
        return WebSocketResponses.Message(
            id,
            sender,
            groupId,
            text,
            createdAt,
            edited
        )
    }
    companion object {
        operator fun invoke(rs:ResultSet):Message {
            return Message(
                rs.getInt("id"),
                rs.getInt("groupid"),
                rs.getInt("sender"),
                rs.getTimestamp("createdat").toInstant().toKotlinInstant(),
                rs.getBoolean("edited"),
                rs.getString("text"),
            )
        }
        operator fun invoke(group:Group,sender:User,text: String):Message {
            if(text.isEmpty()){
                throw BadRequestException("text of message is empty")
            }
            val message = PostgresDb.getMessage(
                """
                    INSERT INTO messages (groupid,sender,createdat,edited,text)
                    VALUES ('${group.id}','${sender.id}','${Clock.System.now()}','${false}','$text')
                    RETURNING *
                """.trimIndent()
            )
            return message!!
        }
        operator fun invoke(id: Int,group: Group):Message {
            val message = PostgresDb.getMessage(
                """
                    SELECT * FROM messages
                    WHERE 
                        messages.id = '$id'
                        AND
                        messages.groupid = '${group.id}'
                    LIMIT 1
                """.trimIndent())
            return message?:throw NotFoundException("Message not found")
        }
        fun castToResponseMessages(message:MutableList<Message>):MutableList<WebSocketResponses.Message> {
            val res: MutableList<WebSocketResponses.Message> = mutableListOf()
            message.forEach {
                res += WebSocketResponses.Message(
                    it.id,
                    it.sender,
                    it.groupId,
                    it.text,
                    it.createdAt,
                    it.edited
                )
            }
            return res
        }
    }
}
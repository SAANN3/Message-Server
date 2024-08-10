package com.example

import com.example.DataClasses.UserRegister
import com.example.types.Group
import com.example.types.Message
import com.example.types.User
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync
import org.jetbrains.exposed.sql.transactions.transaction
import mu.KotlinLogging
import org.jetbrains.exposed.sql.statements.StatementType
import java.sql.ResultSet

fun Application.configureDatabase(){
     Database.connect(
        url = environment.config.property("postgres.url").getString(),
         user = "postgres"
    )

}
object PostgresDb {
    private val logger = KotlinLogging.logger {  }
    fun transactionNoReturn(query: String) {
        transaction {
            TransactionManager.current().exec(query)
        }
    }
    fun isExists(query: String): Boolean {
        var res = false;
        transaction {
            TransactionManager.current().exec(
                query,
                explicitStatementType = StatementType.SELECT
            ) { rs ->
                if(rs.next()) {
                    res = true
                }
            }
        }
        return res;
    }
    private fun <T>getSomething(query:String,constructor:(rs:ResultSet)->T): T? {
        var something:T? = null;
        transaction {
            TransactionManager.current().exec(
                query,
                explicitStatementType = StatementType.SELECT
            ) { rs ->
                if(rs.next()) {
                    something = constructor(rs);
                }
            }
        }
        return something;
    }
    private fun <T>getListOfSomething(query:String,constructor:(rs:ResultSet)->T): MutableList<T> {
        val list:MutableList<T> = mutableListOf();
        transaction {
            TransactionManager.current().exec(
                query,
                explicitStatementType = StatementType.SELECT
            ) { rs ->
                while(rs.next()) {
                    list += constructor(rs);
                }
            }
        }
        return list;
    }
    fun getUser(query: String): User? {
        return getSomething<User>(query,User::invoke)
    }
    fun getGroup(query: String): Group? {
        return getSomething<Group>(query,Group::invoke)
    }
    fun getGroups(query: String): MutableList<Group> {
        return getListOfSomething<Group>(query,Group::invoke);
    }
    fun getMessage(query: String): Message? {
        return getSomething<Message>(query,Message::invoke)
    }
    fun getMessages(query: String): MutableList<Message> {
        return getListOfSomething<Message>(query,Message::invoke);
    }
    fun getIntList(query: String): MutableList<Int> {
        return getListOfSomething<Int>(query){ rs ->
            rs.getInt(1)
        }
    }
    fun getInt(query: String): Int {
        var number:Int = 0
        transaction {
            TransactionManager.current().exec(
                query,
                explicitStatementType = StatementType.SELECT
            ) { rs ->
                while(rs.next()) {
                    number = rs.getInt(1)
                }
            }
        }
        return number;
    }

}

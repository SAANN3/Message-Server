package com.example.types

import com.example.PostgresDb

class UserFriends(
    private val user: User
){
    val friends: MutableList<Int> = mutableListOf()
    val friendsInvites: MutableList<Int> = mutableListOf()
    val myInvitations: MutableList<Int> = mutableListOf()
    init {
        loadFriends()
    }
    private fun loadFriends(){
        friends += PostgresDb.getIntList(
            """
                SELECT usersfriends.friendid 
                FROM usersfriends
                WHERE 
                    usersfriends.userid = '${user.id}'
            """.trimIndent()
        ) + PostgresDb.getIntList(
            """
                SELECT usersfriends.userid 
                FROM usersfriends
                WHERE 
                    usersfriends.friendid = '${user.id}'
            """.trimIndent()
        )
        friendsInvites += PostgresDb.getIntList(
            """
                SELECT friendsinvites.userid 
                FROM friendsinvites
                WHERE 
                    friendsinvites.friendid = '${user.id}'
            """.trimIndent()
        )
        myInvitations += PostgresDb.getIntList(
            """
                SELECT friendsinvites.friendid 
                FROM friendsinvites
                WHERE 
                    friendsinvites.userid = '${user.id}'
            """.trimIndent()
        )
    }
    fun addFriend(friend:User) {
        if(friends.contains(friend.id) ||  myInvitations.contains(friend.id)){
            throw BadRequestException("You already added or sent invite to this friend")
        }
        if(friend.id == user.id){
            throw BadRequestException("You cant add yourself")
        }
        friend.isBlocked(user)
        user.isBlocked(friend)
        PostgresDb.transactionNoReturn(
            """
                INSERT INTO friendsinvites (userid,friendid)
                VALUES ('${user.id}','${friend.id}')
            """.trimIndent()
        )
        myInvitations += friend.id
        friend.friendsClass.friendsInvites += user.id
    }
    fun acceptFriend(friend:User) {
        if(!friendsInvites.contains(friend.id)) {
            throw BadRequestException("User didn't send an invitation to you")
        }
        PostgresDb.transactionNoReturn(
            """
                INSERT INTO usersfriends (userid,friendid)
                VALUES ('${user.id}','${friend.id}')
                ON CONFLICT DO NOTHING
            """.trimIndent()
        )
        PostgresDb.transactionNoReturn(
            """
                DELETE FROM friendsinvites
                WHERE
                    friendsinvites.userid = ${friend.id}
                    AND
                    friendsinvites.friendid = ${user.id}
            """.trimIndent()
        )
        friendsInvites -= friend.id
        friends += friend.id
        friend.friendsClass.friends += user.id
        friend.friendsClass.myInvitations -= user.id
    }
    fun removeFriend(friend: User) {
        if(!friends.contains(friend.id)){
            return
        }
        PostgresDb.transactionNoReturn(
            """
                DELETE FROM usersfriends 
                WHERE 
                        usersfriends.userid = '${user.id}'
                    AND
                        usersfriends.friendid = '${friend.id}'
                    OR
                        usersfriends.userid = '${friend.id}'
                    AND
                       usersfriends.friendid = '${user.id}'
                    
            """.trimIndent()
        )
        friends -= friend.id
        friend.friendsClass.friends -= user.id
    }
    fun removeInvite(friend: User){
        if(!myInvitations.contains(friend.id)){
            return
        }
        PostgresDb.transactionNoReturn(
            """
                DELETE FROM friendsinvites 
                WHERE 
                        friendsinvites.userid = '${user.id}'
                    AND
                        friendsinvites.friendid = '${friend.id}'
            """.trimIndent()
        )
        myInvitations -= friend.id
        friend.friendsClass.friendsInvites -= user.id
    }
    fun declineInvite(friend: User){
        if(!friendsInvites.contains(friend.id)){
            return
        }
        PostgresDb.transactionNoReturn(
            """
                DELETE FROM friendsinvites 
                WHERE 
                        friendsinvites.userid = '${friend.id}'
                    AND
                        friendsinvites.friendid = '${user.id}'
            """.trimIndent()
        )
        friendsInvites -= friend.id
        friend.friendsClass.myInvitations -= user.id
    }
    fun onBlock(blocked: User){
        if(friends.contains(blocked.id)){
            removeFriend(blocked)
        }
        if(myInvitations.contains(blocked.id)){
            removeInvite(blocked)
        }
        if(friendsInvites.contains(blocked.id)){
            declineInvite(blocked)
        }
    }
    fun isFriendWith(friend: User):Boolean{
        return friends.contains(friend.id)
    }

}
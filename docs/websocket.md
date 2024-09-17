
#  Websocket documentation

## General
All requests (messages) should have ```type``` value, it's used for choosing a request type.

If you didn't send ```type``` variable or it's not in the list of allowed you get simillar message.
```
{
    status: "ERR",
    type: "BAD_REQUEST",
    errorMessage: "Type [THIS_IS_UNKNOWN_TYPE] not recognized",
    data: {}
}
```
If type is detected, but requested params are not sended, or sended with not right type you get this.
```
{
    status: "ERR",
    type: "BAD_REQUEST",
    errorMessage: "Failed parse json to [KICK_USER] message ",
    data: {}
}
```
Point of this is to check if type exists,and if it was understood by a server.

Here is a response schema, where if status is "OK" then data contains object, related to type
```
{
    status: "OK" or "ERR",
    type: String,
    errorMessage: null or string,
    data: object
}
```

## Requests( messages that server can accept)
#### Send message
```
{
    type: "SEND_MESSAGE",
    groupId: Number,
    message: String,
}
```
returns  ```MESSAGE_SENT``` response type
#### Create group
```
{
    type: "CREATE_GROUP",
    users: Array of numbers or null,
    name: String
}
```
returns  ```CREATED_GROUP``` response type
#### Leave group
```
{
    type: "LEAVE_GROUP",
    groupId: Number
}
```
returns  ```LEFT_GROUP``` response type
#### Load the newest messages
By default, 30 most new messages are loaded. If, for example offset equals to 1,we return 30 messages after the newest one
```
{
    type: "LOAD_MESSAGES",
    groupId: Number,
    offset: Number
}
```
returns  ```LOADED_MESSAGES``` response type
#### Load messages after another message
Loads 30 messages that was written after given id
```
{
    type: "LOAD_MESSAGES_AFTER"
    groupId: Number,
    messageId: Number
}
```
returns  ```LOADED_MESSAGES``` response type
#### Get last read message from every group
```
{
    type: "GET_LAST_READ_MESSAGES"
}
```
returns ```ALL_UNREAD_MESSAGES``` response type
#### Get multiple groups info
```
{
    type: "GET_GROUPS_INFO",
    groupId: Array<Number>, // ids of groups
}
```
returns  ```GROUPS_INFO``` response type
#### Unblock user
```
{
    type: "UNBLOCK_USER",
    userId: Number,
}
```
returns  ```USER_UNBLOCKED``` response type
#### Kick user
```
{
    type: "KICK_USER",
    userId: Number,
    groupId: Number
}
```
returns  ```USER_KICKED``` response type
#### Delete group
```
{
    type: "DELETE_GROUP",
    groupId: Number
}
```
returns  ```GROUP_DELETED``` response type
#### Change group name
```
{
    type:"CHANGE_GROUP_NAME",
    groupId: Number,
    name: String
}
```
returns  ```GROUP_NAME_CHANGED``` response type
#### Remove message
```
{
    type:"REMOVE_MESSAGE",
    messageId: Number,
    groupId: Number
}
```
returns  ```MESSAGE_DELETED``` response type
#### Edit message
```
{
    type:"EDIT_MESSAGE",
    messageId: Number,
    text: String,
    groupId: Number
}
```
returns  ```MESSAGE_EDITED``` response type
#### Invite user to group
```
{
    type:"INVITE_USER",
    groupId: Int,
    userId: Number
}
```
returns  ```USER_JOINED``` response type
#### Read all messages before this
```
{
    type: "READ_MESSAGES",
    messages: messageId,
    groupId: Number
}
```
returns ```MESSAGES_READ``` response type to all users in group
#### Get users info
```
{
    type: "GET_USERS_INFO",
    usersId: Array<Number>
}
```
returns ```USERS_INFO``` type
#### Decline friend invite
```
{
    type: "DECLINE_FRIEND_INVITE",
    userId: Number
}
```
returns to you and userid  ```FRIEND_CHANGED``` with status ```DECLINED```
#### Revoke yours friend invite
```
{
    type: "REVOKE_FRIEND_INVITE",
    userId: Number
}
```
returns to you and userid  ```FRIEND_CHANGED``` with status ```REVOKED```
#### Accept friend request
```
{
    type: "ACCEPT_FRIEND_REQUEST",
    userId: Number
}
```
returns to you and userid  ```FRIEND_CHANGED``` with status ```ACCEPTED```
#### Remove friend
```
{
    type: "REMOVE_FRIEND",
    userId: Number
}
```
returns to you and userid  ```FRIEND_CHANGED``` with status ```DELETED```
#### Add friend
```
{
    type: "ADD_FRIEND",
    userId: Number
}
```
returns to you and userid  ```FRIEND_CHANGED``` with status ```SENT```
#### 
## Responses (messages that server can send to client)
#### New message received
```
{
    status: "OK",
    type: "NEW_MESSAGE",
    errorMessage: null,
    data: {
        id: Number, // id of the message
        sender: Number, // id of user who sent message
        groupId: Number,
        text: String,
        date: Date,
        edited: Bool,
        readBy: Array<Number> // id of users who read
    }
}
```
#### Get id of last read message from every group
```
{
    status: "OK",
    type: "ALL_UNREAD_MESSAGES",
    errorMessage: null,
    data: {
        messages: Map<Int, Int | null>,
        unreadAmount: Map<Int, Int>
    }
}
```
#### User blocked
```
{
    status: "OK",
    type: "USER_BLOCKED",
    errorMessage: null,
    data: {
        blockedId: Number
    }
}
```
#### User kicked
```
{
    status: "OK",
    type: "USER_KICKED",
    errorMessage: null,
    data: {
        senderId: Number, // by who was kicked
        kickedId: Number,
        groupId: Number
    }
}
```
#### User joined
```
{
    status: "OK",
    type: "USER_JOINED",
    errorMessage: null,
    data: {
        groupId: Number,
        senderId: Number,
        userId: Number
    }
}
```
#### You left a group
```
{
    status: "OK",
    type: "LEFT_GROUP",
    errorMessage: null,
    data: {
        groupId: Number
    }
}
```
#### You created group
```
{
    status: "OK",
    type: "CREATED_GROUP",
    errorMessage: null,
    data: {
        groupId: Number
    }
}
```
#### Message sent
```
{
    status: "OK",
    type: "USER_BLOCKED",
    errorMessage: null,
    data: {
        messageId: Number
    }
}
```
#### User unblocked
```
{
    status: "OK",
    type: "USER_UBLOCKED",
    errorMessage: null,
    data: {
        blockedId: Number
    }
}
```
#### Group Info
```
{
    status: "OK",
    type: "GROUP_INFO",
    errorMessage: null,
    data: {
        users: Array<Number>,
        name: String,
        creationDate: Date,
        id: Number,
        unreadAmount: Number,
        lastMessage:{
            id: Number, // id of the message
            sender: Number, // id of user who sent message
            groupId: Number,
            text: String,
            date: Date,
            edited: Bool,
            readBy: Array<Number> // id of users who read
        }
    }
}
```
#### Multiple Groups Info
```
{
    status: "OK",
    type: "GROUPS_INFO",
    errorMessage: null,
    data: Array<{
        users: Array<Number>,
        name: String,
        creationDate: Date,
        id: Number,
        unreadAmount: Number,
        lastMessage: {
            id: Number, // id of the message
            sender: Number, // id of user who sent message
            groupId: Number,
            text: String,
            date: Date,
            edited: Bool,
            readBy: Array<Number> // id of users who read
        } | null
    }>
}
```
#### Loaded messages
```
{
    status: "OK",
    type: "LOADED_MESSAGES",
    errorMessage: null,
    data: {
        messages: Array<{
            id: Number, // id of the message
            sender: Number, // id of user who sent message
            groupId: Number,
            text: String,
            date: Date,
            edited: Bool,
            readBy: Array<Number> // id of users who read
        }>,
        groupId: Number
    }
}
```
#### Bad request
```
{
    status: "ERR",
    type: "BAD_REQUEST",
    errorMessage: null,
    data: {
        
    }
}
```
#### Group name was changed
```
{
    status: "OK",
    type: "GROUP_NAME_CHANGED",
    errorMessage: null,
    data: {
        groupId:Number,
        name:String
    }
}
```
#### Message was deleted
```
{
    status: "OK",
    type: "MESSAGE_DELETED",
    errorMessage: null,
    data: {
            id: Number, // id of the message
            sender: Number, // id of user who sent message
            groupId: Number,
            text: String,
            date: Date,
            edited: Bool,
            readBy: Array<Number> // id of users who read
    }
}
```
#### Message was edited
```
{
    status: "OK",
    type: "MESSAGE_EDITED",
    errorMessage: null,
    data: {
            id: Number, // id of the message
            sender: Number, // id of user who sent message
            groupId: Number,
            text: String,
            date: Date,
            edited: Bool,
            readBy: Array<Number> // id of users who read
    }
}
```
#### User from your group became online | offline
```
{
    status: "OK",
    type: "USER_ONLINE_CHANGED",
    errorMessage: null,
    data: {
        userId: Number,
        state: Bool // true === online , false === user became offline
    }
}
```
#### Group deleted
```
{
    status: "OK",
    type: "GROUP_DELETED",
    errorMessage: null,
    data: {
        groupId: Number
    }
}
```
#### User read all messages before this
```
{
    status: "OK",
    type: "MESSAGES_READ",
    errorMessage: null,
    data: {
        groupId: Number,
        userId: Number,
        lastMessageId: Number,
    }
}
```
#### Get info about users
```
{
    status: "OK",
    type: "USERS_INFO",
    errorMessage: null,
    data: {
        users: Map<Int, {
            id: Int,
            name: String,
            online: Boolean,
            lastLogin: Date,
            createdAt: Date,
        }
    }
}
```
#### Something related to friends was changed
```
{
    status: "OK",
    type: "FRIEND_CHANGED",
    errorMessage: null,
    data: {
        userId: Number, // yours or friend id
        status: "REVOKED" | "ACCEPTED" | "DECLINED" | "DELETED" | "SENT" // enum
        initiatorId: Number, // who called
    }
}
```


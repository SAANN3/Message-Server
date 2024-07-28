
#  Websocket documentation

## General
All requests (messages) should have ```type``` value, it's used for choosing a request type.

If you didn't send ```type``` variable or it's not in the list of allowed you get simillar message.
```json
{
    status: "ERR",
    type: "BAD_REQUEST",
    errorMessage: "Type [THIS_IS_UNKNOWN_TYPE] not recognized",
    data: {}
}
```
If type is detected, but requested params are not sended, or sended with not right type you get this.
```json
{
    status: "ERR",
    type: "BAD_REQUEST",
    errorMessage: "Failed parse json to [KICK_USER] message ",
    data: {}
}
```
Point of this is to check if type exists,and if it was understood by a server.

Here is a response schema, where if status is "OK" then data contains object, related to type
```json
{
    status: "OK" or "ERR",
    type: String,
    errorMessage: null or string,
    data: object
}
```

## Requests( messages that server can accept)
#### Send message
```json
{
    type: "SEND_MESSAGE",
    groupId: Number,
    message: String,
}
```
returns  ```MESSAGE_SENT``` response type
#### Create group
```json
{
    type: "CREATE_GROUP",
    users: Array of numbers or null,
    name: String
}
```
returns  ```CREATED_GROUP``` response type
#### Leave group
```json
{
    type: "LEAVE_GROUP",
    groupId: Number
}
```
returns  ```LEFT_GROUP``` response type
#### Load messages
By default, 30 most new messages are loaded. If, for example offset equals to 1,we return 30 messages after the newest one
```json
{
    type: "LOAD_MESSAGES",
    groupId: Number,
    offset: Number
}
```
returns  ```LOADED_MESSAGES``` response type
#### Get unread messages
Not implemented
```json
{
    type: "GET_UNREAD_MESSAGES"
}
```
#### Get group info
```json
{
    type: "GET_GROUP_INFO",
    groupId: Number,
}
```
returns  ```GROUP_INFO``` response type
#### Unblock user
```json
{
    type: "UNBLOCK_USER",
    userId: Number,
}
```
returns  ```USER_UNBLOCKED``` response type
#### Kick user
```json
{
    type: "KICK_USER",
    userId: Number,
    groupId: Number
}
```
returns  ```USER_KICKED``` response type
#### Delete group
```json
{
    type: "DELETE_GROUP",
    groupId: Number
}
```
returns  ```GROUP_DELETED``` response type
#### Change group name
```json
{
    type:"CHANGE_GROUP_NAME",
    groupId: Number,
    name: String
}
```
returns  ```GROUP_NAME_CHANGED``` response type
#### Remove message
```json
{
    type:"REMOVE_MESSAGE",
    messageId: Number,
    groupId: Number
}
```
returns  ```MESSAGE_DELETED``` response type
#### Edit message
```json
{
    type:"EDIT_MESSAGE",
    messageId: Number,
    text: String,
    groupId: Number
}
```
returns  ```MESSAGE_EDITED``` response type
#### Invite user to group
```json
{
    type:"INVITE_USER",
    groupId: Int,
    userId: Number
}
```
returns  ```USER_JOINED``` response type

## Responses (messages that server can send to client)
#### New message recieved
```json
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
        edited: Bool
    }
}
```
#### Unread messages (probably be deleted or replaced)
not implemented
```json
{
    status: "OK",
    type: "ALL_UNREAD_MESSAGES",
    errorMessage: null,
    data: {
        Map<Int, {
            groupId: Number,
            unreadAmount: Number,
            lastMessage: {
                id: Number, // id of the message
                sender: Number, // id of user who sent message
                groupId: Number,
                text: String,
                date: Date,
                edited: Bool
            }
        }>
    }
}
```
#### User blocked
```json
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
```json
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
```json
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
```json
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
```json
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
```json
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
```json
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
```json
{
    status: "OK",
    type: "GROUP_INFO",
    errorMessage: null,
    data: {
        users: Array<Number>,
        name: String,
        creationDate: Date
    }
}
```
#### Loaded messages
```json
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
            edited: Bool
        }>,
        groupId: Number
    }
}
```
#### Bad request
```json
{
    status: "ERR",
    type: "BAD_REQUEST",
    errorMessage: null,
    data: {
        
    }
}
```
#### Group name was changed
```json
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
```json
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
            edited: Bool
    }
}
```
#### Message was edited
```json
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
            edited: Bool
    }
}
```
#### User from your group became online | offline
```json
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
```json
{
    status: "OK",
    type: "GROUP_DELETED",
    errorMessage: null,
    data: {
        groupId: Number
    }
}
```

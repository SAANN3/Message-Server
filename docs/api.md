
## API Reference
Basically i duplicated swagger docs here with fixed instance type, but who cares.

If you want to see websocket docs go [here](https://github.com/SAANN3/Message-Server/blob/main/docs/websocket.md) 
#### Get User info

```http
  GET /info
```

| Header          | Description  |
| :-------------- | :----------- |
| `Authorization` | bearer token |

##### Responses
| Status | Response  |
| ------ | --------- |
| 200    |<br><pre lang="json">{&#13;  "id": 1,&#13;  "name": "string",&#13;  "createdAt": "2024-07-28T18:27:06.785Z", &#13;  "lastLogin": "2024-07-28T18:27:06.785Z", &#13;  "email": "string", &#13;  "groups": [1,2,3,4,5]  &#13;}</pre>|
| 400    |Unathorized|

#### Get User settings

```http
  GET /settings
```

| Header          | Description  |
| :-------------- | :----------- |
| `Authorization` | bearer token |

##### Responses
| Status | Response  |
| ------ | --------- |
| 200    |<br><pre lang="json">{&#13; "name":"string" &#13;}</pre>|
| 400    |Unathorized|

#### Set User settings
only passed properties will be changed
```http
  POST /settings
```

| Header          | Description  |
| :-------------- | :----------- |
| `Authorization` | bearer token |
Request body
```json
{
  "name": "string",
  "password": {
    "newPassword": "string",
    "oldPassword": "string"
  }
}
```
##### Responses
| Status | Response  |
| ------ | --------- |
| 200    |ok		 |
| 400    |Unathorized|


#### Register user

```http
  POST /register
```

Request body
```json
{
  "name": "string",
  "email": "string",
  "login": "string",
  "password": "string"
}
```
##### Responses
| Status | Response  |
| ------ | --------- |
| 200    |return jwt token <br><pre lang="json">{&#13; "token":"string" &#13;}</pre>|
| 400    |User with such parameters already exists|

#### Login

```http
  POST /login
```

Request body
```json
{
  "login": "string",
  "password": "string"
}
```
##### Responses
| Status | Response  |
| ------ | --------- |
| 200    |return jwt token <br><pre lang="json">{&#13; "token":"string" &#13;}</pre>|
| 400    |User not found|

#### Main websocket
```http
  GET /messages/${token}
```
| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `token`      | `string` | **Required**. Jwt token|

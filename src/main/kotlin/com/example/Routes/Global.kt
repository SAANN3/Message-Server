package com.example.Routes

import com.example.DataClasses.*
import com.example.PostgresDb
import com.example.types.*
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.github.smiley4.ktorswaggerui.dsl.routing.route
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Route.globalRoutes(){
    authenticate {
        get("/info",{
            description = "Get user info"
            request {
                headerParameter<String>("Authorization") {
                    description = "Bearer Token"
                    required = true
                }
            }
            response {
                HttpStatusCode.OK to {
                    body<UserInfoFull>()
                }
                HttpStatusCode.Unauthorized to {
                    description = "Unathorized"
                    body<String>()
                }
            }
        })  {
            val user = call.principal<User>()!!
            call.respond(user.serializeFull())
        }

        route("/settings"){
            get({
                description = "Get user settings"
                request {
                    headerParameter<String>("Authorization") {
                        description = "Bearer Token"
                        required = true
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        body<UserSettingsGet>()
                    }
                    HttpStatusCode.Unauthorized to {
                        description = "Unathorized"
                        body<String>()
                    }
                }
            }) {
                val user = call.principal<User>()!!
                val settings = user.getSettingsSerializable()
                call.respond(settings)
            }

            post({
                description = "Set user settings, only not null parameters will be accounted"
                request {
                    headerParameter<String>("Authorization") {
                        description = "Bearer Token"
                        required = true
                    }
                    body<UserSettingsPost>()
                }
                response {
                    HttpStatusCode.OK to {
                        description = "ok"
                        body<String>()
                    }
                    HttpStatusCode.Unauthorized to {
                        description = "Unathorized"
                        body<String>()
                    }
                }
            }) {
                val user = call.principal<User>()!!
                val settings = call.receive<UserSettingsPost>()
                val userSettings = user.getSettings()
                settings.password?.let { user.changePassword(it) }
                settings.name?.let { user.changeName(it) }
                settings.nonFriendsGroupsInvites?.let { userSettings.nonFriendsGroupsInvites = it }
                user.setSettings(userSettings)
                call.respondText("ok")
            }
        }
    }

    route({
        description = "websocket"
    }){
        webSocket("/messages/{token}") {
            try {
                val user = Auth.getUserFromToken(call.parameters["token"]!!)
                try {
                    GlobalInfo.users[user.id] = UserWebsocket(this,user)
                    for (frame in incoming){
                        frame as? Frame.Text ?: continue
                        val text = frame.readText()
                        GlobalInfo.users[user.id]?.readMessage(text)
                    }
                } finally {
                    GlobalInfo.users.remove(user.id)
                    user.groups.forEach {
                        if(GlobalInfo.groups[it] != null) {
                            GlobalInfo.groups[it]?.changeOnline(user, false)
                            if (GlobalInfo.groups[it]!!.howMuchOnline() == 0) {
                                GlobalInfo.groups.remove(it)
                            }
                        }
                    }
                    user.updateLoginDate()
                }
            } catch (e:UnauthorizedException) {
                close(CloseReason(CloseReason.Codes.NORMAL,e.message))
            }
        }
    }

    post("/register",{
        description = "Register user"
        request {
            body<UserRegister>()
        }
        response {
            HttpStatusCode.OK to {
                description = "return jwt token"
                body<Token>()
            }
            HttpStatusCode.BadRequest to {
                description = "User with such parameters already exists "
                body<String>()
            }
        }
    }) {
        val user = User(call.receive<UserRegister>())
        call.respond(Auth.generateToken(user))
    }

    post("/login",{
        description = "Register user"
        request {
            body<UserLogin>()
        }
        response {
            HttpStatusCode.OK to {
                description = "return jwt token"
                body<Token>()
            }
            HttpStatusCode.NotFound to {
                description = "User not found"
                body<String>()
            }
        }
    }){
        val user = User(call.receive<UserLogin>())
        call.respond(Auth.generateToken(user))
    }

    get("/test"){
        throw BadRequestException("s")
    }

}

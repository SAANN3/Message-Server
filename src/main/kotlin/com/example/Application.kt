package com.example

import com.example.Routes.globalRoutes
import com.example.types.Auth
import com.example.types.Exception
import com.example.types.UnauthorizedException
import com.example.types.User
import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.routing.openApiSpec
import io.github.smiley4.ktorswaggerui.routing.swaggerUI
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

@OptIn(ExperimentalSerializationApi::class)
fun Application.module() {
    Auth.installJWT(Auth.Companion.JwtConfig(
        secret = environment.config.property("jwt.secret").getString(),
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        realm = environment.config.property("jwt.realm").getString()
    ))
    
    install(SwaggerUI){
        info {
            title = "Message-server"
            version = "latest"
            description = "Message-server written on kotlin with ktor."
        }
        server {
            url = "http://localhost:8080"
            description = "Development Server"
        }
    }
    install(CORS){
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
    }
    install(ContentNegotiation) {
        json(Json {
            explicitNulls = false
        })
    }
    install(Authentication) {
        jwt {
            verifier(Auth.Companion.verifier)
            realm = Auth.jwtInfo.realm
            validate {
                it.payload.getClaim("login").asString()?.let(User::invoke);
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }
    install(StatusPages) {
        status(HttpStatusCode.NotFound) { call, status ->
            call.respondText(text = "404: Page Not Found", status = status)
        }
        status(HttpStatusCode.MethodNotAllowed) { call, status ->
            call.respondText(text = "405: Method not allowed", status = status)
        }
        exception<Throwable> { call,cause ->
            when(cause) {
                is NumberFormatException -> call.respondText(
                    text = "${HttpStatusCode.BadRequest.value}: Not a number: ${cause.message}",
                    status = HttpStatusCode.BadRequest)

                is NotFoundException -> call.respondText(
                    text = "${HttpStatusCode.NotFound.value} ${cause.message}",
                    status = HttpStatusCode.NotFound)
                is BadRequestException -> call.respondText(
                    text = "${HttpStatusCode.BadRequest.value}: Bad request: ${cause.message}",
                    status = HttpStatusCode.BadRequest
                )
                is UnauthorizedException -> call.respondText(
                    text = cause.message,
                    status = cause.code
                )
                is Exception -> call.respondText(
                    text = cause.message,
                    status = cause.code
                )
                else -> {
                    call.respondText(text = "500: Internal error", status = HttpStatusCode.InternalServerError)
                }
            }
        }
    }
    configureDatabase()
    routing {
        globalRoutes()
        route("api.json") {
            openApiSpec() // api-spec json is served at '/myApi.json'
        }
        route("swagger") {
            swaggerUI("/api.json") // swagger-ui is available at '/mySwagger' or '/mySwagger/index.html'
        }
    }
}

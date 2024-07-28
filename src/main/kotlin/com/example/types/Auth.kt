package com.example.types

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.example.DataClasses.*
import com.example.PostgresDb
import io.ktor.server.plugins.*
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.Serializable
import java.time.temporal.ChronoUnit
@Serializable
data class Token(val token:String);
class Auth {
    companion object {
        data class JwtConfig (val secret:String, val issuer:String,val audience:String,val realm:String)
        lateinit var jwtInfo:JwtConfig
        lateinit var verifier: JWTVerifier

        fun installJWT(jwtInfo: JwtConfig){
            this.jwtInfo = jwtInfo
            verifier = JWT
                .require(Algorithm.HMAC256(Companion.jwtInfo.secret))
                .withIssuer(Companion.jwtInfo.issuer)
                .build()
        }
        fun bcryptPassword(password:String): String {
            return BCrypt.withDefaults().hashToString(12,password.toCharArray())
        }
        fun verifyPassword(oldPassword:String,plainPassword:String):Boolean{
            return  BCrypt.verifyer().verify(plainPassword.toCharArray(),oldPassword.toCharArray()).verified;
        }
        fun generateToken(user:User):Token{
            val token = JWT.create()
                .withSubject("Authentication")
                .withAudience(jwtInfo.audience)
                .withIssuer(jwtInfo.issuer)
                .withClaim("login", user.login)
                .withExpiresAt(Clock.System.now().toJavaInstant().plus(365,ChronoUnit.DAYS))
                .sign(Algorithm.HMAC256(jwtInfo.secret))
            return Token(token);
        }
        fun getUserFromToken(token:String): User{
            return try {
                val login = Auth.verifier.verify(token).getClaim("login").asString()!!
                User(login)
            } catch (e: Throwable){
                throw UnauthorizedException()
            }
        }
    }
}
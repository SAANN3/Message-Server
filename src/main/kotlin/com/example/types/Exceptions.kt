package com.example.types

import io.ktor.http.*

open class Exception(
    override val message: String,
    val code: HttpStatusCode
): Throwable()
class UnauthorizedException() : Exception("Unauthorized",HttpStatusCode.Unauthorized)
class NotFoundException(message: String): Exception(message, HttpStatusCode.NotFound)
class BadRequestException(message: String): Exception(message, HttpStatusCode.BadRequest)
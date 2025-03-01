package com.motycka.edu.game.account.rest

/**
 * Request object for registering a new account.
 * This object is used to map the incoming JSON request to a Kotlin object and is exposed to outside world.
 */
data class AccountRegistrationRequest(
    val name: String,
    val username: String,
    val password: String
)


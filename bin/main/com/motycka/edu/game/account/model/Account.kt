package com.motycka.edu.game.account.model

/**
 * This is a class that represents a data entity for an account.
 * It is used internally within the application and is not supposed to be exposed to the outside world.
 */
data class Account(
    val id: AccountId? = null,
    val name: String,
    val username: String,
    val password: String
)

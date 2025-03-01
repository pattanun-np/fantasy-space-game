package com.motycka.edu.game.characters.rest

/**
 * Request object for registering a new account.
 * This object is used to map the incoming JSON request to a Kotlin object and is exposed to outside world.
 */
data class CharacterRegistrationRequest(
    val name: String,
    val characterClass: String,
    val health: Int,
    val attackPower: Int,
    val stamina: Int,
    val defensePower: Int

)


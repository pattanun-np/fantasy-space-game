package com.motycka.edu.game.characters.rest

/**
 * Request object for updating a character (level up).
 * This object is used to map the incoming JSON request to a Kotlin object.
 */
data class CharacterUpdateRequest(
    val name: String,
    val health: Int,
    val attackPower: Int,
    val stamina: Int?,
    val defensePower: Int?,
    val mana: Int?,
    val healingPower: Int?
)

fun CharacterUpdateRequest.toCharacter() = com.motycka.edu.game.characters.model.Character(
    id = null,
    name = name,
    health = health,
    attack = attackPower,
    stamina = stamina ?: 0,
    defense = defensePower ?: 0,
    mana = mana,
    healing = healingPower,
    characterClass = determineCharacterClass()
)

private fun CharacterUpdateRequest.determineCharacterClass(): String {
    return when {
        mana != null && healingPower != null && stamina == null && defensePower == null -> "SORCERER"
        stamina != null && defensePower != null && mana == null && healingPower == null -> "WARRIOR"
        else -> throw IllegalArgumentException(
            "Invalid attribute combination. Warriors must have stamina and defense power (no mana/healing). " +
            "Sorcerers must have mana and healing power (no stamina/defense)."
        )
    }
} 
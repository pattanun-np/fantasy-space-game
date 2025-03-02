package com.motycka.edu.game.characters.rest

data class CharacterResponse(
    val id: String,
    val name: String,
    val health: Int,
    val attack: Int,
    val stamina: Int,
    val defense: Int,
    val characterClass: String,
    val experience: Int,
    val level: String,
    val isOwner: Boolean,
    val shouldLevelUp: Boolean,
    val mana: Int?,
    val healing: Int?
)




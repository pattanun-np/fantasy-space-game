package com.motycka.edu.game.characters.rest

import com.motycka.edu.game.characters.model.CharacterId

data class CharacterResponse(
    val id: CharacterId,
    val name: String,
    val health: Int,
    val attack: Int,
    val stamina: Int,
    val defense: Int,
    val characterClass: String,
    val experience: Int,
    val level: String,
    val isOwner: Boolean
)




package com.motycka.edu.game.characters.rest

import com.motycka.edu.game.characters.model.Character


fun CharacterRegistrationRequest.toCharacter() = Character(
    id = null,
    name = name,
    health = health,
    attack = attackPower,
    stamina = stamina,
    defense = defensePower,
    characterClass = characterClass,

)

fun Character.toCharacterResponse() = experience?.let {
    CharacterResponse(
        id = requireNotNull(id) { "Character id must not be null" },
        name = name,
        health = health,
        attack = attack,
        stamina = stamina,
        defense = defense,
        characterClass = characterClass,
        experience = it,
        level = level.toString(),
        isOwner = isOwner


//    characterClass = when(this){
//        is Warrior -> CharacterClass.WARRIOR
//        is Sorcerer -> CharacterClass.SORCERER
//        else -> error("Unknown character class")
//    }.name,

    )
}

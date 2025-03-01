package com.motycka.edu.game.characters.rest

import com.motycka.edu.game.characters.model.*

fun CharacterRegistrationRequest.toCharacter(): Character {

    var healingPower = 0
    return when (characterClass) {
        CharacterClass.WARRIOR.toString() -> Warrior(
            id = null, accountId = 0, name = name, health = health, attackPower = attackPower,
            stamina = stamina, defensePower = defensePower, experience = 0, level = CharacterLevel.LEVEL_1
        )

        CharacterClass.SORCERER.toString() -> Sorcerer(
            id = null, accountId = 0, name = name, health = health, attackPower = attackPower,
            mana = stamina, defensePower = defensePower, experience = 0, level = CharacterLevel.LEVEL_1,
            healingPower = healingPower
        )

        else -> error("Unsupported character class: $characterClass")
    }
}

fun Character.toCharacterResponse() = experience?.let {
    CharacterResponse(
        id = id,
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

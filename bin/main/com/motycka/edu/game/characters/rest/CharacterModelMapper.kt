package com.motycka.edu.game.characters.rest

import com.motycka.edu.game.characters.model.Character
import com.motycka.edu.game.characters.model.CharacterClass
import com.motycka.edu.game.characters.model.CharacterLevel
import com.motycka.edu.game.characters.model.Sorcerer
import com.motycka.edu.game.characters.model.Warrior

fun CharacterRegistrationRequest.toCharacter(): Character {

    var healingPower = 0
    return when (characterClass) {
        CharacterClass.WARRIOR.toString() -> Warrior(
            id = null, accountId = 0, name = name, health = health, attackPower = attackPower,
            stamina = stamina, defensePower = defensePower, experience = 0, level = CharacterLevel.LEVEL_1
        )

        CharacterClass.SORCERER.toString() -> Sorcerer(
            id = null, accountId = 0, name = name, health = health, attackPower = attackPower,
            mana = stamina, experience = 0, level = CharacterLevel.LEVEL_1,
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
        mana = if (this is Sorcerer) this.mana else null,
        healing = if (this is Sorcerer) this.healing else null,
        characterClass = characterClass,
        experience = it,
        level = level.toString(),
        shouldLevelUp = CharacterLevel.fromLevel(level).shouldLevelUp(it),
        isOwner = isOwner
    )
}

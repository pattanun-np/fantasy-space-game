package com.motycka.edu.game.characters.model

import com.motycka.edu.game.account.model.AccountId
import io.github.oshai.kotlinlogging.KotlinLogging

class Warrior(
    id: Long?,
    accountId: AccountId,
    name: String,
    health: Int,
    attackPower: Int,
    stamina: Int,
    defensePower: Int,
    level: CharacterLevel,
    experience: Int
) : Character(
    id = id.toString(),
    accountId = accountId,
    name = name,
    health = health,
    attack = attackPower,
    characterClass = CharacterClass.WARRIOR.name,
    experience = experience,
    level = level.ordinal + 1,
    stamina = stamina,
    defense = defensePower
) {

    private val logger = KotlinLogging.logger {}



    override fun attack(target: Character) {
        if (!isAlive()) {
            logger.info { "$name is dead and cannot attack" }
            return
        }
        if (stamina <= 0) {
            logger.info { "$name is too tired to attack" }
            return
        } else {
            logger.info { "$name swings a sword at ${target.name}" }
            target.receiveAttack(attack)
            stamina -= 1
        }
    }
}
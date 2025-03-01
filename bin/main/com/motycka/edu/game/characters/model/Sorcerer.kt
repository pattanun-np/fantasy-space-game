package com.motycka.edu.game.characters.model

import com.motycka.edu.game.account.model.AccountId
import io.github.oshai.kotlinlogging.KotlinLogging

class Sorcerer(
    id: Long?,
    accountId: AccountId,
    name: String,
    health: Int,
    attackPower: Int,
    level: CharacterLevel,
    experience: Int,
    mana: Int,
    healingPower: Int,
) : Character(
    id = id.toString(),
    accountId = accountId,
    name = name,
    health = health,
    attack = attackPower,
    characterClass = CharacterClass.SORCERER.name,
    experience = experience,
    level = level.ordinal + 1,
    mana = mana,
    healing = healingPower
) {

    private val logger = KotlinLogging.logger {}

    override fun attack(target: Character) {
        if (!isAlive()) {
            logger.info { "$name is dead and cannot attack" }
            return
        }
        if (mana <= 0) {
            logger.info { "$name is too tired to attack" }
            return
        } else {
            logger.info { "$name use wand at ${target.name}" }
            target.receiveAttack(attack)
            mana -= 1
        }
    }
}
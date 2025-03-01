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
    defensePower: Int,
    override val mana: Int,
    val healingPower: Int,
) : Character(

    id = id.toString(),
    accountId = accountId,
    name = name,
    health = health,
    attack = attackPower,
    stamina = mana,
    defense = defensePower,

    characterClass = CharacterClass.SORCERER.name,
    experience = experience,
    level = level.ordinal + 1


) {


    private val logger = KotlinLogging.logger {}

    override fun attack(target: Character) {
        if (!isAlive()) {

            logger.info { "$name is dead and cannot attack" }

        }
        if (stamina <= 0) {
            logger.info { "$name is too tired to attack" }
            return
        } else {
            logger.info { "$name use wand at ${target.name}" }
            target.receiveAttack(attack)
            stamina -= 1
        }
    }


}
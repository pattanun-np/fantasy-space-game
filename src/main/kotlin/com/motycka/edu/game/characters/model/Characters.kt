package com.motycka.edu.game.characters.model

import com.motycka.edu.game.account.model.AccountId
import io.github.oshai.kotlinlogging.KotlinLogging


abstract class Character(
    val id: String = null.toString(),
    val accountId: AccountId? = null,
    val name: String,
    var health: Int,
    val attack: Int,
    var stamina: Int,
    val defense: Int,
    open val mana: Int? = null,
    val healing: Int? = null,
    val characterClass: String,
    var experience: Int? = null,
    var level: Int = 1,
    var isOwner: Boolean = false
) {

    private val logger = KotlinLogging.logger {}

    private var currentHealth = health




    open fun isAlive(): Boolean {
        return currentHealth > 0
    }

    open fun receiveAttack(attackPower: Int) {
        currentHealth -= attackPower

        if (currentHealth <= 0) {
            logger.info { "$name has been defeated" }
            currentHealth = 0
        } else {
            logger.info { "$name has $currentHealth remaining" }
        }
    }

    open fun getCurrentHealth(): Int {
        return currentHealth
    }




    fun getDefensePower(): Int {
        return defense
    }

    abstract fun attack(target: Character)


    open fun heal(heal: Int) {
        health += heal
    }


}

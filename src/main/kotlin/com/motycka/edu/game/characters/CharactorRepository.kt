package com.motycka.edu.game.characters

import com.motycka.edu.game.account.model.AccountId
import com.motycka.edu.game.characters.model.Character
import com.motycka.edu.game.characters.model.CharacterLevel
import com.motycka.edu.game.characters.model.CharacterLevel.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.SQLException
import com.motycka.edu.game.characters.model.Warrior
import com.motycka.edu.game.characters.model.Sorcerer

@Repository
class CharacterRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    fun selectAll(characterClass: String?, name: String?, accID: AccountId?): List<Character> {
        return executeQuery(characterClass, name, accID, includeSameAccount = true)
    }

    fun selectOpponent(characterClass: String?, name: String?, accID: AccountId?): List<Character> {
        return executeQuery(characterClass, name, accID, includeSameAccount = false)
    }

    fun selectById(id: AccountId): Character? {
        logger.debug { "Selecting character by id $id" }
        return jdbcTemplate.query(
            "SELECT * FROM character WHERE account_id = ?;",
            ::rowMapper,
            id
        ).firstOrNull()
    }

    fun selectByName(name: String): Character? {
        logger.debug { "Selecting character by name ***" }
        return jdbcTemplate.query(
            "SELECT * FROM character WHERE name = ?;",
            ::rowMapper,
            name
        ).firstOrNull()
    }

    fun insertCharacter(character: Character, accID: AccountId): Character? {
        logger.debug { "Inserting new character ${character.name}" }
        return jdbcTemplate.query(
            """
              SELECT * FROM FINAL TABLE (
                  INSERT INTO character (account_id, name, health, attack, stamina, defense, mana, healing, class, experience, level) 
                  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
              )
          """.trimIndent(),
            ::rowMapper,
            accID,
            character.name,
            character.health,
            character.attack,
            character.stamina,
            character.defense,
            character.mana,
            character.healing,
            character.characterClass,
            character.experience,
            character.level
        ).firstOrNull()
    }

    fun updateCharacter(character: Character): Character? {
        logger.debug { "Updating character with ID: ${character.id}" }
        return jdbcTemplate.query(
            """
            SELECT * FROM FINAL TABLE (
                UPDATE character 
                SET name = ?, 
                    health = ?, 
                    attack = ?, 
                    stamina = ?, 
                    defense = ?, 
                    mana = ?, 
                    healing = ?,
                    level = ?
                WHERE id = ?
            )
            """.trimIndent(),
            ::rowMapper,
            character.name,
            character.health,
            character.attack,
            character.stamina,
            character.defense,
            character.mana,
            character.healing,
            character.level,
            character.id
        ).firstOrNull()
    }

    private fun executeQuery(
        characterClass: String?,
        name: String?,
        accID: AccountId?,
        includeSameAccount: Boolean
    ): List<Character> {
        val sql = buildQuery(characterClass, name, accID, includeSameAccount)
        val params = mutableListOf<Any>().apply {
            characterClass?.let { add(it) }
            name?.let { add(it) }
            accID?.let { add(it) }
        }

        return jdbcTemplate.query(sql, ::rowMapper, *params.toTypedArray())
    }

    private fun buildQuery(
        characterClass: String?,
        name: String?,
        accID: AccountId?,
        includeSameAccount: Boolean
    ): String {
        return StringBuilder("SELECT * FROM character WHERE 1=1").apply {
            if (characterClass != null) append(" AND class = ?")
            if (name != null) append(" AND name = ?")
            if (accID != null) {
                append(if (includeSameAccount) " AND account_id = ?" else " AND account_id != ?")
            }
            append(" ORDER BY name")
        }.toString()
    }

    @Throws(SQLException::class)
    private fun rowMapper(rs: ResultSet, i: Int): Character {
        val id = rs.getLong("id")
        val accountId = rs.getLong("account_id")
        val name = rs.getString("name")
        val health = rs.getInt("health")
        val attack = rs.getInt("attack")
        val stamina = rs.getInt("stamina")
        val defense = rs.getInt("defense")
        val mana = rs.getInt("mana")
        val healing = rs.getInt("healing")
        val characterClass = rs.getString("class")
        val experience = rs.getInt("experience")
        val level = rs.getInt("level")



        if (characterClass == "WARRIOR") {
            return Warrior(
                id = id,
                accountId = accountId,
                name = name,
                health = health,
                attackPower = attack,
                stamina = stamina,
                defensePower = defense,
                level = toCharacterLevel(level),
                experience = experience,
            )
        } else if (characterClass == "SORCERER") {
            return Sorcerer(
                id = id,
                accountId = accountId,
                name = name,
                health = health,
                attackPower = attack,
                mana = mana,
                healingPower = healing,
                level = toCharacterLevel(level),
                experience = experience,
                defensePower = defense
            )
        }

        throw IllegalArgumentException("Invalid character class")

    }

    fun toCharacterLevel(level: Int): CharacterLevel {
        return when (level) {
            1 -> LEVEL_1
            2 -> LEVEL_2
            3 -> LEVEL_3
            4 -> LEVEL_4
            5 -> LEVEL_5
            6 -> LEVEL_6
            7 -> LEVEL_7
            8 -> LEVEL_8
            9 -> LEVEL_9
            10 -> LEVEL_10
            else -> LEVEL_1
        }
    }


}
package com.motycka.edu.game.characters

import com.motycka.edu.game.account.model.AccountId
import com.motycka.edu.game.characters.model.Character
import com.motycka.edu.game.characters.model.CharacterLevel
import com.motycka.edu.game.characters.model.CharacterLevel.*
import io.github.oshai.kotlinlogging.KotlinLogging
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
    private val logger = KotlinLogging.logger {}

    fun selectById(id: Long): Character? {
        logger.debug { "Selecting character by id $id" }
        return jdbcTemplate.query(
            "SELECT * FROM character WHERE id = ?;",
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

    fun updateExperience(characterId: Long, newExperience: Int, newLevel: Int): Character? {
        logger.debug { "Updating experience for character $characterId to $newExperience and level to $newLevel" }
        return jdbcTemplate.query(
            """
            SELECT * FROM FINAL TABLE (
                UPDATE character 
                SET experience = ?, 
                    level = ?
                WHERE id = ?
            )
            """.trimIndent(),
            ::rowMapper,
            newExperience,
            newLevel,
            characterId
        ).firstOrNull()
    }

    fun selectAll(characterClass: String?, name: String?, accID: AccountId?): List<Character> {
        logger.debug { "Selecting all characters with class: $characterClass, name: $name, accountId: $accID (including same account)" }
        return executeQuery(characterClass, name, accID, includeSameAccount = true)
    }

    fun selectOpponent(characterClass: String?, name: String?, accID: AccountId?): List<Character> {
        logger.debug { "Selecting opponent characters with class: $characterClass, name: $name, excluding accountId: $accID" }
        // Only proceed if we have an account ID to filter against
        if (accID == null) {
            logger.warn { "Cannot select opponents without an account ID" }
            return emptyList()
        }
        
        // Check if there are any characters in the database at all
        val allCharactersCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM character", Int::class.java) ?: 0
        logger.info { "Total characters in database: $allCharactersCount" }
        
        // Check if there are any characters not owned by this account
        val opponentsCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM character WHERE account_id != ?", 
            Int::class.java, 
            accID
        ) ?: 0
        logger.info { "Characters not owned by account $accID: $opponentsCount" }
        
        return executeQuery(characterClass, name, accID, includeSameAccount = false)
    }
    
    private fun executeQuery(
        characterClass: String?,
        name: String?,
        accID: AccountId?,
        includeSameAccount: Boolean
    ): List<Character> {
        // Special case: if we're looking for opponents but there's only one account in the system
        if (!includeSameAccount && accID != null) {
            val distinctAccounts = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT account_id) FROM character", 
                Int::class.java
            ) ?: 0
            
            logger.info { "Number of distinct accounts in the system: $distinctAccounts" }
            
            // If there's only one account, we can't have opponents
            if (distinctAccounts <= 1) {
                logger.info { "Only one account in the system, no opponents possible" }
                return emptyList()
            }
        }
        
        val sql = buildQuery(characterClass, name, accID, includeSameAccount)
        val params = mutableListOf<Any>().apply {
            // Only add characterClass if it's not null and not "null" string
            if (!characterClass.isNullOrEmpty() && characterClass != "null") {
                add(characterClass)
            }
            // Only add name if it's not null and not empty
            if (!name.isNullOrEmpty()) {
                add("%$name%")
            }
            // Add accountId if provided - this will be used to find challengers
            // when includeSameAccount is true, or opponents when false
            accID?.let { add(it) }
        }

        logger.info { "Executing SQL: $sql with params: $params" }
        try {
            val results = jdbcTemplate.query(sql, ::rowMapper, *params.toTypedArray())
            logger.info { "Query returned ${results.size} results" }
            return results
        } catch (e: Exception) {
            logger.error(e) { "Error executing query: $sql with params: $params" }
            throw e
        }
    }

    private fun buildQuery(
        characterClass: String?,
        name: String?,
        accID: AccountId?,
        includeSameAccount: Boolean
    ): String {
        val query = StringBuilder("SELECT * FROM character WHERE 1=1")
        
        // Add class filter if provided
        if (!characterClass.isNullOrEmpty() && characterClass != "null") {
            query.append(" AND class = ?")
        }
        
        // Add name filter if provided
        if (!name.isNullOrEmpty()) {
            query.append(" AND name LIKE ?")
        }
        
        // Add account filter if provided
        if (accID != null) {
            // For challengers (own characters), use account_id = ?
            // For opponents (other characters), use account_id != ?
            val operator = if (includeSameAccount) "=" else "!="
            query.append(" AND account_id $operator ?")
        }
        
        // Always order by name
        query.append(" ORDER BY name")
        
        val finalQuery = query.toString()
        logger.debug { "Built query: $finalQuery" }
        return finalQuery
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
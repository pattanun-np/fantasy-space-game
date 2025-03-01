package com.motycka.edu.game.characters

import com.motycka.edu.game.account.model.AccountId
import com.motycka.edu.game.characters.model.Character
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.SQLException

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
        return Character(
            rs.getLong("id"),
            rs.getLong("account_id"),
            rs.getString("name"),
            rs.getInt("health"),
            rs.getInt("attack"),
            rs.getInt("stamina"),
            rs.getInt("defense"),
            rs.getInt("mana"),
            rs.getInt("healing"),
            rs.getString("class"),
            rs.getInt("experience"),
            rs.getInt("level")
        )
    }
}
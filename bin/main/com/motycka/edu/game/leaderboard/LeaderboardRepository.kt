package com.motycka.edu.game.leaderboard

import com.motycka.edu.game.characters.CharacterRepository
import com.motycka.edu.game.leaderboard.model.LeaderboardEntry
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.sql.ResultSet

private val logger = KotlinLogging.logger {}

@Repository
class LeaderboardRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val characterRepository: CharacterRepository
) {

    fun getLeaderboard(characterClass: String? = null): List<LeaderboardEntry> {
        logger.info { "Getting leaderboard with filter: $characterClass" }
        
        val sql = buildString {
            append("""
                SELECT l.character_id, l.wins, l.losses, l.draws, 
                       c.id, c.name, c.class, c.health, c.attack, c.experience, 
                       c.defense, c.stamina, c.healing, c.mana, c.level, c.account_id
                FROM leaderboard l
                JOIN character c ON l.character_id = c.id
            """.trimIndent())
            
            if (characterClass != null) {
                append(" WHERE c.class = ?")
            }
            
            append(" ORDER BY (l.wins * 3 - l.losses + l.draws) DESC")
        }
        
        val params = if (characterClass != null) arrayOf(characterClass) else arrayOf()
        
        val entries = jdbcTemplate.query(sql, { rs, rowNum ->
            val characterId = rs.getLong("character_id")
            val character = characterRepository.selectById(characterId)
                ?: throw IllegalStateException("Character not found for ID: $characterId")
            
            LeaderboardEntry(
                position = rowNum + 1, // Position is 1-indexed
                character = character,
                wins = rs.getInt("wins"),
                losses = rs.getInt("losses"),
                draws = rs.getInt("draws")
            )
        }, *params)
        
        return entries
    }
    
    fun updateLeaderboard(characterId: Long, isWin: Boolean, isLoss: Boolean, isDraw: Boolean) {
        logger.info { "Updating leaderboard for character $characterId: win=$isWin, loss=$isLoss, draw=$isDraw" }
        
        // Check if character exists in leaderboard
        val exists = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM leaderboard WHERE character_id = ?",
            Int::class.java,
            characterId
        ) ?: 0
        
        if (exists > 0) {
            // Update existing entry
            val sql = """
                UPDATE leaderboard 
                SET wins = wins + ?, 
                    losses = losses + ?, 
                    draws = draws + ? 
                WHERE character_id = ?
            """.trimIndent()
            
            jdbcTemplate.update(
                sql,
                if (isWin) 1 else 0,
                if (isLoss) 1 else 0,
                if (isDraw) 1 else 0,
                characterId
            )
        } else {
            // Create new entry
            val params = mapOf(
                "character_id" to characterId,
                "wins" to if (isWin) 1 else 0,
                "losses" to if (isLoss) 1 else 0,
                "draws" to if (isDraw) 1 else 0
            )
            
            SimpleJdbcInsert(jdbcTemplate)
                .withTableName("leaderboard")
                .execute(params)
        }
    }
} 
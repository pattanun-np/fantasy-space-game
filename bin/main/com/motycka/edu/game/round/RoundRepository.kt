package com.pattnun.game.matches

import com.pattnun.game.matches.model.Round
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.Timestamp

private val logger = KotlinLogging.logger {}

@Repository
class RoundRepository(private val jdbcTemplate: JdbcTemplate) {

    private val roundRowMapper = RowMapper<Round> { rs: ResultSet, _: Int ->
        Round(
            id = rs.getLong("id"),
            matchId = rs.getLong("match_id"),
            roundNumber = rs.getInt("round_number"),
            characterId = rs.getLong("character_id"),
            healthDelta = rs.getInt("health_delta"),
            staminaDelta = rs.getInt("stamina_delta"),
            manaDelta = rs.getInt("mana_delta"),
            isWinner = rs.getBoolean("is_winner"),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime()
        )
    }

    

    
    fun saveRound(round: Round): Long {
        logger.debug { "Saving round ${round.roundNumber} for match ${round.matchId}" }
        
        jdbcTemplate.update(
            """
            INSERT INTO round (match_id, round_number, character_id, health_delta, stamina_delta, mana_delta, is_winner)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            round.matchId,
            round.roundNumber,
            round.characterId,
            round.healthDelta,
            round.staminaDelta,
            round.manaDelta,
            round.isWinner
        )
        
        return jdbcTemplate.queryForObject(
            "SELECT LAST_INSERT_ID()",
            Long::class.java
        ) ?: throw RuntimeException("Failed to get ID of inserted round")
    }

    fun getRoundsByMatchId(matchId: Long): List<Round> {
        logger.debug { "Getting rounds for match $matchId" }
        
        return jdbcTemplate.query(
            """
            SELECT id, match_id, round_number, character_id, health_delta, stamina_delta, mana_delta, is_winner, created_at
            FROM round
            WHERE match_id = ?
            ORDER BY round_number
            """.trimIndent(),
            roundRowMapper,
            matchId
        )
    }

    fun deleteRoundsByMatchId(matchId: Long) {
        logger.debug { "Deleting rounds for match $matchId" }
        
        jdbcTemplate.update(
            "DELETE FROM round WHERE match_id = ?",
            matchId
        )
    }
} 
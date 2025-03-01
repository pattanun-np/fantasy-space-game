package com.motycka.edu.game.matches

import com.motycka.edu.game.matches.model.*
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.*

private val logger = KotlinLogging.logger {}

@Repository
class MatchRepository(private val jdbcTemplate: JdbcTemplate) {

    fun getAllMatches(): List<Match> {
        logger.info { "Getting all matches" }

        val sql = """
            SELECT m.id as match_id, m.challenger_id, m.opponent_id, 
                   c1.id as challenger_character_id, c1.name as challenger_name, c1.class as challenger_class, c1.level as challenger_level, c1.experience as challenger_exp, m.challenger_exp_gained, m.challenger_is_victor,
                   c2.id as opponent_character_id, c2.name as opponent_name, c2.class as opponent_class, c2.level as opponent_level, c2.experience as opponent_exp, m.opponent_exp_gained, m.opponent_is_victor
            FROM match m
            JOIN character c1 ON m.challenger_id = c1.id
            JOIN character c2 ON m.opponent_id = c2.id
        """.trimIndent()

        val matches = jdbcTemplate.query(sql) { rs, _ ->
            val matchId = rs.getLong("match_id")
            
            val challenger = MatchCharacter(
                id = rs.getLong("challenger_character_id"),
                name = rs.getString("challenger_name"),
                characterClass = rs.getString("challenger_class"),
                level = rs.getString("challenger_level"),
                experienceTotal = rs.getInt("challenger_exp"),
                experienceGained = rs.getInt("challenger_exp_gained"),
                isVictor = rs.getBoolean("challenger_is_victor")
            )
            
            val opponent = MatchCharacter(
                id = rs.getLong("opponent_character_id"),
                name = rs.getString("opponent_name"),
                characterClass = rs.getString("opponent_class"),
                level = rs.getString("opponent_level"),
                experienceTotal = rs.getInt("opponent_exp"),
                experienceGained = rs.getInt("opponent_exp_gained"),
                isVictor = rs.getBoolean("opponent_is_victor")
            )
            
            val rounds = getRoundsForMatch(matchId)
            
            Match(
                id = matchId,
                challenger = challenger,
                opponent = opponent,
                rounds = rounds
            )
        }

        return matches
    }
    
    fun getMatchById(matchId: Long): Match? {
        logger.info { "Getting match by id: $matchId" }
        
        val sql = """
            SELECT m.id as match_id, m.challenger_id, m.opponent_id, 
                   c1.id as challenger_character_id, c1.name as challenger_name, c1.class as challenger_class, c1.level as challenger_level, c1.experience as challenger_exp, m.challenger_exp_gained, m.challenger_is_victor,
                   c2.id as opponent_character_id, c2.name as opponent_name, c2.class as opponent_class, c2.level as opponent_level, c2.experience as opponent_exp, m.opponent_exp_gained, m.opponent_is_victor
            FROM match m
            JOIN character c1 ON m.challenger_id = c1.id
            JOIN character c2 ON m.opponent_id = c2.id
            WHERE m.id = ?
        """.trimIndent()
        
        val matches = jdbcTemplate.query(sql, { rs, _ ->
            val id = rs.getLong("match_id")
            
            val challenger = MatchCharacter(
                id = rs.getLong("challenger_character_id"),
                name = rs.getString("challenger_name"),
                characterClass = rs.getString("challenger_class"),
                level = rs.getString("challenger_level"),
                experienceTotal = rs.getInt("challenger_exp"),
                experienceGained = rs.getInt("challenger_exp_gained"),
                isVictor = rs.getBoolean("challenger_is_victor")
            )
            
            val opponent = MatchCharacter(
                id = rs.getLong("opponent_character_id"),
                name = rs.getString("opponent_name"),
                characterClass = rs.getString("opponent_class"),
                level = rs.getString("opponent_level"),
                experienceTotal = rs.getInt("opponent_exp"),
                experienceGained = rs.getInt("opponent_exp_gained"),
                isVictor = rs.getBoolean("opponent_is_victor")
            )
            
            val rounds = getRoundsForMatch(id)
            
            Match(
                id = id,
                challenger = challenger,
                opponent = opponent,
                rounds = rounds
            )
        }, matchId)
        
        return matches.firstOrNull()
    }
    
    fun saveMatch(match: Match): Match {
        logger.info { "Saving match: $match" }
        
        val matchParams = mapOf(
            "challenger_id" to match.challenger.id,
            "opponent_id" to match.opponent.id,
            "challenger_exp_gained" to match.challenger.experienceGained,
            "opponent_exp_gained" to match.opponent.experienceGained,
            "challenger_is_victor" to match.challenger.isVictor,
            "opponent_is_victor" to match.opponent.isVictor
        )
        
        val matchId = SimpleJdbcInsert(jdbcTemplate)
            .withTableName("match")
            .usingGeneratedKeyColumns("id")
            .executeAndReturnKey(matchParams)
            .toLong()
        
        // Save rounds if any
        match.rounds.forEach { round ->
            saveRound(matchId, round)
        }
        
        return match.copy(id = matchId)
    }
    
    fun updateMatch(match: Match): Match {
        logger.info { "Updating match: ${match.id}" }
        
        // Update match details
        val sql = """
            UPDATE match 
            SET challenger_exp_gained = ?, 
                opponent_exp_gained = ?,
                challenger_is_victor = ?,
                opponent_is_victor = ?
            WHERE id = ?
        """.trimIndent()
        
        jdbcTemplate.update(
            sql,
            match.challenger.experienceGained,
            match.opponent.experienceGained,
            match.challenger.isVictor,
            match.opponent.isVictor,
            match.id
        )
        
        // Delete existing rounds
        jdbcTemplate.update("DELETE FROM match_round WHERE match_id = ?", match.id)
        
        // Save new rounds
        match.rounds.forEach { round ->
            saveRound(match.id!!, round)
        }
        
        return match
    }
    
    private fun saveRound(matchId: Long, round: MatchRound) {
        logger.info { "Saving round: $round for match: $matchId" }
        
        // First save the round
        val roundParams = mapOf(
            "match_id" to matchId,
            "round_number" to round.round,
            "character_id" to round.characterId,
            "health_delta" to round.healthDelta,
            "stamina_delta" to round.staminaDelta,
            "mana_delta" to round.manaDelta
        )
        
        val roundId = SimpleJdbcInsert(jdbcTemplate)
            .withTableName("match_round")
            .usingGeneratedKeyColumns("id")
            .executeAndReturnKey(roundParams)
            .toLong()
        
        // Then save the flight if it exists
        round.flight?.let { flight ->
            val flightParams = mapOf(
                "round_id" to roundId,
                "flight_type" to flight.flightType.name,
                "distance" to flight.distance,
                "duration" to flight.duration,
                "success" to flight.success
            )
            
            SimpleJdbcInsert(jdbcTemplate)
                .withTableName("flight")
                .usingGeneratedKeyColumns("id")
                .executeAndReturnKey(flightParams)
        }
    }
    
    private fun getRoundsForMatch(matchId: Long): List<MatchRound> {
        logger.info { "Getting rounds for match: $matchId" }
        
        val sql = """
            SELECT r.id as round_id, r.round_number, r.character_id, r.health_delta, r.stamina_delta, r.mana_delta,
                   f.id as flight_id, f.flight_type, f.distance, f.duration, f.success
            FROM match_round r
            LEFT JOIN flight f ON r.id = f.round_id
            WHERE r.match_id = ?
            ORDER BY r.round_number
        """.trimIndent()
        
        return jdbcTemplate.query(sql, { rs, _ ->
            val flight = if (rs.getObject("flight_id") != null) {
                Flight(
                    flightType = FlightType.valueOf(rs.getString("flight_type")),
                    distance = rs.getInt("distance"),
                    duration = rs.getInt("duration"),
                    success = rs.getBoolean("success")
                )
            } else null
            
            MatchRound(
                round = rs.getInt("round_number"),
                characterId = rs.getLong("character_id"),
                healthDelta = rs.getInt("health_delta"),
                staminaDelta = rs.getInt("stamina_delta"),
                manaDelta = rs.getInt("mana_delta"),
                flight = flight
            )
        }, matchId)
    }
}
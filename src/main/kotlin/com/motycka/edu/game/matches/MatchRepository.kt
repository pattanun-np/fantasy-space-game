package com.motycka.edu.game.matches

import com.motycka.edu.game.matches.model.Match
import com.motycka.edu.game.matches.model.MatchCharacter
import com.motycka.edu.game.matches.model.MatchRound
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.sql.SQLException

private val logger = KotlinLogging.logger {}

@Repository
class MatchRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    fun getAllMatches(): List<Match> {
        logger.debug { "Getting all matches" }
        val matches = mutableMapOf<Long, Match>()
        
        // Query to get match and character data
        jdbcTemplate.query(
            """
            SELECT m.id, m.challenger_id, m.opponent_id, m.match_outcome, m.challenger_xp, m.opponent_xp,
                   c1.name as challenger_name, c1.class as challenger_class, c1.level as challenger_level, c1.experience as challenger_experience,
                   c2.name as opponent_name, c2.class as opponent_class, c2.level as opponent_level, c2.experience as opponent_experience
            FROM match m
            JOIN character c1 ON m.challenger_id = c1.id
            JOIN character c2 ON m.opponent_id = c2.id
            ORDER BY m.id
            """.trimIndent()
        ) { rs, _ ->
            val matchId = rs.getLong("id")
            
            if (!matches.containsKey(matchId)) {
                val challengerId = rs.getLong("challenger_id")
                val opponentId = rs.getLong("opponent_id")
                val matchOutcome = rs.getString("match_outcome")
                val challengerXp = rs.getInt("challenger_xp")
                val opponentXp = rs.getInt("opponent_xp")
                
                val challenger = MatchCharacter(
                    id = challengerId,
                    name = rs.getString("challenger_name"),
                    characterClass = rs.getString("challenger_class"),
                    level = rs.getString("challenger_level"),
                    experienceTotal = rs.getInt("challenger_experience"),
                    experienceGained = challengerXp,
                    isVictor = matchOutcome == "CHALLENGER_WIN"
                )
                
                val opponent = MatchCharacter(
                    id = opponentId,
                    name = rs.getString("opponent_name"),
                    characterClass = rs.getString("opponent_class"),
                    level = rs.getString("opponent_level"),
                    experienceTotal = rs.getInt("opponent_experience"),
                    experienceGained = opponentXp,
                    isVictor = matchOutcome == "OPPONENT_WIN"
                )
                
                matches[matchId] = Match(
                    id = matchId,
                    challenger = challenger,
                    opponent = opponent,
                    rounds = mutableListOf()
                )
            }
            
            matchId
        }


        
        
       
        
        return matches.values.toList()
    }
    
   
}
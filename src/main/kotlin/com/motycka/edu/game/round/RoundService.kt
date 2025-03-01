package com.pattnun.game.matches

import com.pattnun.game.matches.model.Round
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

interface InterfaceRoundService {
    fun getRoundsByMatchId(matchId: Long): List<Round>
    fun saveRound(round: Round): Long
    fun saveRounds(rounds: List<Round>): List<Long>
    fun deleteRoundsByMatchId(matchId: Long)
}

@Service
class RoundService(private val roundRepository: RoundRepository) : InterfaceRoundService {
    
    override fun getRoundsByMatchId(matchId: Long): List<Round> {
        logger.debug { "Getting rounds for match $matchId" }
        return roundRepository.getRoundsByMatchId(matchId)
    }
    
    override fun saveRound(round: Round): Long {
        logger.debug { "Saving round ${round.roundNumber} for match ${round.matchId}" }
        return roundRepository.saveRound(round)
    }
    
    @Transactional
    override fun saveRounds(rounds: List<Round>): List<Long> {
        logger.debug { "Saving ${rounds.size} rounds" }
        return rounds.map { saveRound(it) }
    }
    
    override fun deleteRoundsByMatchId(matchId: Long) {
        logger.debug { "Deleting rounds for match $matchId" }
        roundRepository.deleteRoundsByMatchId(matchId)
    }
} 
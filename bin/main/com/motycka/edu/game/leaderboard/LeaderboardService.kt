package com.motycka.edu.game.leaderboard

import com.motycka.edu.game.leaderboard.model.LeaderboardEntry
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

interface InterfaceLeaderboardService {
    fun getLeaderboard(characterClass: String? = null): List<LeaderboardEntry>
    fun updateLeaderboardForMatch(challengerId: Long, opponentId: Long, challengerWon: Boolean)
}

@Service
class LeaderboardService(
    private val leaderboardRepository: LeaderboardRepository
) : InterfaceLeaderboardService {

    override fun getLeaderboard(characterClass: String?): List<LeaderboardEntry> {
        logger.info { "Getting leaderboard with filter: $characterClass" }
        return leaderboardRepository.getLeaderboard(characterClass)
    }
    
    override fun updateLeaderboardForMatch(challengerId: Long, opponentId: Long, challengerWon: Boolean) {
        logger.info { "Updating leaderboard for match: challenger=$challengerId, opponent=$opponentId, challengerWon=$challengerWon" }
        
        if (challengerWon) {
            // Challenger won
            leaderboardRepository.updateLeaderboard(challengerId, isWin = true, isLoss = false, isDraw = false)
            leaderboardRepository.updateLeaderboard(opponentId, isWin = false, isLoss = true, isDraw = false)
        } else {
            // Opponent won
            leaderboardRepository.updateLeaderboard(challengerId, isWin = false, isLoss = true, isDraw = false)
            leaderboardRepository.updateLeaderboard(opponentId, isWin = true, isLoss = false, isDraw = false)
        }
    }
} 
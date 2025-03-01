package com.motycka.edu.game.leaderboard

import com.motycka.edu.game.leaderboard.model.LeaderboardEntry
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/leaderboards")
class LeaderboardController(private val leaderboardService: InterfaceLeaderboardService) {

    @GetMapping
    fun getLeaderboard(@RequestParam(required = false) characterClass: String?): ResponseEntity<List<LeaderboardEntry>> {
        logger.info { "Getting leaderboard with filter: $characterClass" }
        val leaderboard = leaderboardService.getLeaderboard(characterClass)
        return ResponseEntity.ok(leaderboard)
    }
} 
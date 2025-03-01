package com.pattnun.game.matches

import com.pattnun.game.matches.model.Match
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class MatchRequest(
    val challengerId: Long,
    val opponentId: Long,
    val rounds: Int = 3
)

@RestController
@RequestMapping("/api/matches")
class MatchController(
    private val matchService: InterfaceMatchService
) {
    @GetMapping
    fun getMatches(): List<Match> {
        return matchService.getAllMatches()
    }


}
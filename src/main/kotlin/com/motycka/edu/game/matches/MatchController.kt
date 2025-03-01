package com.motycka.edu.game.matches

import com.motycka.edu.game.matches.model.Match
import com.motycka.edu.game.matches.rest.MatchRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
package com.motycka.edu.game.matches

import com.motycka.edu.game.matches.model.Match
import com.motycka.edu.game.matches.rest.MatchRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/matches")
class MatchController(private val matchService: InterfaceMatchService) {

    @GetMapping
    fun getMatches(): ResponseEntity<List<Match>> {
        logger.info { "Getting all matches" }
        val matches = matchService.getAllMatches()
        return ResponseEntity.ok(matches)
    }

    @PostMapping
    fun createMatch(@RequestBody matchRequest: MatchRequest): ResponseEntity<Match> {
        logger.info { "Creating match: $matchRequest" }
        val match = matchService.createMatch(matchRequest)
        return ResponseEntity.ok(match)
    }
} 
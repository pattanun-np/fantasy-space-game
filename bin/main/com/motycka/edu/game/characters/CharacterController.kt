package com.motycka.edu.game.characters

import com.motycka.edu.game.characters.model.CharacterClass
import com.motycka.edu.game.characters.rest.CharacterRegistrationRequest
import com.motycka.edu.game.characters.rest.CharacterResponse
import com.motycka.edu.game.characters.rest.toCharacter
import com.motycka.edu.game.characters.rest.toCharacterResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import com.motycka.edu.game.characters.model.CharacterLevel

/**
 * Controller handling character-related operations through HTTP endpoints.
 */
@RestController
@RequestMapping("/api/characters")
class CharacterController(
    private val characterService: InterfaceCharacterService
) {

    @GetMapping
    fun getCharacters(
        @RequestParam("class", required = false) characterClass: String?,
        @RequestParam("name", required = false) name: String?
    ): List<CharacterResponse> {
        return characterService.getCharacters(
            characterClass = parseCharacterClass(characterClass)?.name,
            name = name
        ).map { it.toCharacterResponse()!! }
    }

    @GetMapping("/challengers")
    fun getChallengers(
        @RequestParam("class", required = false) characterClass: String?,
        @RequestParam("name", required = false) name: String?
    ): List<CharacterResponse> {
        return characterService.getChallenger(
            characterClass = parseCharacterClass(characterClass)?.name,
            name = name
        ).map { it.toCharacterResponse()!! }
    }

    @GetMapping("/opponents")
    fun getOpponents(
        @RequestParam("class", required = false) characterClass: String?,
        @RequestParam("name", required = false) name: String?
    ): List<CharacterResponse> {
        return characterService.getOpponents(
            characterClass = parseCharacterClass(characterClass)?.name,
            name = name
        ).map { it.toCharacterResponse()!! }
    }
    
    @GetMapping("/debug")
    fun debugCharacters(): Map<String, Any> {
        val allCharacters = characterService.getCharacters(null, null)
        val challengers = characterService.getChallenger(null, null)
        val opponents = characterService.getOpponents(null, null)
        
        return mapOf(
            "totalCharacters" to allCharacters.size,
            "challengers" to challengers.size,
            "opponents" to opponents.size,
            "allCharacterIds" to allCharacters.map { it.id },
            "challengerIds" to challengers.map { it.id },
            "opponentIds" to opponents.map { it.id }
        )
    }

    @PostMapping
    fun createCharacter(
        @RequestBody characterRequest: CharacterRegistrationRequest
    ): ResponseEntity<CharacterResponse> {
        val createdCharacter = characterService.createCharacter(characterRequest.toCharacter())
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCharacter.toCharacterResponse()!!)
    }

    /**
     * Helper method to safely parse character class from string
     */
    private fun parseCharacterClass(characterClass: String?): CharacterClass? {
        return characterClass?.let {
            try {
                CharacterClass.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    // Helper method to determine if a character should level up
    private fun shouldCharacterLevelUp(level: Int, experience: Int): Boolean {
        return when (level) {
            1 -> experience >= 300
            2 -> experience >= 600
            3 -> experience >= 900
            4 -> experience >= 1200
            5 -> experience >= 1500
            6 -> experience >= 1800
            7 -> experience >= 2100
            8 -> experience >= 2400
            9 -> experience >= 2700
            else -> false
        }
    }
}


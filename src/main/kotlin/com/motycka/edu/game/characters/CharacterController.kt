package com.motycka.edu.game.characters

import com.motycka.edu.game.characters.model.CharacterClass
import com.motycka.edu.game.characters.rest.CharacterRegistrationRequest
import com.motycka.edu.game.characters.rest.CharacterResponse
import com.motycka.edu.game.characters.rest.toCharacter
import com.motycka.edu.game.characters.rest.toCharacterResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
            name = name ?: ""
        ).map { it.toCharacterResponse()!! }
    }

    @GetMapping("/opponents")
    fun getOpponents(
        @RequestParam("class", required = false) characterClass: String?,
        @RequestParam("name", required = false) name: String?
    ): List<CharacterResponse>? {
        return name?.let {
            characterService.getOpponents(
                characterClass = parseCharacterClass(characterClass)?.name,
                it
            ).map { it.toCharacterResponse()!! }
        }
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
}
package com.motycka.edu.game.characters

import com.motycka.edu.game.account.InterfaceAccountService
import com.motycka.edu.game.characters.model.Character
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

val logger = KotlinLogging.logger {}

/**
 * Service interface for character-related operations.
 */
interface InterfaceCharacterService {
    fun getCharacters(characterClass: String?, name: String?): List<Character>
    fun getChallenger(
        characterClass: String?,
        name: String?
    ): List<Character>

    fun getOpponents(
        characterClass: String?,
        name: String?
    ): List<Character>

    fun getCharacterById(id: Long): Character
    fun createCharacter(character: Character): Character
    fun updateExperience(characterId: Long, newExperience: Int): Character
    fun isCharacterOwner(characterId: Int): Boolean
}

@Service
class CharacterService(
    private val characterRepository: CharacterRepository,
    private val accountService: InterfaceAccountService
) : InterfaceCharacterService {

    override fun getCharacters(characterClass: String?, name: String?): List<Character> {
        logger.debug { "Fetching all characters with class: $characterClass and name: $name" }
        val accountId = accountService.getCurrentAccountId()
        return fetchAndMarkOwnership(characterRepository.selectAll(characterClass, name, null), accountId)
    }

    override fun getChallenger(
        characterClass: String?,
        name: String?
    ): List<Character> {
        val accountId = accountService.getCurrentAccountId()
        logger.debug { "Fetching challenger characters for account: $accountId with class: $characterClass and name: $name" }
        return fetchAndMarkOwnership(
            characterRepository.selectAll(characterClass, name, accountId),
            accountId
        )
    }

    override fun getOpponents(
        characterClass: String?,
        name: String?
    ): List<Character> {
        val accountId = accountService.getCurrentAccountId()
        logger.debug { "Fetching opponent characters for account: $accountId with class: $characterClass and name: $name" }
        
        // Get opponents (characters not owned by the current account)
        val opponents = characterRepository.selectOpponent(characterClass, name, accountId)
        
        // Mark ownership (should all be false for opponents)
        return fetchAndMarkOwnership(opponents, accountId)
    }

    override fun getCharacterById(id: Long): Character {
        logger.debug { "Fetching character by ID: $id" }
        val character = characterRepository.selectById(id)
            ?: throw NoSuchElementException("Character with ID $id not found.")
        
        // Mark ownership based on current account
        val accountId = accountService.getCurrentAccountId()
        character.isOwner = character.accountId == accountId
        
        return character
    }

    override fun createCharacter(character: Character): Character {
        logger.debug { "Creating new character: $character" }

        character.apply {
            experience = 0
            level = 1
        }

        val accountId = accountService.getCurrentAccountId()
        return characterRepository.insertCharacter(character, accountId)
            ?: throw IllegalStateException(CREATE_ERROR)
    }

    override fun updateExperience(characterId: Long, newExperience: Int): Character {
        logger.debug { "Updating experience for character $characterId to $newExperience" }
        
        val character = getCharacterById(characterId)
        
        // Calculate new level based on experience
        val newLevel = calculateLevel(newExperience)
        
        // Update character in repository
        return characterRepository.updateExperience(characterId, newExperience, newLevel)
            ?: throw IllegalStateException("Failed to update character experience")
    }
    
    private fun calculateLevel(experience: Int): Int {
        // Simple level calculation: 1 level per 1000 experience points, starting at level 1
        return (experience / 1000) + 1
    }

    private fun validatePointDistribution(character: Character) {
        // Base stats validation
        if (character.health <= 0 || character.attack <= 0) {
            throw IllegalArgumentException("Health and attack power must be positive")
        }

        // Class-specific validation
        when (character.characterClass) {
            "WARRIOR" -> {
                if (character.stamina <= 0 || character.defense <= 0) {
                    throw IllegalArgumentException("Warriors must have positive stamina and defense power")
                }
                if (character.mana > 0 || character.healing > 0) {
                    throw IllegalArgumentException("Warriors cannot have mana or healing power")
                }
            }
            "SORCERER" -> {
                if (character.mana <= 0 || character.healing <= 0) {
                    throw IllegalArgumentException("Sorcerers must have positive mana and healing power")
                }
                if (character.stamina > 0 || character.defense > 0) {
                    throw IllegalArgumentException("Sorcerers cannot have stamina or defense power")
                }
            }
            else -> throw IllegalArgumentException("Invalid character class")
        }
    }

    private fun validateClassSpecificAttributes(character: Character) {
        when (character.characterClass) {
            "WARRIOR" -> {
                if (character.stamina <= 0 || character.defense <= 0) {
                    throw IllegalArgumentException("Warriors must specify stamina and defense power")
                }
            }
            "SORCERER" -> {
                if (character.mana <= 0 || character.healing <= 0) {
                    throw IllegalArgumentException("Sorcerers must specify mana and healing power")
                }
            }
        }
    }

    /**
     * Helper function to set ownership on characters.
     */
    private fun fetchAndMarkOwnership(characters: List<Character>, accountId: Long): List<Character> {
        return characters.map { it.apply { isOwner = it.accountId == accountId } }
    }

    override fun isCharacterOwner(characterId: Int): Boolean {
        val character = getCharacterById(characterId.toLong())
        val accountId = accountService.getCurrentAccountId()
        logger.debug { "Checking if character $characterId is owned by account $accountId" }
        return character.accountId == accountId
    }

    companion object {
        private const val CREATE_ERROR = "Character creation failed."
    }
}
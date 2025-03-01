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
        name: String
    ): List<Character>

    fun getOpponents(
        characterClass: String?,
        name: String
    ): List<Character>

    fun getCharacterById(id: Long): Character
    fun createCharacter(character: Character): Character
}

@Service
class CharacterService(
    private val characterRepository: CharacterRepository,
    private val accountService: InterfaceAccountService
) : InterfaceCharacterService {

    override fun getCharacters(characterClass: String?, name: String?): List<Character> {
        logger.debug { "Fetching characters with class: $characterClass and name: $name" }
        val accountId = accountService.getCurrentAccountId()
        return fetchAndMarkOwnership(characterRepository.selectAll(characterClass, name, null), accountId)
    }

    override fun getChallenger(
        characterClass: String?,
        name: String
    ): List<Character> = getCharactersByType(characterClass, name, "Challenger")

    override fun getOpponents(
        characterClass: String?,
        name: String
    ): List<Character> = getCharactersByType(characterClass, name, "Opponents")

    override fun getCharacterById(id: Long): Character {
        logger.debug { "Fetching character by ID: $id" }
        return characterRepository.selectById(id)
            ?: throw NoSuchElementException("Character with ID $id not found.")
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

    /**
     * Helper function to fetch characters for the current account and mark ownership.
     */
    private fun getCharactersByType(characterClass: String?, name: String, type: String): List<Character> {
        val accountId = accountService.getCurrentAccountId()
        logger.debug { "Fetching $type characters for account: $accountId" }
        return if (type == "Challenger") {
            fetchAndMarkOwnership(
                characterRepository.selectAll(
                    characterClass.toString(), name,
                    accountId
                ), accountId
            )

        } else {
            fetchAndMarkOwnership(
                characterRepository.selectOpponent(
                    characterClass.toString(), name, accountId
                ), accountId
            )
        }

    }

    /**
     * Helper function to set ownership on characters.
     */
    private fun fetchAndMarkOwnership(characters: List<Character>, accountId: Long): List<Character> {
        return characters.map { it.apply { isOwner = it.accountId == accountId } }
    }

    companion object {
        private const val CREATE_ERROR = "Character creation failed."
    }
}
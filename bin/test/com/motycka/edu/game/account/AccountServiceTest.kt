package com.motycka.edu.game.account

import com.motycka.edu.game.account.AccountFixtures.DEVELOPER
import com.motycka.edu.game.account.AccountFixtures.UNKNOWN
import com.motycka.edu.game.config.SecurityContextHolderHelper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser

class AccountServiceTest {

    private val accountRepository: AccountRepository = mockk()
    private val accountService: AccountService = AccountService(
        accountRepository = accountRepository
    )

    @BeforeEach
    fun setUp() {
        SecurityContextHolderHelper.setSecurityContext(DEVELOPER)
        every { accountRepository.selectByUsername(DEVELOPER.username) } returns DEVELOPER
        every { accountRepository.selectByUsername(UNKNOWN) } returns null
    }

    @Test
    fun `getByUsername should return account when found`() {
        val result = accountService.getByUsername(DEVELOPER.username)
        assertEquals(DEVELOPER, result)

        verify { accountRepository.selectByUsername(DEVELOPER.username) }
    }

    @Test
    fun `getByUsername should return null when not found`() {
        val result = accountService.getByUsername(UNKNOWN)
        assertNull(result)

        verify { accountRepository.selectByUsername(UNKNOWN) }
    }

    @Test
    fun `createAccount should return created account`() {
        every { accountRepository.insertAccount(DEVELOPER) } returns DEVELOPER

        val result = accountService.createAccount(DEVELOPER)
        assertEquals(DEVELOPER, result)

        verify { accountRepository.insertAccount(DEVELOPER) }
    }

    @Test
    @WithMockUser(username = "developer")
    fun `getCurrentAccountId should return account id`() {
        every { accountRepository.selectByUsername(DEVELOPER.username) } returns DEVELOPER

        val result = accountService.getCurrentAccountId()
        assertEquals(DEVELOPER.id, result)

        verify { accountRepository.selectByUsername(DEVELOPER.username) }
    }
}

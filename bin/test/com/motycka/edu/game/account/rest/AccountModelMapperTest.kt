package com.motycka.edu.game.account.rest

import com.motycka.edu.game.account.model.Account
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AccountModelMapperTest {

    @Test
    fun `should map AccountRegistrationRequest to Account`() {
        val request = AccountRegistrationRequest(
            name = "John Doe",
            username = "johndoe",
            password = "password123"
        )

        val account = request.toAccount()

        assertEquals(
            Account(
                id = null,
                name = request.name,
                username = request.username,
                password = request.password
            ),
            account
        )
    }

    @Test
    fun `should map Account to AccountResponse`() {
        val account = Account(
            id = 1L,
            name = "John Doe",
            username = "johndoe",
            password = "***" // should mask the password
        )

        val response = account.toAccountResponse()

        assertEquals(
            AccountResponse(
                id = account.id!!,
                name = account.name,
                username = account.username,
                password = account.password
            ),
            response
        )
    }
}

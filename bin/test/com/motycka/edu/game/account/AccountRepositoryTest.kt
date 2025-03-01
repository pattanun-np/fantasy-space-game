package com.motycka.edu.game.account

import com.motycka.edu.game.account.AccountFixtures.DEVELOPER
import com.motycka.edu.game.account.AccountFixtures.TESTER
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import javax.sql.DataSource

@JdbcTest
@ContextConfiguration(classes = [AccountRepository::class])
class AccountRepositoryTest {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var dataSource: DataSource

    @BeforeEach
    fun setUp() {
        // insert test data
        jdbcTemplate.update(
            "INSERT INTO account (name, username, password) VALUES (?, ?, ?)",
            AccountFixtures.DEVELOPER.name,
            DEVELOPER.username,
            DEVELOPER.password
        )
    }

    @Test
    fun `insertAccount should return inserted account`() {
        val result = accountRepository.insertAccount(TESTER)

        assertNotNull(result)
        assertNotNull(result?.id)
        assertEquals(TESTER.copy(id = result?.id), result)
    }

    @Test
    fun `selectByUsername should return account when found`() {
        val result = accountRepository.selectByUsername(DEVELOPER.username)
        assertEquals(DEVELOPER.copy(result?.id), result)
        assertNotNull(result?.id)
        assertEquals(DEVELOPER.copy(result?.id), result)
    }

    @Test
    fun `selectByUsername should return null when not found`() {
        val result = accountRepository.selectByUsername("unknown")
        assertNull(result)
    }

}

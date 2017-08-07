package net.donething.autosign.models

import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<User, Long> {
    fun findById(id: Long): User?
    fun findByUsernameIgnoreCase(username: String): User?
    fun findByEmailIgnoreCase(email: String): User?
}
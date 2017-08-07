package net.donething.autosign.models

import org.springframework.data.repository.CrudRepository

interface Wo17Repository : CrudRepository<Wo17, Long> {
    fun findById(id: Long): Wo17?
    fun findByUid(uid: Long): List<Wo17>?
    fun findByPhone(phone: String): Wo17?
}
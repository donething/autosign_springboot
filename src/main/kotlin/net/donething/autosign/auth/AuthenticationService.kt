package net.donething.autosign.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import net.donething.autosign.models.User
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AuthenticationService {
    fun getToken(user: User): String {
        var token = ""
        try {
            token = JWT.create()
                    .withAudience(user.id.toString())
                    .sign(Algorithm.HMAC256(user.salt))
        } catch (ex: Exception) {
            logger.error("Token生成错误：" + ex.message)
        }
        return token
    }

    private val logger = LoggerFactory.getLogger(this.javaClass)
}
package net.donething.autosign.auth

import net.donething.autosign.models.UserRepository
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.authc.SimpleAuthenticationInfo
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.PrincipalCollection
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

class MyShiroRealm : AuthorizingRealm() {
    override fun doGetAuthenticationInfo(token: AuthenticationToken?): AuthenticationInfo? {
        val username = token?.principal
        val user = repository?.findByUsername(username.toString())
        user ?: return null
        val authenticationInfo = SimpleAuthenticationInfo(user, user.password, name)
        return authenticationInfo
    }

    override fun doGetAuthorizationInfo(principals: PrincipalCollection?): AuthorizationInfo? {
        return null
    }

    private @Autowired val repository: UserRepository? = null
    private val logger = LoggerFactory.getLogger(this.javaClass)
}
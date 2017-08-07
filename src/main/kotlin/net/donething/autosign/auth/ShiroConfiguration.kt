package net.donething.autosign.auth

import org.apache.shiro.cache.ehcache.EhCacheManager
import org.apache.shiro.codec.Base64
import org.apache.shiro.mgt.SecurityManager
import org.apache.shiro.spring.LifecycleBeanPostProcessor
import org.apache.shiro.spring.web.ShiroFilterFactoryBean
import org.apache.shiro.web.mgt.CookieRememberMeManager
import org.apache.shiro.web.mgt.DefaultWebSecurityManager
import org.apache.shiro.web.servlet.SimpleCookie
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class ShiroConfiguration {
    @Bean
    fun shiroFilter(securityManager: SecurityManager): ShiroFilterFactoryBean {
        val shiroFilterFactoryBean = ShiroFilterFactoryBean()
        // 设置SecurityManager
        shiroFilterFactoryBean.securityManager = securityManager
        // 设置拦截器
        val filterChainDefinitionMap = LinkedHashMap<String, String>()
        filterChainDefinitionMap.put("/public/logout", "logout")
        filterChainDefinitionMap.put("/", "anon")
        filterChainDefinitionMap.put("/favicon.ico", "anon")
        filterChainDefinitionMap.put("/api/**", "anon")
        filterChainDefinitionMap.put("/css/**", "anon")
        filterChainDefinitionMap.put("/js/**", "anon")
        filterChainDefinitionMap.put("/public/**", "anon")
        filterChainDefinitionMap.put("/**", "authc")

        // 设置过滤器
        shiroFilterFactoryBean.loginUrl = "/public/login"
        shiroFilterFactoryBean.successUrl = "/"
        shiroFilterFactoryBean.unauthorizedUrl = "/public/403"

        shiroFilterFactoryBean.filterChainDefinitionMap = filterChainDefinitionMap

        return shiroFilterFactoryBean
    }

    @Bean
    fun securityManager(): SecurityManager {
        val securityManager = DefaultWebSecurityManager()
        securityManager.setRealm(myShiroRealm())
        securityManager.cacheManager = ehCacheManager()
        securityManager.rememberMeManager = rememberManager()
        return securityManager
    }

    @Bean
    fun ehCacheManager(): EhCacheManager {
        val cacheManager = EhCacheManager()
        return cacheManager
    }

    @Bean
    fun rememberManager(): CookieRememberMeManager {
        val cookieRememberMeManager = CookieRememberMeManager()
        cookieRememberMeManager.cookie = rememberMeCookie()
        /**
         * rememberme cookie 加密的密钥 建议每个项目都不一样 默认AES算法 密钥长度（128 256 512 位），通过以下代码可以获取：
         *
        val keygen = KeyGenerator.getInstance("AES")
        val key = keygen.generateKey()
        println("key:" + Base64.encodeToString(key.encoded))
         */
        cookieRememberMeManager.cipherKey = Base64.decode("R7jgG4cX8s7elosoU7JSkg==")
        return cookieRememberMeManager
    }

    @Bean
    fun rememberMeCookie(): SimpleCookie {
        val simpleCookie = SimpleCookie("rememberMe")
        simpleCookie.isHttpOnly = true
        // simpleCookie.isSecure = true
        simpleCookie.maxAge = 864000
        return simpleCookie
    }

    @Bean
    fun myShiroRealm(): MyShiroRealm {
        return MyShiroRealm()
    }

    @Bean(name = arrayOf("lifecycleBeanPostProcessor"))
    fun lifecycleBeanPostProcessor(): LifecycleBeanPostProcessor {
        return LifecycleBeanPostProcessor()
    }
}

grails {
    redis {
        poolConfig {
            // pool specific tweaks here
            // for parms see https://github.com/xetorthio/jedis/blob/master/src/main/java/redis/clients/jedis/JedisPoolConfig.java
            // numTestsPerEvictionRun = 4
        }
        port = 6379
        host = "localhost"
    }
}


log4j = {
    error 'org.codehaus.groovy.grails.web.servlet',  //  controllers
          'org.codehaus.groovy.grails.web.pages', //  GSP
          'org.codehaus.groovy.grails.web.sitemesh', //  layouts
          'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
          'org.codehaus.groovy.grails.web.mapping', // URL mapping
          'org.codehaus.groovy.grails.commons', // core / classloading
          'org.codehaus.groovy.grails.plugins', // plugins
          'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
          'org.springframework',
          'org.hibernate',
          'net.sf.ehcache.hibernate'

    warn 'org.mortbay.log'
    debug 'grails.plugin.redis',
          'com.example'
}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"

/* Copyright (C) 2011 SpringSource
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import grails.plugin.redis.RedisService
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.Protocol

class RedisGrailsPlugin {

    def version = "1.3"
    def grailsVersion = "2.0.0 > *"
    def author = "Ted Naleid"
    def authorEmail = "contact@naleid.com"
    def title = "Redis Plugin"

    def description = '''The Redis plugin provides integration with a Redis datastore. Redis is a lightning fast 'data structure server'.  The plugin enables a number of memoization techniques to cache results from complex operations in Redis.
'''
    def issueManagement = [system: 'github', url: 'https://github.com/grails-plugins/grails-redis/issues']

    def license = "APACHE"
    def developers = [
            [name: "Burt Beckwith"],
            [name: "Brian Coles"],
            [name: "Michael Cameron"],
            [name: "Christian Oestreich"]
    ]

    def pluginExcludes = [
            "codenarc.properties",
            "grails-app/conf/DataSource.groovy",
            "grails-app/conf/redis-codenarc.groovy",
            "grails-app/views/**",
            "grails-app/domain/**",
            "grails-app/services/test/**",
            "test/**"

    ]

    def scm = [url: "https://github.com/grails-plugins/grails-redis"]

    def documentation = "http://grails.org/plugin/redis"

    def doWithSpring = {
        def redisConfigMap = application.config.grails.redis ?: [:]
        configureService.delegate = delegate
        configureService(redisConfigMap, "")
        redisConfigMap?.connections?.each { connection ->
            configureService(connection.value, connection?.key?.capitalize())
        }
    }

    /**
     * delegate to wire up the required beans.
     */
    def configureService = {redisConfigMap, key ->
        def poolBean = "redisPoolConfig${key}"
        "${poolBean}"(JedisPoolConfig) {
            // used to set arbitrary config values without calling all of them out here or requiring any of them
            // any property that can be set on RedisPoolConfig can be set here
            redisConfigMap.poolConfig.each { configkey, value ->
                delegate.setProperty(configkey, value)
            }
        }

        def host = redisConfigMap?.host ?: 'localhost'
        def port = redisConfigMap?.port ?: Protocol.DEFAULT_PORT
        def timeout = redisConfigMap?.timeout ?: Protocol.DEFAULT_TIMEOUT
        def password = redisConfigMap?.password ?: null
        def database = redisConfigMap?.database ?: Protocol.DEFAULT_DATABASE

        "redisPool${key}"(JedisPool, ref(poolBean), host, port, timeout, password, database) { bean ->
            bean.destroyMethod = 'destroy'
        }

        //only wire up additional services when key provided for multiple connection support
        if(key) {
            "redisService${key}"(RedisService) {
                redisPool = ref("redisPool${key}")
            }
        }

    }
}

package com.example.demo.redis.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.Protocol;

public abstract class JedisPoolConfiguration {
	
	private JedisPoolConfig poolConfig = null;
	private JedisSentinelPool sentinelPool = null;
	
	public JedisPoolConfiguration(RedisConfigProperties redisConfigProperties) {
		Integer maxTotal = Integer.parseInt(StringUtils.defaultIfEmpty(redisConfigProperties.redisMaxPoolTotal, "10"));
		Integer maxIdle = Integer.parseInt(StringUtils.defaultIfEmpty(redisConfigProperties.redisMaxPoolIdle, "10"));
		Integer minIdle = Integer.parseInt(StringUtils.defaultIfEmpty(redisConfigProperties.redisMinPoolIdle, "10"));
		Integer minEvictableIdleTimeMillis = Integer.parseInt(StringUtils.defaultIfEmpty(redisConfigProperties.redisMinEvictableIdleTimeMillis, "60000"));
		Integer timeBetweenEvictionRunsMillis = Integer.parseInt(StringUtils.defaultIfEmpty(redisConfigProperties.redisTimeBetweenEvictionRunsMillis, "30000"));
		Integer numTestsPerEvictionRun = Integer.parseInt(StringUtils.defaultIfEmpty(redisConfigProperties.redisNumTestsPerEvictionRun, "3"));
		Integer maxWaitMillis = Integer.parseInt(StringUtils.defaultIfEmpty(redisConfigProperties.redisMaxWaitMillis, "10000"));
		String sentinelAddress = redisConfigProperties.redisSentinelAddress;
		String masterName = redisConfigProperties.redisMasterName;
		String password = redisConfigProperties.redisPassword;
		String sentinelPassword = redisConfigProperties.redisSentinelPassword;
		
		buildPoolConfig(maxTotal, maxIdle, minIdle, minEvictableIdleTimeMillis, timeBetweenEvictionRunsMillis, numTestsPerEvictionRun, maxWaitMillis);
		initSentinelPool(sentinelAddress, masterName, password, sentinelPassword);
	}

	protected Jedis getJedisConnectionPool() {
		if (null == this.sentinelPool) {
			return null;
		}
		return this.sentinelPool.getResource();
	}

	private void initSentinelPool(String sentinelAddress, String masterName, String password, String sentinelPassword) {
		if (null == this.sentinelPool) {
			this.sentinelPool = new JedisSentinelPool(masterName, getSentinelsAddress(sentinelAddress), poolConfig,
					Protocol.DEFAULT_TIMEOUT, Protocol.DEFAULT_TIMEOUT, password, Protocol.DEFAULT_DATABASE, null,
					Protocol.DEFAULT_TIMEOUT, Protocol.DEFAULT_TIMEOUT, sentinelPassword, null);		
		}
	}

	private Set<String> getSentinelsAddress(String sentinelStr) {
		return new HashSet<>(Arrays.asList(StringUtils.split(sentinelStr, ",")));
	}

	private void buildPoolConfig(Integer maxTotal, Integer maxIdle, Integer minIdle, Integer minEvictableIdleTimeMillis, Integer timeBetweenEvictionRunsMillis, Integer numTestsPerEvictionRun, Integer maxWaitMillis) {
		if (null == this.poolConfig) {
			if (null == maxTotal) maxTotal = 10;
			if (null == maxIdle) maxIdle = 10;
			if (null == minIdle) minIdle = 10;
			if (null == minEvictableIdleTimeMillis) minEvictableIdleTimeMillis = 60000;
			if (null == timeBetweenEvictionRunsMillis) timeBetweenEvictionRunsMillis = 30000;
			if (null == numTestsPerEvictionRun) numTestsPerEvictionRun = 3;
			if (null == maxWaitMillis) maxWaitMillis = 10000;
	
			this.poolConfig = new JedisPoolConfig();
			this.poolConfig.setMaxTotal(maxTotal);
			this.poolConfig.setMaxIdle(maxIdle);
			this.poolConfig.setMinIdle(minIdle);
			this.poolConfig.setTestOnBorrow(true);
			this.poolConfig.setTestOnReturn(false);
			this.poolConfig.setTestWhileIdle(true);
			this.poolConfig.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
			this.poolConfig.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
			this.poolConfig.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
			this.poolConfig.setBlockWhenExhausted(true);
			this.poolConfig.setMaxWaitMillis(maxWaitMillis);
		}
	}
}

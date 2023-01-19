package com.example.demo.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;

import lombok.Data;

@ConfigurationProperties(prefix = "config.redis")
@ConfigurationPropertiesScan 
@Data
@Component
public class RedisConfigProperties {
	
	public int index;
	public String redisMaxPoolTotal;
	public String redisMaxPoolIdle;
	public String redisMinPoolIdle;
	public String redisMinEvictableIdleTimeMillis;
	public String redisTimeBetweenEvictionRunsMillis;
	public String redisNumTestsPerEvictionRun;
	public String redisSentinelAddress;
	public String redisMasterName;
	public String redisPassword;
	public String redisSentinelPassword;
	public String redisMaxWaitMillis;

}

package com.example.demo.mq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;

import lombok.Data;

@ConfigurationProperties(prefix = "config.ibm.mq")
@ConfigurationPropertiesScan 
@Data
@Component
public class MQConfigProperties {
	
	private String hostName;
	private int port;
	private String queueManagerName;
	private String channelName;
	private String username;
	private String password;
	private boolean cacheProducers;
	private int sessionCacheSize;
	private String queueDestinationOutput;

}

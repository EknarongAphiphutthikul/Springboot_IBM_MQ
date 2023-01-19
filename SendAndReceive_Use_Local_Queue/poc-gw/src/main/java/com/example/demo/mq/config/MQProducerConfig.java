package com.example.demo.mq.config;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;

import com.example.demo.redis.config.JedisManager;
import com.ibm.mq.jms.MQQueueConnectionFactory;

@Configuration
public class MQProducerConfig {

	Logger logger = LoggerFactory.getLogger(MQProducerConfig.class);

	@Bean
	public MQQueueConnectionFactory MQConnectionFactory(MQConfigProperties configProperties) throws JMSException {
		MQQueueConnectionFactory connectionFactory = new MQQueueConnectionFactory();
		connectionFactory.setHostName(configProperties.getHostName());
		connectionFactory.setPort(configProperties.getPort());
		connectionFactory.setQueueManager(configProperties.getQueueManagerName());
		connectionFactory.setChannel(configProperties.getChannelName());
		connectionFactory.setTransportType(1);
		return connectionFactory;
	}

	@Bean
	public CachingConnectionFactory cachingJmsConnectionFactory(MQConfigProperties configProperties,
			MQQueueConnectionFactory wrappedConnectionFactory) {
		UserCredentialsConnectionFactoryAdapter userCredentials = new UserCredentialsConnectionFactoryAdapter();
		userCredentials.setUsername(configProperties.getUsername());
		userCredentials.setPassword(configProperties.getPassword());
		userCredentials.setTargetConnectionFactory(wrappedConnectionFactory);

		CachingConnectionFactory connectionFactory = new CachingConnectionFactory(userCredentials);
		connectionFactory.setCacheProducers(configProperties.isCacheProducers());
		connectionFactory.setSessionCacheSize(configProperties.getSessionCacheSize());
		connectionFactory.setReconnectOnException(true);
		return connectionFactory;
	}

	@Bean
	public WrapperJmsTemplate jmsTemplate(CachingConnectionFactory connectionFactory, JedisManager jedisManager, MQConfigProperties configProperties) {
		int timeout = 10000;
		WrapperJmsTemplate jmsTemplate = new WrapperJmsTemplate(connectionFactory, jedisManager, timeout + 500, configProperties.getQueueDestinationResponse());
		jmsTemplate.setReceiveTimeout(timeout);
		return jmsTemplate;
	}
}

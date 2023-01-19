package com.example.demo.mq.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jca.support.ResourceAdapterFactoryBean;
import org.springframework.jca.work.SimpleTaskWorkManager;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.endpoint.JmsMessageEndpointManager;

import com.example.demo.mq.listener.ExampleListener;
import com.example.demo.redis.config.JedisManager;
import com.ibm.mq.connector.ResourceAdapterImpl;
import com.ibm.mq.connector.inbound.ActivationSpecImpl;

@Configuration
public class MQConsumerConfig {

	Logger logger = LoggerFactory.getLogger(MQConsumerConfig.class);
	
	@Autowired
	private JmsTemplate jmsTemplate;
	
	@Bean
	public SimpleTaskWorkManager createSimpleTaskWorkManager() throws Exception { 
		return new SimpleTaskWorkManager();
	}

	@SuppressWarnings("deprecation")
	@Bean
	public ResourceAdapterImpl createResourceAdapterImpl() throws Exception {
		ResourceAdapterImpl resourceAdapter = new ResourceAdapterImpl();
		resourceAdapter.setConnectionConcurrency(5);
		resourceAdapter.setMaxConnections(10);
		resourceAdapter.setReconnectionRetryCount(5);
		resourceAdapter.setReconnectionRetryInterval(300000);
		return resourceAdapter;
	}

	@Bean
	public ResourceAdapterFactoryBean createResourceAdapterFactoryBean(SimpleTaskWorkManager workManager,
			ResourceAdapterImpl resourceAdapter) throws Exception {
		ResourceAdapterFactoryBean resourceAdapterFactoryBean = new ResourceAdapterFactoryBean();
		resourceAdapterFactoryBean.setResourceAdapter(resourceAdapter);
		resourceAdapterFactoryBean.setWorkManager(workManager);
		resourceAdapterFactoryBean.afterPropertiesSet();
		return resourceAdapterFactoryBean;
	}

	@Bean
	public ActivationSpecImpl createActivationSpecImpl(MQConfigProperties config) throws Exception {
		ActivationSpecImpl activationSpec = new ActivationSpecImpl();
		activationSpec.setDestinationType("javax.jms.Queue");
		activationSpec.setDestination(config.getQueueDestinationInput());
		activationSpec.setHostName(config.getHostName());
		activationSpec.setQueueManager(config.getQueueManagerName());
		activationSpec.setPort(config.getPort());
		activationSpec.setChannel(config.getChannelName());
		activationSpec.setTransportType("CLIENT");
		activationSpec.setUserName(config.getUsername());
		activationSpec.setPassword(config.getPassword());
		activationSpec.setMaxPoolDepth(config.getMaxPoolDepth());
		return activationSpec;
	}

	@Bean
	public JmsMessageEndpointManager createJmsMessageEndpointManager(ResourceAdapterImpl resourceAdapter,
			ActivationSpecImpl activationSpec, JedisManager jedisManager) throws Exception {
		JmsMessageEndpointManager jmsMessageEndpointManager = new JmsMessageEndpointManager();
		jmsMessageEndpointManager.setActivationSpec(activationSpec);
		jmsMessageEndpointManager.setMessageListener(new ExampleListener(jmsTemplate, jedisManager));
		jmsMessageEndpointManager.setResourceAdapter(resourceAdapter);
		jmsMessageEndpointManager.afterPropertiesSet();
		return jmsMessageEndpointManager;
	}

}

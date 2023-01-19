package com.example.demo.mq.config;

import java.util.Calendar;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.JmsUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.example.demo.model.MsgMq;
import com.example.demo.redis.config.JedisManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class WrapperJmsTemplate extends JmsTemplate {
	
	Logger logger = LoggerFactory.getLogger(WrapperJmsTemplate.class);
	private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
	
	private JedisManager jedisManager;
	private int expireRedis;
	
	public WrapperJmsTemplate(ConnectionFactory connectionFactory, JedisManager jedisManager, int expireRedis) {
		super(connectionFactory);
		this.jedisManager = jedisManager;
		this.expireRedis = expireRedis;
	}

	@Nullable
	protected Message doSendAndReceive(Session session, Destination destination, MessageCreator messageCreator)
			throws JMSException {

		Assert.notNull(messageCreator, "MessageCreator must not be null");
		TemporaryQueue responseQueue = null;
		MessageProducer producer = null;
		MessageConsumer consumer = null;
		try {
			Message requestMessage = messageCreator.createMessage(session);
			responseQueue = session.createTemporaryQueue();
			producer = session.createProducer(destination);
			consumer = session.createConsumer(responseQueue);
			requestMessage = setTempQueueToRedis(session, responseQueue, requestMessage);
			requestMessage.setJMSReplyTo(responseQueue);
			if (logger.isDebugEnabled()) {
				logger.debug("Sending created message: " + requestMessage);
			}
			doSend(producer, requestMessage);
			return receiveFromConsumer(consumer, getReceiveTimeout());
		}
		finally {
			JmsUtils.closeMessageConsumer(consumer);
			JmsUtils.closeMessageProducer(producer);
			if (responseQueue != null) {
				responseQueue.delete();
			}
		}
	}
	
	private Message setTempQueueToRedis(Session session, TemporaryQueue tempQueue, Message requestMessage) throws JMSException {
		String queueName = tempQueue.getQueueName();
		String uuid = requestMessage.getStringProperty("uuid");

		try {
			jedisManager.setKeyValueAndExpire(uuid, queueName, expireRedis);
		} catch (Exception e) {
			logger.error("SetTempQueueToRedis Exception : ", e);
		}
		
		String text = ((TextMessage) requestMessage).getText();
		MsgMq msg = gson.fromJson(text, MsgMq.class);
		msg.setDateGwSend(Calendar.getInstance().getTime());
		
		return session.createTextMessage(gson.toJson(msg));
	}
}

package com.example.demo.mq.listener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.example.demo.model.MsgMq;
import com.example.demo.redis.config.JedisManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.mq.jms.MQQueue;

public class ExampleListener implements MessageListener {

	private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
	Logger logger = LoggerFactory.getLogger(ExampleListener.class);
	Logger loggerDebug = LoggerFactory.getLogger("LogDebug");
	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+07:00'");  
	private JmsTemplate jmsTemplate = null;
	private JedisManager jedisManager = null;

	public ExampleListener(JmsTemplate jmsTemplate, JedisManager jedisManager) throws JMSException {
		this.jmsTemplate = jmsTemplate;
		this.jedisManager = jedisManager;
	}

	public void onMessage(Message message) {
		Date now = Calendar.getInstance().getTime();
		try {
			String text = ((TextMessage) message).getText();
			logger.info("Msg Input : {}", text);

			MsgMq msg = gson.fromJson(text, MsgMq.class);
			msg.setDateCorReq(now);
			
			String queueName = getQueueName(msg.getUuid());
			if (StringUtils.isBlank(queueName)) {
				logger.error("Queue Name is Blank.");
				printTime(msg);
				return;
			}
			
			jmsTemplate.send(new MQQueue(queueName), new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					msg.setDateCorResp(Calendar.getInstance().getTime());
					return session.createTextMessage(gson.toJson(msg));
				}
			});
		} catch (JMSException e) {
			logger.error("ExampleListener JMSException : ", e);
		}
	}
	
	private String getQueueName(String key) {
		try {
			return this.jedisManager.getValueAndDelete(key);
		} catch (Exception e) {
			logger.error("Get Queue Name Exception", e);
		}
		return null;
	}
	
	private void printTime(MsgMq msgMq) {
		Long timeFromReqToSendToQueue = msgMq.getDateGwSend().getTime() - msgMq.getDateGwReq().getTime();
		Long timeQueueTest1 = msgMq.getDateSocketReq().getTime() - msgMq.getDateGwSend().getTime();
		Long timeInSocket = msgMq.getDateSocketResp().getTime() - msgMq.getDateSocketReq().getTime();
		Long timeQueueTest2 = msgMq.getDateCorReq().getTime() - msgMq.getDateSocketResp().getTime();
		
		Date dateNow = Calendar.getInstance().getTime();
		
		Map<String, Object> map = new HashMap<>();
		map.put("@timestamp", dateFormat.format(dateNow));
		map.put("data-type", "poc-cor-queue-blank");
		map.put("uuid", msgMq.getUuid());
		map.put("time-from-req-to-send-queue", timeFromReqToSendToQueue);
		map.put("time-queue-test1", timeQueueTest1);
		map.put("time-socket", timeInSocket);
		map.put("time-queue-test2", timeQueueTest2);
		
		loggerDebug.info(gson.toJson(map));
	}
}

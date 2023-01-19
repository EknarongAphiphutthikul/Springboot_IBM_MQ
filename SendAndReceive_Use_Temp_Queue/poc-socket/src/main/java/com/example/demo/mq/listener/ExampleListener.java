package com.example.demo.mq.listener;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.example.demo.model.MsgMq;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.mq.jms.MQQueue;

public class ExampleListener implements MessageListener {

	private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
	Logger logger = LoggerFactory.getLogger(ExampleListener.class);
	private JmsTemplate jmsTemplate = null;
	private Destination queue = null;
	private int maxDelayMilli = 1000;

	public ExampleListener(JmsTemplate jmsTemplate, String queueOutputName, Integer maxDelayMilli) throws JMSException {
		this.jmsTemplate = jmsTemplate;
		this.queue = new MQQueue(queueOutputName);
		if (null != maxDelayMilli) {
			this.maxDelayMilli = maxDelayMilli;
		}
	}

	public void onMessage(Message message) {
		Date now = Calendar.getInstance().getTime();
		try {
			String text = ((TextMessage) message).getText();
			logger.info("Msg Input : {}", text);

			MsgMq req = gson.fromJson(text, MsgMq.class);
			req.setDateSocketReq(now);
			req.setRespMsg("Replying to " + req.getMsg());
			
			try {
				TimeUnit.MILLISECONDS.sleep((new Random()).nextInt(this.maxDelayMilli) + 1);
			} catch (InterruptedException e) {
				logger.error("Sleep InterruptedException", e);
			}

			jmsTemplate.send(this.queue, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					req.setDateSocketResp(Calendar.getInstance().getTime());
					return session.createTextMessage(gson.toJson(req));
				}
			});
		} catch (JMSException e) {
			logger.error("ExampleListener JMSException : ", e);
		}
	}
}

package com.example.demo.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.MsgMq;
import com.example.demo.model.Request;
import com.example.demo.model.Response;
import com.example.demo.mq.config.MQConfigProperties;
import com.example.demo.mq.config.WrapperJmsTemplate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.mq.jms.MQQueue;

@RestController
public class Controller {

	Logger logger = LoggerFactory.getLogger(Controller.class);
	Logger loggerDebug = LoggerFactory.getLogger("LogDebug");
	private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'+07:00'");  

	@Autowired
	private WrapperJmsTemplate jmsTemplate;
	@Autowired
	private MQConfigProperties mqConfig;
	private Destination destination = null;

	@PostConstruct
	public void init() throws JMSException {
		destination = new MQQueue(mqConfig.getQueueDestinationOutput());
	}

	@PostMapping("/post/test/poc")
	public @ResponseBody Response PostMethod(@RequestBody Request req) throws JMSException {
		Date now = Calendar.getInstance().getTime();
		String uuid = UUID.randomUUID().toString() + Calendar.getInstance().getTime().getTime();
		
		logger.info("POC Request : {}, {}", uuid, gson.toJson(req));
		
		Message replyMsg = null;
		try {
			replyMsg = jmsTemplate.sendAndReceive(destination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					MsgMq msgMq = MsgMq.builder().dateGwReq(now).msg(req.getMsg()).uuid(uuid).build();
					TextMessage textMsg = session.createTextMessage(gson.toJson(msgMq));
					textMsg.setJMSCorrelationID(uuid);
					textMsg.setStringProperty("uuid", uuid);
					return textMsg;
				}
			});
		} catch (org.springframework.jms.JmsException e) {
			logger.error("JmsTemplate.sendAndReceive JmsException.", e);
			Response resp = new Response();
			resp.setCode("1");
			resp.setDesc("JmsTemplate.sendAndReceive JmsException.");
			return resp;
		} catch (Throwable e) {
			logger.error("JmsTemplate.sendAndReceive Throwable.", e);
			Response resp = new Response();
			resp.setCode("1");
			resp.setDesc("JmsTemplate.sendAndReceive Throwable.");
			return resp;
		}
		
		if (null == replyMsg) {
			Response resp = new Response();
			resp.setCode("1");
			resp.setDesc("msg resp is null.");
			return resp;
		}
		
		String json = ((TextMessage) replyMsg).getText();
		MsgMq msgMq = gson.fromJson(json, MsgMq.class);
		msgMq.setDateGwResp(Calendar.getInstance().getTime());

		logger.info("POC Msg Mq Reply : {}", gson.toJson(msgMq));
		
		if (!("Replying to " + req.getMsg()).equals(msgMq.getRespMsg())) {
			Response resp = new Response();
			resp.setCode("2");
			resp.setDesc("Message Resp invalid.");
			return resp;
		}

		printTime(msgMq);
		
		Response resp = new Response();
		resp.setCode("0");
		resp.setDesc("Success");
		resp.setMsg(msgMq.getRespMsg());
		resp.setUuid(msgMq.getUuid());
		return resp;
	}
	
	private void printTime(MsgMq msgMq) {
		Long timeTotal = msgMq.getDateGwResp().getTime() - msgMq.getDateGwReq().getTime();
		Long timeFromReqToSendToQueue = msgMq.getDateGwSend().getTime() - msgMq.getDateGwReq().getTime();
		Long timeQueueTest1 = msgMq.getDateSocketReq().getTime() - msgMq.getDateGwSend().getTime();
		Long timeInSocket = msgMq.getDateSocketResp().getTime() - msgMq.getDateSocketReq().getTime();
		Long timeQueueTest2 = msgMq.getDateCorReq().getTime() - msgMq.getDateSocketResp().getTime();
		Long timeInCor = msgMq.getDateCorResp().getTime() - msgMq.getDateCorReq().getTime();
		Long timeTempQueue = msgMq.getDateGwResp().getTime() - msgMq.getDateCorResp().getTime();
		
		Date dateNow = Calendar.getInstance().getTime();
		
		Map<String, Object> map = new HashMap<>();
		map.put("@timestamp", dateFormat.format(dateNow));
		map.put("data-type", "poc-gw-success");
		map.put("uuid", msgMq.getUuid());
		map.put("time-total", timeTotal);
		map.put("time-from-req-to-send-queue", timeFromReqToSendToQueue);
		map.put("time-queue-test1", timeQueueTest1);
		map.put("time-socket", timeInSocket);
		map.put("time-queue-test2", timeQueueTest2);
		map.put("time-cor", timeInCor);
		map.put("time-temp-queue", timeTempQueue);
		
		loggerDebug.info(gson.toJson(map));
	}

}

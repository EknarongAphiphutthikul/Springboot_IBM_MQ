package com.example.demo.model;

import java.util.Date;

import lombok.Data;

@Data
public class MsgMq {

	private Date dateGwReq;
	private Date dateGwSend;
	private Date dateGwResp;
	private Date dateSocketReq;
	private Date dateSocketResp;
	private Date dateCorReq;
	private Date dateCorResp;
	private String msg;
	private String uuid;
	private String respMsg;
}

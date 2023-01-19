package com.example.demo.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

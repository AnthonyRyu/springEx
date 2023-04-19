package com.co.kr.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderMethodName="builder")
public class SoccerListDomain {

	private String socSeq;
	private String mbId;
	private String socTitle;
	private String socPosition;
	private String socTeam;
	private String socContent;
	private String socCreateAt;
	private String socUpdateAt;

}
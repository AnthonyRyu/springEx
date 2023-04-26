package com.co.kr.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderMethodName="builder")
public class SoccerContentDomain {

	private Integer socSeq;
	private String mbId;

	private String socTitle;
	private String socPosition;
	private String socTeam;
	private String socContent;

}
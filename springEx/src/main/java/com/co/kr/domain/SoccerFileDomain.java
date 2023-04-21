package com.co.kr.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderMethodName="builder")
public class SoccerFileDomain {

	
	private Integer socSeq;
	private String mbId;
	private String socPosition;
	private String socTeam;
	
	private String socUpOriginalFileName;
	private String socUpNewFileName; //동일 이름 업로드 될 경우
	private String socUpFilePath;
	private Integer socUpFileSize;
	
}
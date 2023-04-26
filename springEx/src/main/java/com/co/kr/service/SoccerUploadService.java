package com.co.kr.service;


import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.co.kr.domain.SoccerFileDomain;
import com.co.kr.domain.SoccerListDomain;
import com.co.kr.vo.SoccerListVO;

public interface SoccerUploadService {
	
	// 전체 리스트 조회
	public List<SoccerListDomain> soccerList();

	public int soccerFileProcess(SoccerListVO soccerListVO, MultipartHttpServletRequest request, HttpServletRequest httpReq);
	
	// 하나 삭제
	public void soccerContentRemove(HashMap<String, Object> map);
	
	// 하나 삭제
	public void soccerFileRemove(SoccerFileDomain soccerFileDomain);
	
	// 하나 리스트 조회
	public SoccerListDomain soccerSelectOne(HashMap<String, Object> map);
	// 하나 파일 리스트 조회
	public List<SoccerFileDomain> soccerSelectOneFile(HashMap<String, Object> map);

}
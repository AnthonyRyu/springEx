package com.co.kr.service;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.co.kr.domain.SoccerFileDomain;
import com.co.kr.domain.SoccerListDomain;
import com.co.kr.vo.soccerListVO;


public interface SoccerUploadService {
	
	// 인서트
	public int socFileProcess(soccerListVO soccerListVO, MultipartHttpServletRequest request, HttpServletRequest httpReq);
	
	// 전체 리스트 조회   // 지난시간 작성
	public List<SoccerListDomain> soccerList();

	// 하나 삭제
	public void socContentRemove(HashMap<String, Object> map);

	// 하나 삭제
	public void socFileRemove(SoccerFileDomain soccerFileDomain);
	
	// 하나 리스트 조회
	public SoccerListDomain soccerSelectOne(HashMap<String, Object> map);
	// 하나 파일 리스트 조회
	public List<SoccerFileDomain> soccerSelectOneFile(HashMap<String, Object> map);
		
}
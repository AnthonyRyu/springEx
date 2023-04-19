package com.co.kr.controller;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.co.kr.code.Code;
import com.co.kr.domain.SoccerFileDomain;
import com.co.kr.domain.SoccerListDomain;
import com.co.kr.exception.RequestException;
import com.co.kr.service.SoccerUploadService;
import com.co.kr.service.UploadService;

import com.co.kr.vo.soccerListVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class SoccerController {
	
	@Autowired
	private SoccerUploadService soccerUploadService;

	
	@PostMapping(value = "socUpload")
	public ModelAndView socUpload(soccerListVO soccerListVO, MultipartHttpServletRequest request, HttpServletRequest httpReq) throws IOException, ParseException {
		
		ModelAndView mav = new ModelAndView();
		int socSeq = soccerUploadService.socFileProcess(soccerListVO, request, httpReq);
		soccerListVO.setContent(""); //초기화
		soccerListVO.setTitle(""); //초기화
		
		// 화면에서 넘어올때는 bdSeq String이라 string으로 변환해서 넣어즘
		mav = socSelectOneCall(soccerListVO, String.valueOf(socSeq),request);
		mav.setViewName("soccer/socList.html");
		return mav;
		
	}
	
	//리스트 하나 가져오기 따로 함수뺌
	public ModelAndView socSelectOneCall(@ModelAttribute("soccerListVO") soccerListVO soccerListVO, String socSeq, HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		HashMap<String, Object> map = new HashMap<String, Object>();
		HttpSession session = request.getSession();
			
		map.put("socSeq", Integer.parseInt(socSeq));
		SoccerListDomain soccerListDomain = soccerUploadService.soccerSelectOne(map);
		System.out.println("soccerListDomain"+soccerListDomain);
		List<SoccerFileDomain> socFileList =  soccerUploadService.soccerSelectOneFile(map);
			
		for (SoccerFileDomain list : socFileList) {
			String path = list.getUpFilePath().replaceAll("\\\\", "/");
			list.setUpFilePath(path);
		}
		mav.addObject("soccerDetail", soccerListDomain);
		mav.addObject("soccerFiles", socFileList);

		//삭제시 사용할 용도
		session.setAttribute("soccerFiles", socFileList);

		return mav;
	}	
	
	//detail
		@GetMapping("socDetail")
	    public ModelAndView socDetail(@ModelAttribute("soccerListVO") soccerListVO soccerListVO, @RequestParam("socSeq") String socSeq, HttpServletRequest request) throws IOException {
			ModelAndView mav = new ModelAndView();
			//하나파일 가져오기
			mav = socSelectOneCall(soccerListVO, socSeq,request);
			mav.setViewName("soccer/socList.html");
			return mav;
		}
		
	@GetMapping("socEdit")
	public ModelAndView edit(soccerListVO soccerListVO, @RequestParam("socSeq") String socSeq, HttpServletRequest request) throws IOException {
		
		ModelAndView mav = new ModelAndView();
		HashMap<String, Object> map = new HashMap<String, Object>();
		HttpSession session = request.getSession();
			
		map.put("socSeq", Integer.parseInt(socSeq));
		SoccerListDomain soccerListDomain = soccerUploadService.soccerSelectOne(map);
		List<SoccerFileDomain> socFileList =  soccerUploadService.soccerSelectOneFile(map);
			
		for (SoccerFileDomain list : socFileList) {
			String path = list.getUpFilePath().replaceAll("\\\\", "/");
			list.setUpFilePath(path);
		}

		soccerListVO.setSeq(soccerListDomain.getSocSeq());
		soccerListVO.setContent(soccerListDomain.getSocContent());
		soccerListVO.setTitle(soccerListDomain.getSocTitle());
		soccerListVO.setIsEdit("edit");  // upload 재활용하기위해서
			
		
		mav.addObject("detail", soccerListDomain);
		mav.addObject("files", socFileList);
		mav.addObject("fileLen",socFileList.size());
			
		mav.setViewName("soccer/socBoardEditList.html");
		return mav;
	}
	
	@PostMapping("socEditSave")
	public ModelAndView editSave(@ModelAttribute("soccerListVO") soccerListVO soccerListVO, MultipartHttpServletRequest request, HttpServletRequest httpReq) throws IOException {
		ModelAndView mav = new ModelAndView();
		
		//저장
		soccerUploadService.socFileProcess(soccerListVO, request, httpReq);
		
		mav = socSelectOneCall(soccerListVO, soccerListVO.getSeq(),request);
		soccerListVO.setContent(""); //초기화
		soccerListVO.setTitle(""); //초기화
		mav.setViewName("soccer/socList.html");
		return mav;
	}

	@SuppressWarnings("unchecked")
	@GetMapping("socRemove")
	public ModelAndView mbRemove(@RequestParam("socSeq") String socSeq, HttpServletRequest request) throws IOException {
		ModelAndView mav = new ModelAndView();
		
		HttpSession session = request.getSession();
		HashMap<String, Object> map = new HashMap<String, Object>();
		List<SoccerFileDomain> socFileList = null;
		if(session.getAttribute("socFiles") != null) {						
			socFileList = (List<SoccerFileDomain>) session.getAttribute("socFiles");
		}

		map.put("socSeq", Integer.parseInt(socSeq));
		
		//내용삭제
		soccerUploadService.socContentRemove(map);

		for (SoccerFileDomain list : socFileList) {
			list.getUpFilePath();
			Path filePath = Paths.get(list.getUpFilePath());
	 
	        try {
	        	
	            // 파일 물리삭제
	            Files.deleteIfExists(filePath); // notfound시 exception 발생안하고 false 처리
	            // db 삭제 
							soccerUploadService.socFileRemove(list);
				
	        } catch (DirectoryNotEmptyException e) {
							throw RequestException.fire(Code.E404, "디렉토리가 존재하지 않습니다", HttpStatus.NOT_FOUND);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}

		//세션해제
		session.removeAttribute("socFiles"); // 삭제
		mav = socListCall();
		mav.setViewName("soccer/socList.html");
		
		return mav;
	}


	//리스트 가져오기 따로 함수뺌
	public ModelAndView socListCall() {
		ModelAndView mav = new ModelAndView();
		List<SoccerListDomain> items = soccerUploadService.soccerList();
		mav.addObject("items", items);
		return mav;
	}


}
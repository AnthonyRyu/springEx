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
import com.co.kr.vo.SoccerListVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class SoccerListController {
	
	@Autowired
	private SoccerUploadService soccerUploadService;
	
	@PostMapping(value = "soccerUpload")
	public ModelAndView soccerUpload(SoccerListVO soccerListVO, MultipartHttpServletRequest request, HttpServletRequest httpReq) throws IOException, ParseException {
		
		ModelAndView mav = new ModelAndView();
		int socSeq = soccerUploadService.soccerFileProcess(soccerListVO, request, httpReq);
		soccerListVO.setContent(""); //초기화
		soccerListVO.setTitle(""); //초기화
		
		mav = soccerSelectOneCall(soccerListVO, String.valueOf(socSeq),request);
		mav.setViewName("soccer/soccerList.html");
		return mav;
	}
	
	//리스트 하나 가져오기 따로 함수뺌
		public ModelAndView soccerSelectOneCall(@ModelAttribute("soccerListVO") SoccerListVO soccerListVO, String socSeq, HttpServletRequest request) {
			ModelAndView mav = new ModelAndView();
			HashMap<String, Object> map = new HashMap<String, Object>();
			HttpSession session = request.getSession();
			
			map.put("socSeq", Integer.parseInt(socSeq));
			SoccerListDomain soccerListDomain = soccerUploadService.soccerSelectOne(map);
			System.out.println("soccerListDomain" + soccerListDomain);
			List<SoccerFileDomain> soccerList =  soccerUploadService.soccerSelectOneFile(map);
			
			for (SoccerFileDomain list : soccerList) {
				String path = list.getSocUpFilePath().replaceAll("\\\\", "/");
				list.setSocUpFilePath(path);
			}
			mav.addObject("soccerDetail", soccerListDomain);
			mav.addObject("soccerFiles", soccerList);

			//삭제시 사용할 용도
			session.setAttribute("soccerFiles", soccerList);
			
			mav.setViewName("soccer/soccerList.html");
			return mav;
		}
		

		//detail
		@GetMapping("soccerDetail")
		  public ModelAndView bdDetail(@ModelAttribute("soccerListVO") SoccerListVO soccerListVO, @RequestParam("socSeq") String socSeq, HttpServletRequest request) throws IOException {
			ModelAndView mav = new ModelAndView();
			//하나파일 가져오기
			mav = soccerSelectOneCall(soccerListVO, socSeq,request);
			mav.setViewName("soccer/soccerList.html");
			return mav;
		}
		
		@GetMapping("soccerEdit")
		public ModelAndView edit(SoccerListVO soccerListVO, @RequestParam("socSeq") String socSeq, HttpServletRequest request) throws IOException {
			ModelAndView mav = new ModelAndView();

			HashMap<String, Object> map = new HashMap<String, Object>();
			HttpSession session = request.getSession();
			
			map.put("socSeq", Integer.parseInt(socSeq));
			SoccerListDomain soccerListDomain = soccerUploadService.soccerSelectOne(map);
			List<SoccerFileDomain> soccerList =  soccerUploadService.soccerSelectOneFile(map);
			
			for (SoccerFileDomain list : soccerList) {
				String path = list.getSocUpFilePath().replaceAll("\\\\", "/");
				list.setSocUpFilePath(path);
			}

			soccerListVO.setSeq(soccerListDomain.getSocSeq());
			soccerListVO.setContent(soccerListDomain.getSocContent());
			soccerListVO.setTitle(soccerListDomain.getSocTitle());
			soccerListVO.setPosition(soccerListDomain.getSocPosition());
			soccerListVO.setTeam(soccerListDomain.getSocTeam());
			soccerListVO.setIsEdit("soccerEdit");  // upload 재활용하기위해서
			
		
			mav.addObject("soccerDetail", soccerListDomain);
			mav.addObject("soccerFiles", soccerList);
			mav.addObject("soccerFileLen",soccerList.size());
			
			mav.setViewName("soccer/soccerEditList.html");
			return mav;
		}
		
		@PostMapping("soccerEditSave")
		public ModelAndView soccerEditSave(@ModelAttribute("soccerListVO") SoccerListVO soccerListVO, MultipartHttpServletRequest request, HttpServletRequest httpReq) throws IOException {
			ModelAndView mav = new ModelAndView();
			
			//저장
			soccerUploadService.soccerFileProcess(soccerListVO, request, httpReq);
			
			mav = soccerSelectOneCall(soccerListVO, soccerListVO.getSeq(),request);
			soccerListVO.setContent(""); //초기화
			soccerListVO.setTitle(""); //초기화
			mav.setViewName("soccer/soccerList.html");
			return mav;
		}
		
		@GetMapping("soccerRemove")
		public ModelAndView soccerRemove(@RequestParam("socSeq") String socSeq, HttpServletRequest request) throws IOException {
			ModelAndView mav = new ModelAndView();
			
			HttpSession session = request.getSession();
			HashMap<String, Object> map = new HashMap<String, Object>();
			List<SoccerFileDomain> soccerList = null;
			if(session.getAttribute("soccerFiles") != null) {						
				soccerList = (List<SoccerFileDomain>) session.getAttribute("soccerFiles");
			}

			map.put("socSeq", Integer.parseInt(socSeq));
			
			//내용삭제
			soccerUploadService.soccerContentRemove(map);

			for (SoccerFileDomain list : soccerList) {
				list.getSocUpFilePath();
				Path filePath = Paths.get(list.getSocUpFilePath());
		 
		        try {
		        	
		            // 파일 물리삭제
		            Files.deleteIfExists(filePath); // notfound시 exception 발생안하고 false 처리
		            // db 삭제 
								soccerUploadService.soccerFileRemove(list);
					
		        } catch (DirectoryNotEmptyException e) {
								throw RequestException.fire(Code.E404, "디렉토리가 존재하지 않습니다", HttpStatus.NOT_FOUND);
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
			}

			//세션해제
			session.removeAttribute("soccerFiles"); // 삭제
			mav = soccerListCall();
			mav.setViewName("soccer/soccerList.html");
			
			return mav;
		}

		//리스트 가져오기 따로 함수뺌
		public ModelAndView soccerListCall() {
			ModelAndView mav = new ModelAndView();
			List<SoccerListDomain> items = soccerUploadService.soccerList();
			mav.addObject("items", items);
			return mav;
		}


	
}
package com.co.kr.service;


import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.co.kr.mapper.SoccerUploadMapper;
import com.co.kr.util.CommonUtils;
import com.co.kr.vo.SoccerListVO;
import com.co.kr.code.Code;
import com.co.kr.domain.SoccerContentDomain;
import com.co.kr.domain.SoccerFileDomain;
import com.co.kr.domain.SoccerListDomain;
import com.co.kr.exception.RequestException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class SoccerUploadServiceImple implements SoccerUploadService {

	@Autowired
	private SoccerUploadMapper soccerUploadMapper;
	
	@Override
	public List<SoccerListDomain> soccerList() {
		// TODO Auto-generated method stub
		return soccerUploadMapper.soccerList();
	}
	
	@Override
	public int soccerFileProcess(SoccerListVO soccerListVO, MultipartHttpServletRequest request, HttpServletRequest httpReq) {
		//session 생성
		HttpSession session = httpReq.getSession();
		
		//content domain 생성 
		SoccerContentDomain soccerContentDomain = SoccerContentDomain.builder()
				.mbId(session.getAttribute("id").toString())
				.socTitle(soccerListVO.getTitle())
				.socPosition(soccerListVO.getPosition())
				.socTeam(soccerListVO.getTeam())
				.socContent(soccerListVO.getContent())
				.build();
		
				if(soccerListVO.getIsEdit() != null) {
					soccerContentDomain.setSocSeq(Integer.parseInt(soccerListVO.getSeq()));
					System.out.println("수정업데이트");
					// db 업데이트
					soccerUploadMapper.soccerContentUpdate(soccerContentDomain);
				}else {	
					// db 인서트
					soccerUploadMapper.soccerContentUpload(soccerContentDomain);
					System.out.println(" db 인서트");

				}
				
				// file 데이터 db 저장시 쓰일 값 추출
				int socSeq = soccerContentDomain.getSocSeq();
				String mbId = soccerContentDomain.getMbId();
				
				//파일객체 담음
				List<MultipartFile> multipartFiles = request.getFiles("soccerFiles");
				
				
				// 게시글 수정시 파일관련 물리저장 파일, db 데이터 삭제 
				if(soccerListVO.getIsEdit() != null) { // 수정시 

	
					List<SoccerFileDomain> soccerList = null;
					
					
					
					for (MultipartFile multipartFile : multipartFiles) {
					
					
						if(!multipartFile.isEmpty()) {   // 수정시 새로 파일 첨부될때 세션에 담긴 파일 지우기
							
							
							if(session.getAttribute("soccerFiles") != null) {	

								soccerList = (List<SoccerFileDomain>) session.getAttribute("soccerFiles");
								
								for (SoccerFileDomain list : soccerList) {
									list.getSocUpFilePath();
									Path filePath = Paths.get(list.getSocUpFilePath());
							 
							        try {
							        	
							            // 파일 삭제
							            Files.deleteIfExists(filePath); // notfound시 exception 발생안하고 false 처리
							            //삭제 
													soccerFileRemove(list); //데이터 삭제
										
							        } catch (DirectoryNotEmptyException e) {
														throw RequestException.fire(Code.E404, "디렉토리가 존재하지 않습니다", HttpStatus.NOT_FOUND);
							        } catch (IOException e) {
							            e.printStackTrace();
							        }
								}
								
							}
							
						}

					}
					
					
				}
				
				
				///////////////////////////// 새로운 파일 저장 ///////////////////////
				
				// 저장 root 경로만들기
				Path rootPath = Paths.get(new File("C://").toString(),"soccerUpload", File.separator).toAbsolutePath().normalize();			
				File pathCheck = new File(rootPath.toString());
				
				// folder chcek
				if(!pathCheck.exists()) pathCheck.mkdirs();
				
	
				for (MultipartFile multipartFile : multipartFiles) {
					
					if(!multipartFile.isEmpty()) {  // 파일 있을때 
						
						//확장자 추출
						String originalFileExtension;
						String contentType = multipartFile.getContentType();
						String origFilename = multipartFile.getOriginalFilename();
						
						//확장자 존재하지 않을 경우
						if(ObjectUtils.isEmpty(contentType)){
							break;
						}else { // 확장자가 jpeg, png인 파일들만 받아서 처리
							if(contentType.contains("image/jpeg")) {
								originalFileExtension = ".jpg";
							}else if(contentType.contains("image/png")) {
								originalFileExtension = ".png";
							}else {
								break;
							}
						}
						
						//파일명을 업로드한 날짜로 변환하여 저장
						String uuid = UUID.randomUUID().toString();
						String current = CommonUtils.currentTime();
						String newFileName = uuid + current + originalFileExtension;
						
						//최종경로까지 지정
						Path targetPath = rootPath.resolve(newFileName);
						
						File soccerFile = new File(targetPath.toString());
						
						try {
							//파일복사저장
							multipartFile.transferTo(soccerFile);
							// 파일 권한 설정(쓰기, 읽기)
							soccerFile.setWritable(true);
							soccerFile.setReadable(true);
							
							
							//파일 domain 생성 
							SoccerFileDomain soccerFileDomain = SoccerFileDomain.builder()
									.socSeq(socSeq)
									.mbId(mbId)
									.socUpOriginalFileName(origFilename)
									.socUpNewFileName("resources/soccerUpload/"+newFileName) // WebConfig에 동적 이미지 폴더 생성 했기때문
									.socUpFilePath(targetPath.toString())
									.socUpFileSize((int)multipartFile.getSize())
									.build();
							
								// db 인서트
								soccerUploadMapper.soccerFileUpload(soccerFileDomain);
								System.out.println("upload done");
							
						} catch (IOException e) {
							throw RequestException.fire(Code.E404, "잘못된 업로드 파일", HttpStatus.NOT_FOUND);
						}
					}

				}
				
		
				return socSeq; // 저장된 게시판 번호
	}


	// 컨텐츠 삭제
	@Override
	public void soccerContentRemove(HashMap<String, Object> map) {
		soccerUploadMapper.soccerContentRemove(map);
	}
	
  // 파일 삭제
	@Override
	public void soccerFileRemove(SoccerFileDomain soccerFileDomain) {
		soccerUploadMapper.soccerFileRemove(soccerFileDomain);
	}
	
	// 하나만 가져오기
	@Override
	public SoccerListDomain soccerSelectOne(HashMap<String, Object> map) {
		return soccerUploadMapper.soccerSelectOne(map);
	}

	// 하나 게시글 파일만 가져오기
	@Override
	public List<SoccerFileDomain> soccerSelectOneFile(HashMap<String, Object> map) {
		return soccerUploadMapper.soccerSelectOneFile(map);
	}

}


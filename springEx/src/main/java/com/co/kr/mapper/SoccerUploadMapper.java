package com.co.kr.mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.co.kr.domain.SoccerContentDomain;
import com.co.kr.domain.SoccerFileDomain;
import com.co.kr.domain.SoccerListDomain;

@Mapper
public interface SoccerUploadMapper {

	//list
	public List<SoccerListDomain> soccerList();
	
	//content upload
	public void soccerContentUpload(SoccerContentDomain soccerContentDomain);
	//file upload
	public void soccerFileUpload(SoccerFileDomain soccerFileDomain);

	//content update
	public void soccerContentUpdate(SoccerContentDomain soccerContentDomain);
	//file update
	public void soccerFileUpdate(SoccerFileDomain soccerFileDomain);

  //content delete 
	public void soccerContentRemove(HashMap<String, Object> map);
	//file delete 
	public void soccerFileRemove(SoccerFileDomain soccerFileDomain);
	
	//select one
	public SoccerListDomain soccerSelectOne(HashMap<String, Object> map);

	//select one file
	public List<SoccerFileDomain> soccerSelectOneFile(HashMap<String, Object> map);
	

}


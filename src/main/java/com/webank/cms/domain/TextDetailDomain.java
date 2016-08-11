package com.webank.cms.domain;

import com.google.gson.Gson;

/**
 * @Date Aug 1, 2016
 *
 * @Author smartloli
 *
 * @Email smartloli.org@gmail.com
 *
 * @Note TODO
 */
public class TextDetailDomain {

	private long articleID;
	private String title;
	private String type;
	private String sDate;
	private String eDate;
	private String chanle;
	private String isTop;
	private String content;
	private String isValid;

	public String getIsValid() {
		return isValid;
	}

	public void setIsValid(String isValid) {
		this.isValid = isValid;
	}

	public long getArticleID() {
		return articleID;
	}

	public void setArticleID(long articleID) {
		this.articleID = articleID;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getsDate() {
		return sDate;
	}

	public void setsDate(String sDate) {
		this.sDate = sDate;
	}

	public String geteDate() {
		return eDate;
	}

	public void seteDate(String eDate) {
		this.eDate = eDate;
	}

	public String getChanle() {
		return chanle;
	}

	public void setChanle(String chanle) {
		this.chanle = chanle;
	}

	public String getIsTop() {
		return isTop;
	}

	public void setIsTop(String isTop) {
		this.isTop = isTop;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}

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
public class ArticleDomain {
	private long articleID;
	private String title;
	private String chanle;
	private String isTop;
	private String author;
	private String createDate;
	private String status;// 发布｜草稿

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

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}

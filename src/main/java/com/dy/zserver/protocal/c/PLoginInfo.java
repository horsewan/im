package com.dy.zserver.protocal.c;

public class PLoginInfo
{
	private String loginUserId = null;
	private String loginToken = null;
	private String extra = null;
	
	public PLoginInfo(String loginUserId, String loginToken)
	{
		this(loginUserId, loginToken, null);
	}
	
	public PLoginInfo(String loginUserId, String loginToken, String extra)
	{
		this.loginUserId = loginUserId;
		this.loginToken = loginToken;
		this.extra = extra;
	}
	
	public String getLoginUserId()
	{
		return loginUserId;
	}

	public void setLoginUserId(String loginUserId)
	{
		this.loginUserId = loginUserId;
	}

	public String getLoginToken()
	{
		return loginToken;
	}

	public void setLoginToken(String loginToken)
	{
		this.loginToken = loginToken;
	}

	public String getExtra()
	{
		return extra;
	}
	
	public void setExtra(String extra)
	{
		this.extra = extra;
	}
}

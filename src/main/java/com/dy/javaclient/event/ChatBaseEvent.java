package com.dy.javaclient.event;

public interface ChatBaseEvent
{
    public void onLoginMessage(int dwErrorCode);    
    public void onLinkCloseMessage(int dwErrorCode);	
}

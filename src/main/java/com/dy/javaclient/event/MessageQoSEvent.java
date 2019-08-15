package com.dy.javaclient.event;

import com.dy.zserver.protocal.Protocal;

import java.util.ArrayList;

public interface MessageQoSEvent
{
	void messagesLost(ArrayList<Protocal> lostMessages);
	void messagesBeReceived(String theFingerPrint);
}

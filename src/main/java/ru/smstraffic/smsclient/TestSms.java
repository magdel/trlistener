///////////////////////////////////////////////////////////////////////////////////////////////////
//
// Sample use of java for sending SMS via SMS Traffic
//
// (c) SMS Traffic, 2008
// www.smstraffic.ru, info@smstraffic.ru, (495)228-3649, (495)642-9569
//
///////////////////////////////////////////////////////////////////////////////////////////////////

package ru.smstraffic.smsclient;

public class TestSms 
{
	public static void maina(String[] args) 
	{
		try{
			String sms_id=Sms.send("79161234567", "test message от нетрадара", "java", 1);
			System.out.println("sms_id="+sms_id);
		}
		catch (SmsException e){
			System.out.println("SmsException: "+e.getMessage());
		}
	}
}

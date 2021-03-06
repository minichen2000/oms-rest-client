package com.alu.restfulClientTestMgr.OmsClient;

import com.alu.restfulClientTestMgr.JettyHttpClientService.HttpCall;
import com.alu.restfulClientTestMgr.JettyHttpClientService.HttpClientService;
import com.alu.restfulClientTestMgr.cometd.CometdEntrance;
import com.alu.restfulClientTestMgr.constants.ConfLoader;
import com.alu.restfulClientTestMgr.constants.ConfigKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class RestFulLoginManager implements ISession {
	
	private static Logger log = LogManager.getLogger(RestFulLoginManager.class);
	
	private SessionTask sessionTask=null;
	
	private String url;

	private boolean isConnected=false;

	private static RestFulLoginManager inst_ =null;

	public static RestFulLoginManager getInstance()
	{
		if( inst_ == null )
		{
			inst_ = new RestFulLoginManager();
		}
		return inst_;
	}
	private RestFulLoginManager(){

	}
	
	public synchronized void startSession()
    {
        if( sessionTask == null )
        {
            sessionTask = new SessionTask( this );
            sessionTask.setRegisterInterval( ConfLoader.getInstance().getInt(
                ConfigKey.session_register_interval,
                ConfigKey.default_session_register_interval ) );
            sessionTask.setPingInterval( ConfLoader.getInstance().getInt(
                ConfigKey.session_ping_interval,
                ConfigKey.default_session_ping_interval ) );
            sessionTask.start();
        }
    }

    public synchronized void endSession()
    {
        if( sessionTask == null )
            return;
        sessionTask.terminate();
        sessionTask = null;
		isConnected=false;
		HttpClientService.instance().closeClient();
    }

	@Override
	public boolean register() {
		// TODO Auto-generated method stub
		try {
			new RestClientLogin().login();

			CometdEntrance.start();


			return checkLogin();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("Restful Login Failed!", e);
		}
		return false;
	}

	@Override
	public boolean ping() {
		// TODO Auto-generated method stub
		try {
			return checkLogin();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("Restful Login Failed!", e);
		}
		return false;
	}

	private boolean checkLogin() throws Exception {
		if(url == null){
			url = "https://"
					+ ConfLoader.getInstance().getConf("otnip")
					+ ":"
					+ ConfLoader.getInstance().getInt("otnport", RestFulConstant.OTNPORT)
					+ "/oms1350/esmBrowser/customer/getAllCustomers/+8:0";
		}
		String response = HttpCall.instance().getCall(url, "application/xml");
		log.debug( "getAllCustomers rlt : " + response );
		if(response.contains("html")) {
			isConnected=false;
			return false;
		}
		isConnected=true;
		return true;
	}

	@Override
	public void sessionLost() {
		// TODO Auto-generated method stub
	}

	@Override
	public void sessionRecover() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isConnected() {
		return isConnected;
	}


}

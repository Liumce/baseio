package com.generallycloud.test.nio.http11;

import com.generallycloud.nio.container.http11.startup.HttpServerStartup;

public class TestHTTPServer {

	public static void main(String[] args) throws Exception {
		
		HttpServerStartup s = new HttpServerStartup();
		
		s.launch("http");
	}
}

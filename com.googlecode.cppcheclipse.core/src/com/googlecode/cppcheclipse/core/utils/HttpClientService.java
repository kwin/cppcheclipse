package com.googlecode.cppcheclipse.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;

public class HttpClientService implements IHttpClientService {

	private IProxyService proxyService;
	
	private static class ProxyAuthenticator extends Authenticator {  
		  
	    private String user, password;  
	  
	    public ProxyAuthenticator(String user, String password) {  
	        this.user = user;  
	        this.password = password;  
	    }  
	  
	    protected PasswordAuthentication getPasswordAuthentication() {  
	        return new PasswordAuthentication(user, password.toCharArray());  
	    }  
	}  
	
	
	private Proxy getProxy(URI uri) throws UnknownHostException {
		IProxyData proxyData = getProxyData(uri);
		if (proxyData == null) {
			if (!isProxiesEnabled()) {
				return Proxy.NO_PROXY;
			}
			return null;
		}
		
		return getProxyFromProxyData(proxyData);

	}
	
	private Proxy getProxyFromProxyData(IProxyData proxyData) throws UnknownHostException {
		
		Type proxyType;
		if (IProxyData.HTTP_PROXY_TYPE.equals(proxyData.getType())) {
			proxyType = Type.HTTP;
		} else if (IProxyData.SOCKS_PROXY_TYPE.equals(proxyData.getType())) {
			proxyType = Type.SOCKS;
		} else if (IProxyData.HTTPS_PROXY_TYPE.equals(proxyData.getType())) {
			proxyType = Type.HTTP;
		} else {
			throw new IllegalArgumentException("Invalid proxy type " + proxyData.getType());
		}

		InetSocketAddress sockAddr = new InetSocketAddress(InetAddress.getByName(proxyData.getHost()), proxyData.getPort());
		Proxy proxy = new Proxy(proxyType, sockAddr);
		if (!StringUtils.isEmpty(proxyData.getUserId())) {
			Authenticator.setDefault(new ProxyAuthenticator(proxyData.getUserId(), proxyData.getPassword()));  
		}
		return proxy;
	}

	private IProxyData getProxyData(URI uri) {
		IProxyService proxyService = getProxyService();
		if (proxyService != null && proxyService.isProxiesEnabled()) {
			if (!proxyService.isSystemProxiesEnabled()) {
				IProxyData[] proxies = proxyService.select(uri);
				if (proxies.length > 0) {
					return proxies[0];
				}
			}
		}
		return null;
	}
	
	private boolean isProxiesEnabled() {
		IProxyService proxyService = getProxyService();
		if (proxyService != null && proxyService.isProxiesEnabled())
			return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see com.googlecode.cppcheclipse.core.utils.IHttpClient#executeGetRequest(java.net.URL)
	 */
	public InputStream executeGetRequest(URL url) throws URISyntaxException, IOException {
		Proxy proxy = getProxy(url.toURI());
		
		HttpURLConnection connection;
		if (proxy != null) 
			connection = (HttpURLConnection) url.openConnection(proxy);
		else 
			connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setDoOutput(true);
		connection.setReadTimeout(10000);
		if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new IOException("Wrong response code: "
					+ connection.getResponseMessage());
		}
		return connection.getInputStream();
	}
	
	/**
			 * Binds the {@link IProxyService} service reference.
			 *
			 * @param proxyService the {@link IProxyService} service reference to bind
			 */
	protected void bindProxyService(IProxyService proxyService) {
		this.proxyService = proxyService;
	}

	/**
	 * Unbinds the {@link IProxyService} service reference.
	 * 
	 * @param proxyService
	 *            the {@link IProxyService} service reference to unbind
	 */
	protected void unbindProxyService(IProxyService proxyService) {
		this.proxyService = null;
	}

	/**
	 * Gets the {@link IProxyService} instance.
	 * 
	 * @return the {@link IProxyService} instance
	 */
	protected IProxyService getProxyService() {
		return proxyService;
	}
}

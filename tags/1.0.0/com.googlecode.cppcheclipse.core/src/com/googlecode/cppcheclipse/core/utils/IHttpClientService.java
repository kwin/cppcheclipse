package com.googlecode.cppcheclipse.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

public interface IHttpClientService {

	public abstract InputStream executeGetRequest(URL url)
			throws URISyntaxException, IOException;

}
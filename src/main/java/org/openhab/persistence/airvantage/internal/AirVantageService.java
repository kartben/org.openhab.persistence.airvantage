package org.openhab.persistence.airvantage.internal;

import java.util.Dictionary;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.openhab.core.items.Item;
import org.openhab.core.persistence.PersistenceService;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of an AirVantage {@link PersistenceService}. To
 * learn more about AirVantage please visit their <a
 * href="http://airvantage.net">website</a>.
 * 
 * @author Benjamin Cabé
 * @since 0.1.0
 */
public class AirVantageService implements PersistenceService, ManagedService {

	private static final Logger logger = LoggerFactory
			.getLogger(AirVantageService.class);

	private String url;

	private String systemId;

	private String systemPassword;

	private final static String DEFAULT_SERVER_URL = "https://na.airvantage.net";

	private final static String API_ENDPOINT = "/device/messages";

	private final static String DEFAULT_SYSTEM_ID = "foo";

	private final static String DEFAULT_SYSTEM_PASSWORD = "bar";

	private boolean initialized = false;

	/**
	 * @{inheritDoc
	 */
	public String getName() {
		return "airvantage";
	}

	/**
	 * @{inheritDoc
	 */
	public void store(Item item, String alias) {
		if (initialized) {
			try {
				HttpClient httpClient = new HttpClient();
				// authentication
				// httpClient.getParams().setAuthenticationPreemptive(true);
				httpClient.getState().setCredentials(
						new AuthScope(null, 443, "AirVantage Services"),
						new UsernamePasswordCredentials(systemId,
								systemPassword));

				String jsonString = String
						.format("[{ \"%s\": [ { \"timestamp\":%s, \"value\": \"%s\" } ] } ]",
								alias, System.currentTimeMillis(), item.getState().toString());

				PostMethod postMethod = new PostMethod(url + API_ENDPOINT);
				StringRequestEntity requestEntity = new StringRequestEntity(
						jsonString, "application/json", "UTF-8");
				postMethod.setRequestEntity(requestEntity);
				postMethod.setDoAuthentication(true);
				httpClient.executeMethod(postMethod);

				logger.debug(
						"Stored item '{}' ('{}') as '{}' in AirVantage and received response: {}",
						new String[] { item.getName(), jsonString, alias,
								Integer.toString(postMethod.getStatusCode()) });
			} catch (Exception e) {
				e.printStackTrace();
				logger.warn("Connection error");
			}
		}
	}

	/**
	 * @{inheritDoc
	 */
	public void store(Item item) {
		throw new UnsupportedOperationException(
				"The mqtt service requires aliases for persistence configurations that should match the id within the feed");
	}

	/**
	 * @{inheritDoc
	 */
	@SuppressWarnings("rawtypes")
	public void updated(Dictionary config) throws ConfigurationException {
		if (config != null) {

			url = (String) config.get("url");
			if (StringUtils.isBlank(url)) {
				url = DEFAULT_SERVER_URL;
			}

			systemId = (String) config.get("systemId");
			if (StringUtils.isBlank(systemId)) {
				systemId = DEFAULT_SYSTEM_ID;
			}

			systemPassword = (String) config.get("systemPassword");
			if (StringUtils.isBlank(systemPassword)) {
				systemPassword = DEFAULT_SYSTEM_PASSWORD;
			}

			initialized = true;
		}
	}

}

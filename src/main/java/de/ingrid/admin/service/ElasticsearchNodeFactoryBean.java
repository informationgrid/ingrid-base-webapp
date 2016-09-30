/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.admin.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import de.ingrid.admin.Config;
import de.ingrid.admin.JettyStarter;

/**
 * A {@link FactoryBean} implementation used to create a {@link Node} element
 * which is an embedded instance of the cluster within a running application.
 * <p>
 * This factory allows for defining custom configuration via the
 * {@link #setConfigLocation(Resource)} or {@link #setConfigLocations(List)}
 * property setters.
 * <p>
 * <b>Note</b>: multiple configurations can be "accumulated" since
 * {@link Builder#loadFromStream(String, java.io.InputStream)} doesn't replace
 * but adds to the map (this also means that loading order of configuration
 * files matters).
 * <p>
 * In addition Spring's property mechanism can be used via
 * {@link #setSettings(Map)} property setter which allows for local settings to
 * be configured via Spring.
 * <p>
 * The lifecycle of the underlying {@link Node} instance is tied to the
 * lifecycle of the bean via the {@link #destroy()} method which calls
 * {@link Node#close()}
 * 
 * @author Erez Mazor (erezmazor@gmail.com)
 */
@Service
public class ElasticsearchNodeFactoryBean implements FactoryBean<Node>,
		InitializingBean, DisposableBean {

	protected final Log logger = LogFactory.getLog( getClass() );

	private List<Resource> configLocations;

	private Resource configLocation;

	private Map<String, String> settings;

	private Node node = null;
	
	private Client client = null;

    private Properties properties;

    //private boolean isLocal = false;

    private boolean isClient = false;

	public boolean isClient() {
        return isClient;
    }

    public void setClient(boolean isClient) {
        this.isClient = isClient;
    }

    public void setConfigLocation(final Resource configLocation) {
		this.configLocation = configLocation;
	}

	public void setConfigLocations(final List<Resource> configLocations) {
		this.configLocations = configLocations;
	}

	public void setSettings(final Map<String, String> settings) {
		this.settings = settings;
	}
	
	public void setProperties(Properties props) {
        this.properties = props;
        
    }

	@Override
	public void afterPropertiesSet() throws Exception {
	    Config config = JettyStarter.getInstance().config;
	    // only setup elastic nodes if indexing is enabled
	    if (config.getIndexing()) {
	        if (config.esRemoteNode) {
	            createTransportClient(config.esRemoteHosts);
	        } else {
	            internalCreateNode();
	        }
	    } else {
	        logger.warn( "Since Indexing is not enabled, this component should not have Elastic Search enabled at all! This bean should be excluded in the spring configuration." );
	    }
	}
	
	public Client getClient() {
	    return JettyStarter.getInstance().config.esRemoteNode ? client : node.client();
	}

	private void createTransportClient(String[] esRemoteHosts) throws UnknownHostException {
	    TransportClient transportClient = null;
	    
	    Properties props = getPropertiesFromElasticsearch();
	    if (props != null) {
	        transportClient = TransportClient.builder().settings( Settings.builder().put( props ) ).build();
	    } else {
	        transportClient = TransportClient.builder().build();
	    }
	    
	    for (String host : esRemoteHosts) {
	        String[] splittedHost = host.split( ":" );
	        transportClient.addTransportAddress( new InetSocketTransportAddress(InetAddress.getByName(splittedHost[0]), Integer.valueOf( splittedHost[1] )) ); 
        }
	    
	    client = transportClient;
	}
	
	private Properties getPropertiesFromElasticsearch() {
	    try {
            ClassPathResource resource = new ClassPathResource( "/elasticsearch.properties" );
            Properties p = new Properties();
            if (resource.exists()) {
                p.load( resource.getInputStream() );
                ClassPathResource resourceOverride = new ClassPathResource( "/elasticsearch.override.properties" );
                if (resourceOverride.exists()) {
                    p.load( resourceOverride.getInputStream() );
                }
            }
            return p;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
	}
	
	private void internalCreateNode() {
	    
	    // TransportClient tc = TransportClient.builder
		final NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder();
		
		// set inital configurations coming from the property file
		try {
		    ClassPathResource resource = new ClassPathResource( "/elasticsearch.properties" );
		    if (resource.exists()) {
		        Properties p = new Properties();
		        p.load( resource.getInputStream() );
		        nodeBuilder.getSettings().put( p );
		        ClassPathResource resourceOverride = new ClassPathResource( "/elasticsearch.override.properties" );
		        if (resourceOverride.exists()) {
	                p.load( resourceOverride.getInputStream() );
	                nodeBuilder.getSettings().put( p );
	            }
		    }
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		// other possibilities for configuration
		// TODO: remove those not needed!
		if (null != configLocation) {
			internalLoadSettings( nodeBuilder, configLocation );
		}

		if (null != configLocations) {
			for (final Resource location : configLocations) {
				internalLoadSettings( nodeBuilder, location );
			}
		}

		if (null != settings) {
			nodeBuilder.getSettings().put( settings );
		}
		if (null != properties) {
		    nodeBuilder.getSettings().put( properties );
		}

		Node localNode = nodeBuilder.node();
		localNode.client().admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet();
		node = localNode;
	}

	private void internalLoadSettings(final NodeBuilder nodeBuilder,
			final Resource configLocation) {

		try {
			final String filename = configLocation.getFilename();
			if (logger.isInfoEnabled()) {
				logger.info( "Loading configuration file from: " + filename );
			}
			nodeBuilder.getSettings().loadFromStream( filename,
					configLocation.getInputStream() );
		} catch (final Exception e) {
			throw new IllegalArgumentException(
					"Could not load settings from configLocation: "
							+ configLocation.getDescription(), e );
		}
	}

	@Override
	public void destroy() throws Exception {
		try {
		    if (client != null) client.close();
			if (node != null) node.close();
		} catch (final Exception e) {
			logger.error( "Error closing Elasticsearch node: ", e );
		}
	}

	@Override
	public Node getObject() throws Exception {
		int cnt = 1;
	    while (node == null && cnt <= 10) {
		    logger.info("Wait for elastic search node to start: " + cnt + " sec.");
	        Thread.sleep(1000);
		    cnt ++;
		}
	    if (node == null) {
	        logger.error("Could not start Elastic Search node within 10 sec!");
	        throw new RuntimeException("Could not start Elastic Search node within 10 sec!");
	    }
	    return node;
	}

	@Override
	public Class<Node> getObjectType() {
		return Node.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

//    public void setLocal(boolean value) {
//        this.isLocal = value;
//        
//    }

}

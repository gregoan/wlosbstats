//Copyright (C) 2011-2013 Paul Done . All rights reserved.
//This file is part of the HostMachineStats software distribution. Refer to 
//the file LICENSE in the root of the HostMachineStats distribution.
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
//AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
//IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
//ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE 
//LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
//CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
//SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
//INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
//CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
//ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
//POSSIBILITY OF SUCH DAMAGE.
package wlosbstats.mbeans;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import wlosbstats.util.AppLog;
import wlosbstats.util.MonitorProperties;

import com.bea.wli.config.Ref;
import com.bea.wli.monitoring.DomainMonitoringDisabledException;
import com.bea.wli.monitoring.InvalidServiceRefException;
import com.bea.wli.monitoring.MonitoringException;
import com.bea.wli.monitoring.MonitoringNotEnabledException;
import com.bea.wli.monitoring.ResourceStatistic;
import com.bea.wli.monitoring.ResourceType;
import com.bea.wli.monitoring.ServiceDomainMBean;
import com.bea.wli.monitoring.ServiceResourceStatistic;
import com.bea.wli.monitoring.StatisticType;
import com.bea.wli.monitoring.StatisticValue;

/**
 * Implementation of the MBean exposing O.S/machine statistics for the machine
 * hosting this WebLogic Server instances. Provides read-only attributes for 
 * useful CPU, Memory and Network related usages statistics.Use SIGAR JNI/C 
 * libraries under the covers (http://support.hyperic.com/display/SIGAR/Home) 
 * to retrieve specific statistics from host operating system.
 *  
 * @see javax.management.MXBean
 */
public class WLOsbStats implements WLOsbStatsMXBean, MBeanRegistration {
	
	private Map<String, Map<String, Map<String, Double>>>  statistics = new LinkedHashMap<>();
	
	// Constants
	private final static String WL_OSB_APP_VERSION = "0.0.1";
	
	protected final static String JNDI_ROOT = "/jndi/";
	private final static String[] LOCAL_SERVER_RUNTIME_MBEAN_JNDI_LOOKUPS = {"java:comp/env/jmx/runtime", "java:comp/jmx/runtime"};
	private static volatile MBeanServerConnection cachedLocalConn = null;
	
	// Members
	private ServiceDomainMBean serviceDomainMBean = null;
	
	//private static final ObjectName domainRuntimeServiceMBean;
	private static final ObjectName serverRuntimeServiceMBean;
	
	/*
	static {
		try {
			domainRuntimeServiceMBean = new ObjectName("com.bea:Name=DomainRuntimeService,Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean");
		} catch (MalformedObjectNameException e) {
			throw new AssertionError(e.toString());
		}
	}
	*/
	
	static {
		try {
			serverRuntimeServiceMBean = new ObjectName("com.bea:Name=RuntimeService,Type=weblogic.management.mbeanservers.runtime.RuntimeServiceMBean");
		} catch (MalformedObjectNameException e) {
			throw new AssertionError(e.toString());
		}
	}
	
	//public final static String DOMAIN_CONFIGURATION = "DomainConfiguration";
	
	// Constants
	private static final String DOMAIN_RUNTIME_SERVICE_NAME = "weblogic.management.mbeanservers.domainruntime";
	private static final String WEBLOGIC_PROVIDER_PACKAGES = "weblogic.management.remote";
	private static final String WEBLOGIC_INSECURE_REMOTE_PROTOCOL = "t3";
	private static final String WEBLOGIC_SECURE_REMOTE_PROTOCOL = "t3s";
	
	public static final String DEFAULT_WKMGR_NAME = "weblogic.kernel.Default";
	public final static String SERVER_RUNTIME = "ServerRuntime";
	public final static String ADMIN_SERVER_HOSTNAME = "AdminServerHost";
	public final static String ADMIN_SERVER_PORT = "AdminServerListenPort";
	public final static String ADMIN_SERVER_NAME = "AdminServerName";
	public final static String IS_ADMIN_SERVER_PORT_SECURED = "AdminServerListenPortSecure";
	public final static String WORK_MANAGER_RUNTIMES = "WorkManagerRuntimes";
	public final static String NAME = "Name";

	/**
	 * 
	 */
	private boolean init(String serviceName) {
		
		try {
			MBeanServerConnection localConn = getCachedLocalConn();
			ObjectName serverRuntime = (ObjectName) localConn.getAttribute(serverRuntimeServiceMBean, SERVER_RUNTIME);
			boolean isSecure = ((Boolean) localConn.getAttribute(serverRuntime, IS_ADMIN_SERVER_PORT_SECURED)).booleanValue();
			String protocol = isSecure ? WEBLOGIC_SECURE_REMOTE_PROTOCOL: WEBLOGIC_INSECURE_REMOTE_PROTOCOL;
			String host = (String) localConn.getAttribute(serverRuntime, ADMIN_SERVER_HOSTNAME);
			int port = ((Integer) localConn.getAttribute(serverRuntime, ADMIN_SERVER_PORT)).intValue();
			jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(protocol, host, port, JNDI_ROOT + serviceName), getJMXContextProps());
			conn = jmxConnector.getMBeanServerConnection();
			
			return true;
		} catch (Exception ex) {
			AppLog.getLogger().error("Error during init of jmxConnector object - The message is [" + ex.getMessage() + "]");
			
			jmxConnector = null;
			conn = null;
			serviceDomainMBean = null;
			return false;
		}
	}
	
	/**
	 * Populate a JNDI context property file with the minimum properties 
	 * required to access a WebLogic MBean server tree
	 * 
	 * @return WebLogic properties file
	 */
	protected Map<String, String> getJMXContextProps() {
		Map<String, String> props = new HashMap<String, String>();
		props.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, WEBLOGIC_PROVIDER_PACKAGES);
		return props;
	}
	
	/**
	 * Returns the current MBeanServer connection to overriding classes.
	 * 
	 * @return The current JMX MBean server connection
	 */
	protected MBeanServerConnection getConn() {
		return conn;
	}
	
	// Members
	private JMXConnector jmxConnector;
	private MBeanServerConnection conn;
	
	/**
	 * 
	 * @return
	 * @throws NamingException
	 */
	private MBeanServerConnection getCachedLocalConn() throws NamingException {
		if (cachedLocalConn == null) {		
			synchronized (WLOsbStats.class) {
				if (cachedLocalConn == null) {
					InitialContext ctx = null;
					
					try {
						ctx = new InitialContext();
						
						for (String jndiLookup : LOCAL_SERVER_RUNTIME_MBEAN_JNDI_LOOKUPS) {
							try {
								cachedLocalConn = (MBeanServer) ctx.lookup(jndiLookup);
								
								if (cachedLocalConn == null) {
									AppLog.getLogger().debug("Unable to locate local server runtime mbean using jndi lookup of: " + jndiLookup);
								} else {
									AppLog.getLogger().debug("Successfully located local server runtime mbean using jndi lookup of: " + jndiLookup);
									break;
								}
							} catch (Exception e) {
								AppLog.getLogger().error("Error attempting to locate local server runtime mbean using jndi lookup of: " + jndiLookup + "  (" + e + ")");
							}
						}
					} finally {
						if (ctx != null) {
							try { ctx.close(); } catch (Exception e) {}
						}
					}
				}
			}
		}
		return cachedLocalConn;
	}
	
	/**
	 * 
	 * @return
	 */
	/*
	private ObjectName getServerRuntime() throws Exception {
		
		ObjectName serverRuntime = (ObjectName)getConn().getAttribute(serverRuntimeServiceMBean, SERVER_RUNTIME);
		if(serverRuntime != null) {
			AppAppLog.getLogger().getLogger().notice("Found the ServerRuntime");
			return serverRuntime;
		} else {
			AppAppLog.getLogger().getLogger().error("Didn't find the ServerRuntime ...");
			return null;
		}
	}
	*/
	
	/**
	 * 
	 */
	private boolean initServiceDomainMBean() {
		
		// -----------------------------------------------------------------------
		try {
			
			if(init(DOMAIN_RUNTIME_SERVICE_NAME)) {
				
				serviceDomainMBean = getServiceDomainMBean(getConn());
				if(serviceDomainMBean != null) {
					AppLog.getLogger().debug("serviceDomainMBean is properly configured");
					return true;
				}
				else {
					AppLog.getLogger().error("Unable to set serviceDomainMBean");
					return false;
				}
			}
			else {
				AppLog.getLogger().error("Unable to set serviceDomainMBean - The execution of init() method failed");
				return false;
			}
		} catch (Exception ex) {
			AppLog.getLogger().error("Unable to set serviceDomainMBean - The error message is [" + ex.getMessage());
			return false;
		}
		// -----------------------------------------------------------------------
				
	}
	
	/**
	 * Main constructor
	 */
	public WLOsbStats() {
				
		// Check if the MBean is instantiated each time of kept in memory for better performance
		// -> Need to know how the data should be retrieved
		if(!initServiceDomainMBean()) {
			String errorMessage = "Unable to create WLOsbStats object";
			//throw new IllegalStateException(errorMessage);
			AppLog.getLogger().error(errorMessage);
		}
	}
	
	/**
	 * Pre-register event handler - returns MBean name.
	 * 
	 * @return name
	 */
	public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
		return name;
	}

	/**
	 * Post-register event handler - logs that started.
	 * 
	 * @param registrationDone Indicates if registration was completed
	 */
	public void postRegister(Boolean registrationDone) {
		AppLog.getLogger().notice("WlOsbStats MBean initialised");
	}

	/**
	 * Pre-deregister event handler - does nothing
	 * 
	 * @throws Exception Indicates problem is post registration
	 */
	public void preDeregister() throws Exception {
	}

	/**
	 * Post-deregister event handler - logs that stopped
	 */
	public void postDeregister() {
		AppLog.getLogger().notice("WlOsbStats MBean destroyed");
	}

	/**
	 * The version of the WLHostMachineStats MBean. 
	 * 
	 * @return The version of WLHostMachineStats MBean
	 */
	public String getMBeanVersion() {
		return WL_OSB_APP_VERSION;
	}
	
	/**
	 * 
	 * @param osbResourceType
	 * @return
	 */
	/*
	public Set<String> getServiceListSet(String osbResourceType) {
		
    	Ref[] references = getRefForOsbType(osbResourceType);
    	if (references != null && references.length > 0) {
    		
    		Set<String> serviceList = new HashSet<String>();
    		for (int index = 0; index < references.length; index ++) {
    			
    			Ref reference = references[index];
    			String serviceName = reference.getLocalName();
    			serviceList.add(serviceName);
    			AppLog.getLogger().debug("Element [" + serviceName + "] added to the list of services with the OSB type [" + osbResourceType + "]");
    		}
        	return serviceList;
    	}
    	return null;
	}
	*/
	
	/**
	 * 
	 * @param osbResourceType
	 * @return
	 */
	/*
	public String[] getServiceList(String osbResourceType) {
		
    	Ref[] references = getRefForOsbType(osbResourceType);
    	if (references != null && references.length > 0) {
    		
    		String[] serviceList = new String[references.length];
    		for (int index = 0; index < references.length; index ++) {
    			
    			Ref reference = references[index];
    			String serviceName = reference.getLocalName();
    			serviceList[index] = serviceName;
    			AppLog.getLogger().debug("Element [" + serviceName + "] added to the list of services with the OSB type [" + osbResourceType + "]");
    		}
        	return serviceList;
    	}
    	return null;
	}
	*/
	
	/**
	 * 
	 * @param serverName
	 * @param osbResourceType
	 * @param resourceType
	 * @param statisticType
	 */
	//public void collectServiceStatistics(String serverName, String osbResourceType, String resourceType, String statisticType) {
	public void collectServiceStatistics(String osbResourceType, String resourceType, String statisticType) {
		//this.statistics = getServiceStatistics(serverName, osbResourceType, resourceType, statisticType);		
		this.statistics = getServiceStatistics(osbResourceType, resourceType, statisticType);
	}
	
	/**
	 * 
	 * @param serverName
	 * @param osbResourceType
	 * @param resourceType
	 */
	//public void collectServiceStatistics(String serverName, String osbResourceType, String resourceType) {
	public void collectServiceStatistics(String osbResourceType, String resourceType) {
				
		//this.statistics = getServiceStatistics(serverName, osbResourceType, resourceType);
		this.statistics = getServiceStatistics(osbResourceType, resourceType);
	}
	
	/**
	 * 
	 * @param serverName
	 * @param osbResourceType
	 * @param resourceType
	 * @return
	 */
	//public Map<String, Map<String, Map<String, Double>>> getServiceStatistics(String serverName, String osbResourceType, String resourceType) {
	public Map<String, Map<String, Map<String, Double>>> getServiceStatistics(String osbResourceType, String resourceType) {
		
		//return getServiceStatistics(serverName, osbResourceType, resourceType, null);
		return getServiceStatistics(osbResourceType, resourceType, null);
		
	}
	
	/**
	 * 
	 * @param serverName
	 * @param osbResourceType
	 * @param resourceTypeString
	 * @param statisticTypeString
	 * @return
	 */
	//public Map<String, Map<String, Map<String, Double>>> getServiceStatistics(String serverName, String osbResourceType, String resourceTypeString, String statisticTypeString) {
	public Map<String, Map<String, Map<String, Double>>> getServiceStatistics(String osbResourceType, String resourceTypeString, String statisticTypeString) {
		
		Map<String, Map<String, Map<String, Double>>> statistics = new LinkedHashMap<>();
		StatisticType statisticType = null;
		
		// -----------------------------------------------------
		// Check if valid input parameter
		if(!isValidOsbResourceType(osbResourceType)) {
			return statistics;
		}

		ResourceType resourceType = getResourceType(resourceTypeString);
		if(resourceType == null) {
			return statistics;
		}
		
		if(statisticTypeString != null) {
			statisticType = getStatisticType(statisticTypeString);
			if(statisticType == null) {
				AppLog.getLogger().warning("Not possible to get StatisticType enum from string value [" + statisticTypeString + "]");
				return statistics;
			}
		}
		// -----------------------------------------------------
		
		// -----------------------------------------------------
    	// It shouldn't happen but ...
    	if(serviceDomainMBean == null) {
    		
    		AppLog.getLogger().warning("serviceDomainMBean is null - Trying to reset it");
    		if(!initServiceDomainMBean()) {
    			AppLog.getLogger().error("Unable to reset serviceDomainMBean ...");
    			return statistics;
    		}
    	}
    	// -----------------------------------------------------
    		
		// -----------------------------------------------------
		// Get the statistics    		
		//HashMap<Ref, ServiceResourceStatistic> statsMap = getDetailsForResourceType(serverName, osbResourceType, resourceType);
    	HashMap<Ref, ServiceResourceStatistic> statsMap = getDetailsForResourceType(osbResourceType, resourceType);
    	// -----------------------------------------------------
    	
    	// -----------------------------------------------------
    	// Process the statistics
    	if(statsMap != null && statsMap.size() > 0) {
    		
    		if(statisticType != null) statistics = processStatistics(statsMap, statisticType);
    		else statistics = processStatistics(statsMap);
    		return statistics;
    	}
/*
else {
	//AppLog.getLogger().notice("getServiceStatistics() - statsMap is null or empty ... for server [" + serverName + "]");
	AppLog.getLogger().notice("getServiceStatistics() - statsMap is null or empty ...");
}
*/
    	// -----------------------------------------------------
    	
		return statistics;
	}
	
	/**
	 * Gets an instance of ServiceDomainMBean from the weblogic server.
	 * 
	 * @param conn
	 * @return
	 */
	private ServiceDomainMBean getServiceDomainMBean(MBeanServerConnection conn) {	
    	InvocationHandler handler = new ServiceDomainMBeanInvocationHandler(conn);
		Object proxy = Proxy.newProxyInstance(ServiceDomainMBean.class.getClassLoader(), new Class[] { ServiceDomainMBean.class }, handler);
		return (ServiceDomainMBean) proxy;
	}
	
	/**
	 * 
	 * @param serverName
	 * @param osbResourceType
	 * @param resourceType
	 * @return
	 */
	//private HashMap<Ref, ServiceResourceStatistic> getDetailsForResourceType(String serverName, String osbResourceType, ResourceType resourceType) {
	private HashMap<Ref, ServiceResourceStatistic> getDetailsForResourceType(String osbResourceType, ResourceType resourceType) {

		try {

			Ref[] references = getRefForOsbType(osbResourceType);

			if (references != null && references.length > 0) {
				AppLog.getLogger().debug("Found [" + references.length + "] elements of type [" + osbResourceType + "]");

				// Bitwise map for desired resource types.
				int typeFlag = 0;
				typeFlag = typeFlag | resourceType.value();

				// Get cluster-level statistics.
				try {
					// Get statistics.
					//HashMap<Ref, ServiceResourceStatistic> statsMap = getStatisticForOsbType(serverName, osbResourceType, references, typeFlag);					
					HashMap<Ref, ServiceResourceStatistic> statsMap = getStatisticForOsbType(osbResourceType, references, typeFlag);
					return statsMap;
				} catch (IllegalArgumentException iae) {

					AppLog.getLogger().error("------------------------------------------------");
					AppLog.getLogger().error("Encountered IllegalArgumentException... Details:");
					AppLog.getLogger().error(iae.getMessage());
					AppLog.getLogger().error("Check if reference OR bitmap are valid... !!!");
					AppLog.getLogger().error("------------------------------------------------");
					throw iae;
				} catch (DomainMonitoringDisabledException dmde) {

					// Statistics not available as monitoring is turned off at domain level.
					AppLog.getLogger().error("------------------------------------------------");
					AppLog.getLogger().error("Statistics not available as monitoring is turned off at domain level.");
					AppLog.getLogger().error("------------------------------------------------");
					throw dmde;
				} catch (MonitoringException me) {

					// Internal problem... May be aggregation server is crashed...
					AppLog.getLogger().error("------------------------------------------------");
					AppLog.getLogger().error("Statistics is not available... Check if aggregation server is crashed...");
					AppLog.getLogger().error("------------------------------------------------");
					throw me;
				}

			} else {
				AppLog.getLogger().error("Didn't find any element with monitoring enabled - Not possible to collect anything for elements of type [" + osbResourceType + "]");
			}

		} catch (Exception ex) {
			AppLog.getLogger().error("Problem to get the details of OSB resource [" + osbResourceType + "] and ResourceType [" + resourceType + "]", ex);
		}
		return null; 
	}
	
	/**
	 * 
	 * @param resourceType
	 * @return
	 */
	private ResourceType getResourceType(String resourceType) {

		try {
			// Try from the "string" enum value
			return ResourceType.valueOf(resourceType.toUpperCase());
		} catch (Exception ex) {

			// Try from internal DH constants (probably coming from WS)
			switch (resourceType) {

				case MonitorProperties.OSB_RESOURCE_TYPE_FLOW_COMPONENT:
					return ResourceType.FLOW_COMPONENT;
	
				case MonitorProperties.OSB_RESOURCE_TYPE_SERVICE:
					return ResourceType.SERVICE;
	
				case MonitorProperties.OSB_RESOURCE_TYPE_WEBSERVICE_OPERATION:
					return ResourceType.WEBSERVICE_OPERATION;
	
				case MonitorProperties.OSB_RESOURCE_TYPE_URI:
					return ResourceType.URI;
	
				default:
					AppLog.getLogger().error("Wrong resourceType [" + resourceType + "]");
					return null;
			}
		}
	}

	/**
	 * 
	 * @param osbResourceType
	 * @return
	 * @throws Exception
	 */
	private Ref[] getRefForOsbType(String osbResourceType) {

		Ref[] refs = null;
		try {
			switch (osbResourceType) {
	
				case MonitorProperties.OSB_PS_TYPE:
					
					//return serviceDomainMBean.getMonitoredProxyServiceRefs();
					refs = serviceDomainMBean.getMonitoredProxyServiceRefs();
					if(refs != null) {
						AppLog.getLogger().notice("getRefForOsbType() - Ref[] contains [" + refs.length + "] elements");
					} else {
						AppLog.getLogger().notice("getRefForOsbType() - Ref[] is null or empty ...");
					}
					
				case MonitorProperties.OSB_BS_TYPE:
					//return serviceDomainMBean.getMonitoredBusinessServiceRefs();
					refs = serviceDomainMBean.getMonitoredBusinessServiceRefs();
					if(refs != null) {
						AppLog.getLogger().notice("getRefForOsbType() - Ref[] contains [" + refs.length + "] elements");
					} else {
						AppLog.getLogger().notice("getRefForOsbType() - Ref[] is null or empty ...");
					}
					return refs;
							
				default:
					AppLog.getLogger().error("Wrong osbResourceType [" + osbResourceType + "] - Must be [" + MonitorProperties.OSB_PS_TYPE + "] or [" + MonitorProperties.OSB_BS_TYPE + "]");
					return null;
			}
		} catch(Exception ex) {
			AppLog.getLogger().error("Unable to get the reference for [" + osbResourceType + "] - Message is [" + ex.getMessage() + "]");
			return null;
		}
	}

	/**
	 * 
	 * @param serverName
	 * @param osbResourceType
	 * @param serviceRefs
	 * @param typeFlag
	 * @return
	 * @throws Exception
	 */
	//private HashMap<Ref, ServiceResourceStatistic> getStatisticForOsbType(String serverName, String osbResourceType, Ref[] serviceRefs, int typeFlag) throws Exception {
	private HashMap<Ref, ServiceResourceStatistic> getStatisticForOsbType(String osbResourceType, Ref[] serviceRefs, int typeFlag) throws Exception {

		if (serviceRefs != null && serviceRefs.length > 0) {

			switch (osbResourceType) {

			case MonitorProperties.OSB_PS_TYPE:
				
				return serviceDomainMBean.getProxyServiceStatistics(serviceRefs, typeFlag, null);
				//return serviceDomainMBean.getProxyServiceStatistics(serviceRefs, typeFlag, serverName);
				
/*
HashMap<Ref, ServiceResourceStatistic> statistics = serviceDomainMBean.getProxyServiceStatistics(serviceRefs, typeFlag, serverName);
if(statistics != null) {
	if(serverName != null) AppLog.getLogger().notice("statistics is not null and contains [" + statistics.size() + "] elements for server [" + serverName + "]");
	else AppLog.getLogger().notice("statistics is not null and contains [" + statistics.size() + "] elements");
} else {
	if(serverName != null) AppLog.getLogger().error("statistics is null for server [" + serverName + "]");
	else AppLog.getLogger().error("statistics is null");
}
*/

			case MonitorProperties.OSB_BS_TYPE:
				//return serviceDomainMBean.getBusinessServiceStatistics(serviceRefs, typeFlag, serverName);
				return serviceDomainMBean.getBusinessServiceStatistics(serviceRefs, typeFlag, null);

			default:
				return null;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param osbResourceType
	 * @return
	 */
	private boolean isValidOsbResourceType(String osbResourceType) {

		switch (osbResourceType) {

		case MonitorProperties.OSB_PS_TYPE:
			return true;

		case MonitorProperties.OSB_BS_TYPE:
			return true;

		default:
			return false;
		}
	}
	
	/**
	 * 
	 * @param statisticType
	 * @return
	 */
	private StatisticType getStatisticType(String statisticType) {

		try {
			// Try from the "string" enum value
			return StatisticType.valueOf(statisticType.toUpperCase());
		} catch (Exception ex) {

			// Try from internal DH constants (probably coming from WS)
			switch (statisticType) {
	
				case MonitorProperties.OSB_STATISTIC_TYPE_COUNT:
					return StatisticType.COUNT;
					
				case MonitorProperties.OSB_STATISTIC_TYPE_INTERVAL:
					return StatisticType.INTERVAL;
	
				case MonitorProperties.OSB_STATISTIC_TYPE_STATUS:
					return StatisticType.STATUS;
	
				default:
					AppLog.getLogger().error("Wrong statisticType [" + statisticType + "]");
					return null;
			}
		}
	}
	
	/**
	 * 
	 * @param statisticType
	 * @param statisticName
	 * @return
	 */
	private boolean isValidStatisticNameForType(String statisticType, String statisticName) {

		try {
			Set<String> statisticNames = MonitorProperties.OSB_STATISTIC_TYPE_TREE.get(statisticType);
			if (statisticNames != null && statisticNames.contains(statisticName)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			AppLog.getLogger().error("Issue during execution of isValidStatisticNameForType method - statisticType, String statisticName");
			return false;
		}
	}
	
	/**
	 * 
	 * @param statsMap
	 * @return
	 */
	private Map<String, Map<String, Map<String, Double>>> processStatistics(HashMap<Ref, ServiceResourceStatistic> statsMap) {
		return processStatistics(statsMap, null);
	}
	
	/**
	 * 
	 * @param statsMap
	 * @param statisticType
	 * @return
	 */
	private Map<String, Map<String, Map<String, Double>>> processStatistics(HashMap<Ref, ServiceResourceStatistic> statsMap, StatisticType statisticType) {
		
		/*
		 * MAP object containing all the informations
		 * 
		 * The KEY is the NAME of the SERVICE (name of PS or BS for example)
		 * The CONTENT is a MAP object having :
		 *    The KEY is the NAME of the ResourceStatistic object
		 *    The CONTENT is a MAP object having :
		 *        The KEY is the NAME of the StatisticValue object
		 *        The CONTENT is the VALUE of the StatisticValue object
		*/
		Map<String, Map<String, Map<String, Double>>> globalStatistics = new LinkedHashMap<>();
				
		// Check input parameters
		if (statsMap == null) {
			return globalStatistics;
		}
		if (statsMap.size() == 0) {
			return globalStatistics;
		}

		Set<Map.Entry<Ref, ServiceResourceStatistic>> set = statsMap.entrySet();
		for (Map.Entry<Ref, ServiceResourceStatistic> mapEntry : set) {

			String serviceName = mapEntry.getKey().getLocalName();
			ServiceResourceStatistic serviceStats = mapEntry.getValue();
			ResourceStatistic[] resStatsArray = null;
			
			try {
				// Get all the statistics
				resStatsArray = serviceStats.getAllResourceStatistics();
			} catch (MonitoringNotEnabledException mnee) {

				// Statistics not available
				AppLog.getLogger().error("--------------------------------------------------------------------");
				AppLog.getLogger().error("Monitoring is not enabled for the service [" + serviceName + "]");
				AppLog.getLogger().error("--------------------------------------------------------------------");
				continue;
			} catch (InvalidServiceRefException isre) {

				// Invalid service
				AppLog.getLogger().error("---------------------------------------------------------------");
				AppLog.getLogger().error("InvalidRef. Maybe the service  [" + serviceName + "] is deleted");
				AppLog.getLogger().error("---------------------------------------------------------------");
				continue;
			} catch (MonitoringException me) {

				// Statistics not available
				AppLog.getLogger().error("--------------------------------------------------------------------");
				AppLog.getLogger().error("Failed to get statistics for the service  [" + serviceName + "]");
				AppLog.getLogger().error("Details: " + me.getMessage());
				AppLog.getLogger().error("--------------------------------------------------------------------");
				continue;
			}
			
			Map<String, Map<String, Double>> services = new LinkedHashMap<>();
			
			// ----------------------------------------------------------------
			// Process statistics
			for (ResourceStatistic resStats : resStatsArray) {

				String resourceStatisticName = resStats.getName();
				Map<String, Double> statisticValues = new LinkedHashMap<>();
				
				// Now get and print statistics for this resource
				StatisticValue[] statValues = resStats.getStatistics();
				for (StatisticValue value : statValues) {

					StatisticType currentStatisticType = value.getType();
					String currentStatisticValueName = value.getName();
					if (!isValidStatisticNameForType(currentStatisticType.toString(), currentStatisticValueName)) {
						AppLog.getLogger().warning("The StatisticValue [" + currentStatisticValueName + "] is [UNKNOWN] for the type: [" + currentStatisticType + "]");
					}
					
					// boolean value to specify is statistic should be returned or not
					boolean isFiltered = true;
					
					// If the statisticType parameter is used
					if(statisticType != null && statisticType == currentStatisticType) {
						
						// If it's the correct type
						if(statisticType == currentStatisticType) {
							
							// statisticType defined and correct value
							isFiltered = true;
						} else {
							
							// statisticType defined but not correct type/value so should be skipped/filtered
							isFiltered = false;
						}
						
					} else {
						// statisticType is not specified so no need to filter anything
						isFiltered = true;
					}
					
					// If relevant statistic
					if(isFiltered) {
												
						// Determine statistics type
						if (currentStatisticType == StatisticType.INTERVAL) {
							
							StatisticValue.IntervalStatistic is = (StatisticValue.IntervalStatistic) value;
							
							// Add the elements to the list
							statisticValues.put(is.getName() + MonitorProperties.OSB_STATISTIC_TYPE_INTERVAL_PROPERTY_COUNT, new Double(is.getCount()));
							statisticValues.put(is.getName() + MonitorProperties.OSB_STATISTIC_TYPE_INTERVAL_PROPERTY_MIN, new Double(is.getMin()));
							statisticValues.put(is.getName() + MonitorProperties.OSB_STATISTIC_TYPE_INTERVAL_PROPERTY_MAX, new Double(is.getMax()));
							statisticValues.put(is.getName() + MonitorProperties.OSB_STATISTIC_TYPE_INTERVAL_PROPERTY_AVERAGE, new Double(is.getAverage()));
							statisticValues.put(is.getName() + MonitorProperties.OSB_STATISTIC_TYPE_INTERVAL_PROPERTY_SUM, new Double(is.getSum()));
							continue;
							
						} else if (value.getType() == StatisticType.COUNT) {
							
							StatisticValue.CountStatistic cs = (StatisticValue.CountStatistic) value;
							
							// Add the elements to the list
							statisticValues.put(cs.getName().toString() + MonitorProperties.OSB_STATISTIC_TYPE_COUNT_PROPERTY_COUNT, new Double(cs.getCount()));
							continue;

						} else if (value.getType() == StatisticType.STATUS) {
							
							// Is used in 12.1.3
							// Doesn't seem to be used in 10.3.6 ...						
							StatisticValue.StatusStatistic ss = (StatisticValue.StatusStatistic) value;
							
							// Add the elements to the list
							statisticValues.put(ss.getName() + MonitorProperties.OSB_STATISTIC_TYPE_STATUS_PROPERTY_INITIAL, new Double(ss.getInitialStatus()));
							statisticValues.put(ss.getName() + MonitorProperties.OSB_STATISTIC_TYPE_STATUS_PROPERTY_CURRENT, new Double(ss.getCurrentStatus()));
							continue;
						}
					} else {
						AppLog.getLogger().warning("The statistic [] is not considered as relevant - Will not be part of statistics information");
					}
				}
				
				// Add the StatisticValue (NAME as KEY and VALUE as CONTENT)
				services.put(resourceStatisticName, statisticValues);
			}
			
			// Add the service informations
			globalStatistics.put(serviceName, services);
			// ----------------------------------------------------------------
		}
		return globalStatistics;
	}
	
	/**
	 * 
	 * @param statsMap
	 * @return
	 */
	/*
	private Map<String, Map<String, Map<String, String>>> processStatistics(HashMap<Ref, ServiceResourceStatistic> statsMap) {
	*/	
		/*
		 * MAP object containing all the informations
		 * 
		 * The KEY is the NAME of the SERVICE (name of PS or BS for example)
		 * The CONTENT is a MAP object having :
		 *    The KEY is the NAME of the ResourceStatistic object
		 *    The CONTENT is a MAP object having :
		 *        The KEY is the NAME of the StatisticValue object
		 *        The CONTENT is the VALUE of the StatisticValue object
		*/
	/*
		Map<String, Map<String, Map<String, String>>> globalStatistics = new LinkedHashMap<>();
				
		// Check input parameters
		if (statsMap == null) {
			return globalStatistics;
		}
		if (statsMap.size() == 0) {
			return globalStatistics;
		}

		Set<Map.Entry<Ref, ServiceResourceStatistic>> set = statsMap.entrySet();
		for (Map.Entry<Ref, ServiceResourceStatistic> mapEntry : set) {

			//String serviceName = mapEntry.getKey().getGlobalName();
			String serviceName = mapEntry.getKey().getLocalName();
			ServiceResourceStatistic serviceStats = mapEntry.getValue();
			ResourceStatistic[] resStatsArray = null;
			
			try {
				// Get all the statistics
				resStatsArray = serviceStats.getAllResourceStatistics();
			} catch (MonitoringNotEnabledException mnee) {

				// Statistics not available
				AppLog.getLogger().error("--------------------------------------------------------------------");
				AppLog.getLogger().error("Monitoring is not enabled for the service [" + serviceName + "]");
				AppLog.getLogger().error("--------------------------------------------------------------------");
				continue;
			} catch (InvalidServiceRefException isre) {

				// Invalid service
				AppLog.getLogger().error("---------------------------------------------------------------");
				AppLog.getLogger().error("InvalidRef. Maybe the service  [" + serviceName + "] is deleted");
				AppLog.getLogger().error("---------------------------------------------------------------");
				continue;
			} catch (MonitoringException me) {

				// Statistics not available
				AppLog.getLogger().error("--------------------------------------------------------------------");
				AppLog.getLogger().error("Failed to get statistics for the service  [" + serviceName + "]");
				AppLog.getLogger().error("Details: " + me.getMessage());
				AppLog.getLogger().error("--------------------------------------------------------------------");
				continue;
			}
			
			Map<String, Map<String, String>> services = new LinkedHashMap<>();
			
			// ----------------------------------------------------------------
			// Process statistics
			for (ResourceStatistic resStats : resStatsArray) {

				String resourceStatisticName = resStats.getName();
				Map<String, String> statisticValues = new LinkedHashMap<>();
				
				// Now get and print statistics for this resource
				StatisticValue[] statValues = resStats.getStatistics();
				for (StatisticValue value : statValues) {

					StatisticType currentStatisticType = value.getType();
					String currentStatisticValueName = value.getName();
					if (!isValidStatisticNameForType(currentStatisticType.toString(), currentStatisticValueName)) {
						AppLog.getLogger().warning("The StatisticValue [" + currentStatisticValueName + "] is [UNKNOWN] for the type: [" + currentStatisticType + "]");
					}
					
					// Determine statistics type
					if (currentStatisticType == StatisticType.INTERVAL) {
							
//AppLog.getLogger().notice("processStatistics - The value [" + currentStatisticValueName + " is [INTERVAL]");

						StatisticValue.IntervalStatistic is = (StatisticValue.IntervalStatistic) value;
					
						// Add the elements to the list
						statisticValues.put(is.getName() + MonitorProperties.OSB_STATISTIC_TYPE_INTERVAL_PROPERTY_COUNT, new Long(is.getCount()).toString());
						statisticValues.put(is.getName() + MonitorProperties.OSB_STATISTIC_TYPE_INTERVAL_PROPERTY_MIN, new Long(is.getMin()).toString());
						statisticValues.put(is.getName() + MonitorProperties.OSB_STATISTIC_TYPE_INTERVAL_PROPERTY_MAX, new Long(is.getMax()).toString());
						statisticValues.put(is.getName() + MonitorProperties.OSB_STATISTIC_TYPE_INTERVAL_PROPERTY_AVERAGE, new Long(is.getAverage()).toString());
						statisticValues.put(is.getName() + MonitorProperties.OSB_STATISTIC_TYPE_INTERVAL_PROPERTY_SUM, new Double(is.getSum()).toString());
						continue;
						
					} else if (value.getType() == StatisticType.COUNT) {

//AppLog.getLogger().notice("processStatistics - The value [" + currentStatisticValueName + " is [COUNT]");
						
						StatisticValue.CountStatistic cs = (StatisticValue.CountStatistic) value;
						
						// Add the elements to the list
						statisticValues.put(cs.getName().toString() + MonitorProperties.OSB_STATISTIC_TYPE_COUNT_PROPERTY_COUNT, new Long(cs.getCount()).toString());
						continue;
	
					} else if (value.getType() == StatisticType.STATUS) {

//AppLog.getLogger().notice("processStatistics - The value [" + currentStatisticValueName + " is [STATUS]");
						
						// Is used in 12.1.3
						// Doesn't seem to be used in 10.3.6 ...						
						StatisticValue.StatusStatistic ss = (StatisticValue.StatusStatistic) value;
						
						// Add the elements to the list
						statisticValues.put(ss.getName() + MonitorProperties.OSB_STATISTIC_TYPE_STATUS_PROPERTY_INITIAL, new Integer(ss.getInitialStatus()).toString());
						statisticValues.put(ss.getName() + MonitorProperties.OSB_STATISTIC_TYPE_STATUS_PROPERTY_CURRENT, new Integer(ss.getCurrentStatus()).toString());
						continue;
					}
				}
				
				// Add the StatisticValue (NAME as KEY and VALUE as CONTENT)
				services.put(resourceStatisticName, statisticValues);
			}
			
			// Add the service informations
			globalStatistics.put(serviceName, services);
			// ----------------------------------------------------------------
		}
		return globalStatistics;
	}
	*/

	/**
	 * 
	 * @param statsMap
	 * @throws Exception
	 */
	/*
	private void printStatistics(HashMap<Ref, ServiceResourceStatistic> statsMap) {

		if (statsMap == null) {
			AppLog.getLogger().warning("------------------------------------------------------");
			AppLog.getLogger().warning("ServiceResourceStatistics is null... Nothing to report");
			AppLog.getLogger().warning("------------------------------------------------------");
			return;
		}
		if (statsMap.size() == 0) {
			AppLog.getLogger().warning("-------------------------------------------------------");
			AppLog.getLogger().warning("ServiceResourceStatistics is empty... Nothing to report");
			AppLog.getLogger().warning("------------------------------------------------------");
			return;
		}

		Set<Map.Entry<Ref, ServiceResourceStatistic>> set = statsMap.entrySet();

		// Print statistical information of each service
		for (Map.Entry<Ref, ServiceResourceStatistic> mapEntry : set) {

			AppLog.getLogger().notice("");
			AppLog.getLogger().notice("----- Printing statistics for service [" + mapEntry.getKey().getLocalName() + "] -----");

			ServiceResourceStatistic serviceStats = mapEntry.getValue();
			ResourceStatistic[] resStatsArray = null;
			
			try {
				// Get all the statistics
				resStatsArray = serviceStats.getAllResourceStatistics();
			} catch (MonitoringNotEnabledException mnee) {

				// Statistics not available
				AppLog.getLogger().error("--------------------------------------------------------------------");
				AppLog.getLogger().error("Monitoring is not enabled for this service - Please check ...");
				AppLog.getLogger().error("--------------------------------------------------------------------");
				continue;
			} catch (InvalidServiceRefException isre) {

				// Invalid service
				AppLog.getLogger().error("---------------------------------------------------------------");
				AppLog.getLogger().error("InvalidRef. Maybe this service is deleted - Please check ...");
				AppLog.getLogger().error("---------------------------------------------------------------");
				continue;
			} catch (MonitoringException me) {

				// Statistics not available
				AppLog.getLogger().error("--------------------------------------------------------------------");
				AppLog.getLogger().error("Failed to get statistics for this service...");
				AppLog.getLogger().error("Details: " + me.getMessage());
				AppLog.getLogger().error("--------------------------------------------------------------------");
				continue;
			}

			// ----------------------------------------------------------------
			// Print statistics
			for (ResourceStatistic resStats : resStatsArray) {

				// Print resource information
				AppLog.getLogger().notice("");
				AppLog.getLogger().notice("Resource name: [" + resStats.getName() + "] - Resource type: [" + resStats.getResourceType().toString() + "]");
								
				// Now get and print statistics for this resource
				StatisticValue[] statValues = resStats.getStatistics();
				for (StatisticValue value : statValues) {

					String statisticValueType = value.getType().toString();
					String statisticValueName = value.getName();
					if (!isValidStatisticNameForType(statisticValueType, value.getName())) {
						AppLog.getLogger().notice("The StatisticValue with name: [" + statisticValueName + "] is [UNKNOWN] for the type: [" + statisticValueType + "]");
					}

					// Determine statistics type
					if (value.getType() == StatisticType.INTERVAL) {

						StatisticValue.IntervalStatistic is = (StatisticValue.IntervalStatistic) value;

						// Print interval statistics values
						AppLog.getLogger().notice("    Cnt Value: [" + is.getCount() + "]");
						AppLog.getLogger().notice("    Min Value: [" + is.getMin() + "]");
						AppLog.getLogger().notice("    Max Value: [" + is.getMax() + "]");
						AppLog.getLogger().notice("    Sum Value: [" + is.getSum() + "]");
						AppLog.getLogger().notice("    Avg Value: [" + is.getAverage() + "]");
						continue;
						
					} else if (value.getType() == StatisticType.COUNT) {

						StatisticValue.CountStatistic cs = (StatisticValue.CountStatistic) value;
						
						// Print count statistics value
						AppLog.getLogger().notice("    Cnt Value: [" + cs.getCount() + "]");
						continue;

					} else if (value.getType() == StatisticType.STATUS) {

						// Is used in 12.1.3
						// Doesn't seem to be used in 10.3.6 ...
						StatisticValue.StatusStatistic ss = (StatisticValue.StatusStatistic) value;
						
						// Print count statistics value
						AppLog.getLogger().notice("    Initial Status: [" + ss.getInitialStatus() + "]");
						AppLog.getLogger().notice("    Current Status: [" + ss.getCurrentStatus() + "]");
						continue;
					}
				}
			}
			// ----------------------------------------------------------------
		}
	}*/
	
	/**
	 * 
	 * @param globalStatistics
	 * 
	 * MAP object containing all the informations
	 * 
	 * The KEY is the NAME of the SERVICE (name of PS or BS for example)
	 * The CONTENT is a MAP object having :
	 *    The KEY is the NAME of the ResourceStatistic object
	 *    The CONTENT is a MAP object having :
	 *        The KEY is the NAME of the StatisticValue object
	 *        The CONTENT is the VALUE of the StatisticValue object
	 */
	private void printOsbStatistic(Map<String, Map<String, Map<String, Double>>> globalStatistics) {

		try {
			if(globalStatistics != null && globalStatistics.size() > 0) {
				
				Iterator<String> serviceKeys = globalStatistics.keySet().iterator();
				while(serviceKeys.hasNext()) {
					
					String serviceName = serviceKeys.next();
					
					AppLog.getLogger().notice("------------------------------------------------------------------");
					AppLog.getLogger().notice("Service [" + serviceName + "]");
					AppLog.getLogger().notice("--------------------------------------------");
					
					Map<String, Map<String, Double>> services = globalStatistics.get(serviceName);
					Iterator<String> resourceStatisticKeys = services.keySet().iterator();
					while(resourceStatisticKeys.hasNext()) {
						
						String resourceStatisticName = resourceStatisticKeys.next();
						Map<String, Double> statistics = services.get(resourceStatisticName);
						
						AppLog.getLogger().notice("-- ResourceStatisticName  [" + resourceStatisticName + "]");
						
						Iterator<String> statisticsKeys = statistics.keySet().iterator();
						while(statisticsKeys.hasNext()) {
							
							String statisticName = statisticsKeys.next();
							Double statisticValue = statistics.get(statisticName);
							
							AppLog.getLogger().notice("---- Statistic Name [" + statisticName + "] - Value [" + statisticValue + "]");
						}
					}
					AppLog.getLogger().notice("------------------------------------------------------------------");
				}
			}
		} catch (Exception ex) {
			
			AppLog.getLogger().notice("Error during printOsbStatistic method .. - Message is [" + ex.getMessage() + "]");
		}
	}
		
	/**
	 * @return
	 */
	public Set<String> getOsbServiceList() {

		if(statistics != null && statistics.size() > 0) {
			return statistics.keySet();
		}
		return null;
	}
	
	/**
	 * @param statistics
	 * @return
	 */
	public Set<String> getOsbServiceList(Map<String, Map<String, Map<String, Double>>> statistics) {

		if(statistics != null && statistics.size() > 0) {
			return statistics.keySet();
		}
		return null;
	}
	
	/**
	 * 
	 * @param serviceName
	 * @return
	 */
	public Set<String> getOsbResourceStatisticList(String serviceName) {
		
		if(statistics != null && statistics.size() > 0) {
			return statistics.get(serviceName).keySet();
		}
		return null;
	}
	
	/**
	 * 
	 * @param statistics
	 * @param serviceName
	 * @return
	 */
	public Set<String> getOsbResourceStatisticList(Map<String, Map<String, Map<String, Double>>> statistics, String serviceName) {
		
		if(statistics != null && statistics.size() > 0) {
			return statistics.get(serviceName).keySet();
		}
		return null;
	}
	
	/**
	 * 
	 * @param statistics
	 * @param serviceName
	 * @param resourceStatisticName
	 * @return
	 */
	public Set<String> getOsbStatisticList(String serviceName, String resourceStatisticName) {
		
		if(statistics != null && statistics.size() > 0) {			
			return statistics.get(serviceName).get(resourceStatisticName).keySet();
		}
		return null;
	}
	
	/**
	 * 
	 * @param statistics
	 * @param serviceName
	 * @param resourceStatisticName
	 * @return
	 */
	public Set<String> getOsbStatisticList(Map<String, Map<String, Map<String, Double>>> statistics, String serviceName, String resourceStatisticName) {
		
		if(statistics != null && statistics.size() > 0) {			
			return statistics.get(serviceName).get(resourceStatisticName).keySet();
		}
		return null;
	}
	
	/**
	 * 
	 * @param statistics
	 * @param serviceName
	 * @param resourceStatisticName
	 * @param statisticName
	 * @return
	 */
	public double getValueForOsbStatistic(String serviceName, String resourceStatisticName, String statisticName) {
		
		if(statistics != null) {
			
			try {
				
				/*
				Map<String, Map<String, String>> resourceStatistics = statistics.get(serviceName);				
				Map<String, String> statisticValues = resourceStatistics.get(resourceStatisticName);				
				String statisticValue = statisticValues.get(statisticName);
				return statisticValue;
				*/
				
				return statistics.get(serviceName).get(resourceStatisticName).get(statisticName);
				
			} catch (Exception ex) {
				
				AppLog.getLogger().notice("Not possible to get the value of the statistic [" + serviceName + "/" + resourceStatisticName + "/" + statisticName + "] - Message is [" + ex.getMessage() + "]");
			}
		} else {
			AppLog.getLogger().error("Not possible to extract statictic value - The statistic object is null");
		}
		return -1;
	}
	
	/**
	 * 
	 * @param statistics
	 * @param serviceName
	 * @param resourceStatisticName
	 * @param statisticName
	 * @return
	 */
	public double getValueForOsbStatistic(Map<String, Map<String, Map<String, Double>>> statistics, String serviceName, String resourceStatisticName, String statisticName) {
		
		if(statistics != null) {
			
			try {
					
				/*
				Map<String, Map<String, String>> resourceStatistics = statistics.get(serviceName);
				Map<String, String> statisticValues = resourceStatistics.get(resourceStatisticName);
				String statisticValue = statisticValues.get(statisticName);
				return statisticValue;
				*/
				
				return statistics.get(serviceName).get(resourceStatisticName).get(statisticName);
				
			} catch (Exception ex) {
				
				AppLog.getLogger().notice("Not possible to get the value of the statistic [" + serviceName + "/" + resourceStatisticName + "/" + statisticName + "] - Message is [" + ex.getMessage() + "]");
			}
		} else {
			AppLog.getLogger().error("Not possible to extract statictic value - The statistic object is null");
		}
		return -1;
	}
}
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

import static domainhealth.core.jmx.WebLogicMBeanPropConstants.DOMAIN_VERSION;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.bea.wli.config.Ref;
import com.bea.wli.monitoring.DomainMonitoringDisabledException;
import com.bea.wli.monitoring.InvalidServiceRefException;
import com.bea.wli.monitoring.MonitoringException;
import com.bea.wli.monitoring.MonitoringNotEnabledException;
import com.bea.wli.monitoring.ResourceStatistic;
import com.bea.wli.monitoring.ResourceType;
import com.bea.wli.monitoring.ServiceDomainMBean;
import com.bea.wli.monitoring.ServiceResourceStatistic;

import domainhealth.core.env.AppLog;
import domainhealth.core.jmx.DomainRuntimeServiceMBeanConnection;
import domainhealth.core.jmx.WebLogicMBeanException;
import domainhealth.core.jmx.WebLogicMBeanPropConstants;
import weblogic.logging.NonCatalogLogger;

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
	
	// Constants
	private static final String WL_OSB_APP_NAME = "WLOsbStats";
	private static final String WL_OSB_APP_VERSION = "0.0.1";
		
	// Members 
	private final NonCatalogLogger log;
	
	private ServiceDomainMBean serviceDomainMBean = null;
	
	private String elementName = null;
	
	/*
	// Constants
	private static final String DOMAIN_RUNTIME_SERVICE_NAME = "weblogic.management.mbeanservers.domainruntime";
	private static final ObjectName domainRuntimeServiceMBean;

	static {
		try {
			domainRuntimeServiceMBean = new ObjectName("com.bea:Name=DomainRuntimeService,Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean");
		} catch (MalformedObjectNameException e) {
			throw new AssertionError(e.toString());
		}
	}
	*/
	
	/**
	 * Main constructor
	 * 
	 * @param conn
	 */
// Define the right constructor and update WLOsbStatsMBeanRegistrar in consequence
	
	//public WLOsbStats(ServiceDomainMBean serviceDomainMBean) {
	//public WLOsbStats(DomainRuntimeServiceMBeanConnection conn) {
	public WLOsbStats() {
		
		log = new NonCatalogLogger(WL_OSB_APP_NAME);
		
// Check if the MBean is instanciated each time of kept in memory for better performance
// -> Need to know how the datas should be retrieved
log.error("In contructor of WLOsbStats MBean");
		
		//initServiceDomainMBean(conn);
		
		// Init the connectivity to the ServiceDomain
		// Collect the metrics for the specific element "referenceName"
		// Init all the attributes used by "get" methods
		// 
	}
	
	/**
	 * Pre-register event handler - returns MBean name.
	 * 
	 * @return name
	 */
	public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
		
//MBeanServerConnection connection = server.getMBeanServerConnection();
//server.geAtttribute(domainRuntimeServiceMBean);
		
// !!!
// Check how to get a DomainConnection from MBeanServer
// Avoid import on OSB/ALSB package -> load a OSB class to detect if the WL domain is really an OSB domain (deployment of the application will not crash if it's not the case)
// -> Methods should return -1 if it's not a OSB domain
// This MBean should be deployed only on ADMIN as DH is
// !!!
		
		return name;
	}

	/**
	 * Post-register event handler - logs that started.
	 * 
	 * @param registrationDone Indicates if registration was completed
	 */
	public void postRegister(Boolean registrationDone) {
		
		try {
			DomainRuntimeServiceMBeanConnection conn = new DomainRuntimeServiceMBeanConnection();
			initServiceDomainMBean(conn);
		} catch (WebLogicMBeanException wlEx) {
			
			log.error("Unable to get DomainRuntimeMBean due to " + wlEx.getMessage());
			//wlEx.printStackTrace();
		}
		
		log.notice("WlOsbStats MBean initialised");
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
		log.notice("WlOsbStats MBean destroyed");
	}

	/**
	 * The version of the WLHostMachineStats MBean. 
	 * Format: "x.x.x". Example: "0.1.0".
	 * 
	 * @return The version of WLHostMachineStats MBean
	 */
	public String getMBeanVersion() {
		return WL_OSB_APP_VERSION;
	}
	
	/**
	 * 
	 */
	public void setElementName(String elementName) {
		
		this.elementName = elementName;	
	}
	
	/**
	 * 
	 */
	public double getCount() {
		
		return 10;
	}
	
	/**
	 * 
	 */
	// "Count" property for a specific elementName
	public double getCount(String elementName) {
		return 20;
	}

	/**
	 * 
	 */
	// "Min" property for a specific elementName
	public double getMin(String elementName) {
		return 25;
	}

	/**
	 * 
	 */
	// "Max" property for a specific elementName
	public double getMax(String elementName) {
		return 50;
	}

	/**
	 * 
	 */
	// "Avg" property for a specific elementName
	public double getAvg(String elementName) {
		return 3;
	}

	/**
	 * 
	 */
	// "Sum" property for a specific elementName
	public double getSum(String elementName) {
		return 250;
	}

	/**
	 * 
	 */
	public Set<String> getProxyServiceList() {
		
		Set<String> resourceList = new LinkedHashSet<String>();
		resourceList.add("ProxyServiceName1");
		resourceList.add("ProxyServiceName2");
		resourceList.add("ProxyServiceName3");
		return resourceList;
	}
	
	/**
	 * 
	 */
	public Set<String> getBusinessServiceList() {
	
		Set<String> resourceList = new LinkedHashSet<String>();
		resourceList.add("BusinessServiceName1");
		resourceList.add("BusinessServiceName2");
		resourceList.add("BusinessServiceName3");
		return resourceList;
	}
	
	/**
     * 
     * @param conn
     * @return
     * @throws Exception
     */
/*
    private void collectProxyServices(DomainRuntimeServiceMBeanConnection conn) throws Exception {

// -------------------------------------------
AppLog.getLogger().notice("");

initServiceDomainMBean(conn);
Ref[] serviceRefs = serviceDomainMBean.getMonitoredProxyServiceRefs();
if(serviceRefs != null && serviceRefs.length > 0) {
	AppLog.getLogger().notice("Found [" + serviceRefs.length + "] Proxy services");
	
	// Create a bitwise map for desired resource types.
    int typeFlag = 0;
    typeFlag = typeFlag | ResourceType.SERVICE.value();
    typeFlag = typeFlag | ResourceType.FLOW_COMPONENT.value();
    typeFlag = typeFlag | ResourceType.WEBSERVICE_OPERATION.value();
    
    HashMap<Ref, ServiceResourceStatistic> resourcesMap = null;
    
    // Get cluster-level statistics.
    try {
         // Get statistics.
         AppLog.getLogger().notice("Now trying to get statistics for [" + serviceRefs.length + "] proxy services...");
         resourcesMap = serviceDomainMBean.getProxyServiceStatistics(serviceRefs, typeFlag, null);
         
         // Print Statistic
         printStatistics(resourcesMap);
    }
    catch (IllegalArgumentException iae) {
    	
         AppLog.getLogger().error("------------------------------------------------");
         AppLog.getLogger().error("Encountered IllegalArgumentException... Details:");
         AppLog.getLogger().error(iae.getMessage());
         AppLog.getLogger().error("Check if proxy reference OR bitmap are valid... !!!");
         AppLog.getLogger().error("------------------------------------------------");
         throw iae;
    }
    catch (DomainMonitoringDisabledException dmde) {
    	
         // Statistics not available as monitoring is turned off at domain level.
         AppLog.getLogger().error("------------------------------------------------");
         AppLog.getLogger().error("Statistics not available as monitoring is turned off at domain level.");
         AppLog.getLogger().error("------------------------------------------------");
         throw dmde;
    }
    catch (MonitoringException me) {
    	
         // Internal problem... May be aggregation server is crashed...
         AppLog.getLogger().error("------------------------------------------------");
         AppLog.getLogger().error("Statistics is not available... Check if aggregation server is crashed...");
         AppLog.getLogger().error("------------------------------------------------");
         throw me;
    }
} else {
	AppLog.getLogger().warning("Didn't find any Proxy services with monitoring enabled - Not possible to collect anything");
}
AppLog.getLogger().notice("");
//-------------------------------------------

    }
*/
	
    /**
     * 
     * @param conn
     * @return
     * @throws Exception
     */
/*
    private void collectBusinessServices(DomainRuntimeServiceMBeanConnection conn) throws Exception {

// -------------------------------------------
AppLog.getLogger().notice("");

initServiceDomainMBean(conn);
Ref[] serviceRefs = serviceDomainMBean.getMonitoredBusinessServiceRefs();
if(serviceRefs != null && serviceRefs.length > 0) {
	AppLog.getLogger().notice("Found [" + serviceRefs.length + "] Business services");
	
	// Create a bitwise map for desired resource types.
    int typeFlag = 0;
    typeFlag = typeFlag | ResourceType.SERVICE.value();
    typeFlag = typeFlag | ResourceType.WEBSERVICE_OPERATION.value();
    typeFlag = typeFlag | ResourceType.URI.value();
    
    HashMap<Ref, ServiceResourceStatistic> resourcesMap = null;
    
    // Get cluster-level statistics.
    try {
         // Get statistics.
         AppLog.getLogger().notice("Now trying to get statistics for [" + serviceRefs.length + "] business services...");
         resourcesMap = serviceDomainMBean.getBusinessServiceStatistics(serviceRefs, typeFlag, null);
         
         // Print Statistic
         printStatistics(resourcesMap);
    }
    catch (IllegalArgumentException iae) {
    	
         AppLog.getLogger().error("------------------------------------------------");
         AppLog.getLogger().error("Encountered IllegalArgumentException... Details:");
         AppLog.getLogger().error(iae.getMessage());
         AppLog.getLogger().error("Check if business reference OR bitmap are valid... !!!");
         AppLog.getLogger().error("------------------------------------------------");
         throw iae;
    }
    catch (DomainMonitoringDisabledException dmde) {
    	
         // Statistics not available as monitoring is turned off at domain level.
         AppLog.getLogger().error("------------------------------------------------");
         AppLog.getLogger().error("Statistics not available as monitoring is turned off at domain level.");
         AppLog.getLogger().error("------------------------------------------------");
         throw dmde;
    }
    catch (MonitoringException me) {
    	
         // Internal problem... May be aggregation server is crashed...
         AppLog.getLogger().error("------------------------------------------------");
         AppLog.getLogger().error("Statistics is not available... Check if aggregation server is crashed...");
         AppLog.getLogger().error("------------------------------------------------");
         throw me;
    }
} else {
	AppLog.getLogger().warning("Didn't find any Business services with monitoring enabled - Not possible to collect anything");
}
AppLog.getLogger().notice("");
//-------------------------------------------

    }
*/
	
    /**
     * 
     * @param statsMap
     * @throws Exception
     */
/*
    private void printStatistics(HashMap<Ref, ServiceResourceStatistic> statsMap) throws Exception {
         
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
            AppLog.getLogger().notice("======= Printing statistics for service [" + mapEntry.getKey().getFullName() + "] =======");
			
			ServiceResourceStatistic serviceStats = mapEntry.getValue();

              ResourceStatistic[] resStatsArray = null;
              try {
                   resStatsArray = serviceStats.getAllResourceStatistics();
              }
              catch (MonitoringNotEnabledException mnee) {
                   
            	   // Statistics not available
            	   AppLog.getLogger().error("--------------------------------------------------------------------");
                   AppLog.getLogger().error("Monitoring is not enabled for this service - Please do something ...");
                   AppLog.getLogger().error("--------------------------------------------------------------------");
                   continue;
              }
              
              catch (InvalidServiceRefException isre) {
                   
            	   // Invalid service
            	   AppLog.getLogger().error("---------------------------------------------------------------");
                   AppLog.getLogger().error("InvalidRef. Maybe this service is deleted - Please do something");
                   AppLog.getLogger().error("---------------------------------------------------------------");
                   continue;
              }

              catch (MonitoringException me) {
                   
            	   // Statistics not available
            	   AppLog.getLogger().error("--------------------------------------------------------------------");
                   AppLog.getLogger().error("Failed to get statistics for this service...");
                   AppLog.getLogger().error("Details: " + me.getMessage());
                   //me.printStackTrace();
                   AppLog.getLogger().error("--------------------------------------------------------------------");
                   continue;
              }
              
			  // Print statistics
              for (ResourceStatistic resStats : resStatsArray) {
            	  
                   // Print resource information
            	   AppLog.getLogger().notice("");
                   AppLog.getLogger().notice("Resource name: [" + resStats.getName() + "] - Resource type: [" + resStats.getResourceType().toString() + "]");

                   // Now get and print statistics for this resource
                   StatisticValue[] statValues = resStats.getStatistics();
                   for (StatisticValue value : statValues) {
                	   
                        AppLog.getLogger().notice("  Statistic Name: [" + value.getName() + "] - Statistic Type: [" + value.getType().toString() + "]");

                        // Determine statistics type
                        if (value.getType() == StatisticType.INTERVAL) {
                        	
                             StatisticValue.IntervalStatistic is = (StatisticValue.IntervalStatistic)value;

                             // Print interval statistics values
                             AppLog.getLogger().notice("    Cnt Value: [" + is.getCount() + "]");
                             AppLog.getLogger().notice("    Min Value: [" + is.getMin() + "]");
                             AppLog.getLogger().notice("    Max Value: [" + is.getMax() + "]");
                             AppLog.getLogger().notice("    Sum Value: [" + is.getSum() + "]");
                             AppLog.getLogger().notice("    Avg Value: [" + is.getAverage() + "]");
                        }
                        else if (value.getType() == StatisticType.COUNT) {
                        	
                             StatisticValue.CountStatistic cs = (StatisticValue.CountStatistic) value;

                             // Print count statistics value
                             AppLog.getLogger().notice("    Cnt Value: [" + cs.getCount() + "]");
                        }
                        else if (value.getType() == StatisticType.STATUS) {
                        	
                             StatisticValue.StatusStatistic ss = (StatisticValue.StatusStatistic)value;
                             // Print count statistics value
                             AppLog.getLogger().notice("    Initial Status: [" + ss.getInitialStatus() + "]");
                             AppLog.getLogger().notice("    Current Status: [" + ss.getCurrentStatus() + "]");
                        }
                   }
              }
              AppLog.getLogger().notice("=========================================");
         }
    }
*/
	
	/**
	 * Gets an instance of ServiceDomainMBean from the weblogic server.
	 *
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @throws Exception
	 */
    private void initServiceDomainMBean(DomainRuntimeServiceMBeanConnection conn) throws WebLogicMBeanException {    	
    	InvocationHandler handler = new ServiceDomainMBeanInvocationHandler(conn.getJMXConnector());
		Object proxy = Proxy.newProxyInstance(ServiceDomainMBean.class.getClassLoader(), new Class[] { ServiceDomainMBean.class }, handler);
		serviceDomainMBean = (ServiceDomainMBean) proxy;
	}
    
    /**
	 * Determine the running WebLogic domain's version.
	 * 
	 * @return The version text (e.g. 10.3.5)
	 */
    /*
	private String getWLSDomainVersion() {
		String version = null;
		DomainRuntimeServiceMBeanConnection conn = null;
		
		try {
			conn = new DomainRuntimeServiceMBeanConnection();
			ObjectName domainConfig = conn.getDomainConfiguration();
			version = conn.getTextAttr(domainConfig, DOMAIN_VERSION);
		} catch (WebLogicMBeanException e) {
			// Assume caused by "DomainVersion" attribute not existing which
			// would indicate that this is a 9.0 or 9.1 domain version
			version = DEFAULTED_WLS_VERSION;
		} finally {
			if (conn != null) {
				conn.close();
			}
		}	

		return version;
	}
	*/
}
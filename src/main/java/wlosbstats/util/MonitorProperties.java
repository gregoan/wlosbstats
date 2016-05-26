//Copyright (C) 2008-2013 Paul Done . All rights reserved.
//This file is part of the DomainHealth software distribution. Refer to the  
//file LICENSE in the root of the DomainHealth distribution.
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
package wlosbstats.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 * Map of WebLogic MBean property names and their associated attributes 
 * (property name, property title, property units). Also provides constants
 * listing all the attributes that should be monitored.
 */
public class MonitorProperties {
	
	// -----------------------------------------------------------------
	// OSB resources
	// -------------
	public final static String OSB_PS_TYPE = "osb_ps";
	public final static String OSB_BS_TYPE = "osb_bs";
	
	// -----------------------------------------------------------------
	// OSB ResourceType
	// ----------------
	public final static String OSB_RESOURCE_TYPE_SERVICE = "service";
	public final static String OSB_RESOURCE_TYPE_WEBSERVICE_OPERATION = "webserviceoperation";
	public final static String OSB_RESOURCE_TYPE_FLOW_COMPONENT = "flowcomponent";
	public final static String OSB_RESOURCE_TYPE_URI = "uri";
	
	// -----------------------------------------------------------------
	// OSB StatisticType
	// -----------------
	public final static String OSB_STATISTIC_TYPE_COUNT = "COUNT";
	public final static String OSB_STATISTIC_TYPE_INTERVAL = "INTERVAL";
	public final static String OSB_STATISTIC_TYPE_STATUS = "STATUS";
	// -----------------------------------------------------------------
			
	// -----------------------------------------------------------------------------
	// https://docs.oracle.com/cd/E28280_01/admin.1111/e15867/app_jmx_monitoring.htm
	// -----------------------------------------------------------------------------

	// -----------------------------------------------------------------
	// StatisticType (COUNT)
	// ---------------------
	public final static String OSB_MESSAGE_COUNT_PROPERTY = "message-count";
	public final static String OSB_ERROR_COUNT_PROPERTY = "error-count";
	public final static String OSB_FAILOVER_COUNT_PROPERTY = "failover-count";
	public final static String OSB_WSS_ERROR_COUNT_PROPERTY = "wss-error";
	public final static String OSB_FAILURE_RATE_COUNT_PROPERTY = "failure-rate";
	public final static String OSB_SUCCESS_RATE_COUNT_PROPERTY = "success-rate";
	public final static String OSB_SEVERITY_ALL = "severity-all";
	public final static String OSB_SLA_SEVERITY_WARNING_COUNT_PROPERTY = "sla-severity-warning";
	public final static String OSB_SLA_SEVERITY_MAJOR_COUNT_PROPERTY = "sla-severity-major";
	public final static String OSB_SLA_SEVERITY_MINOR_COUNT_PROPERTY = "sla-severity-minor";
	public final static String OSB_SLA_SEVERITY_NORMAL_COUNT_PROPERTY = "sla-severity-normal";
	public final static String OSB_SLA_SEVERITY_FATAL_COUNT_PROPERTY = "sla-severity-fatal";
	public final static String OSB_SLA_SEVERITY_CRITICAL_COUNT_PROPERTY = "sla-severity-critical";
	public final static String OSB_SLA_SEVERITY_ALL_COUNT_PROPERTY = "sla-severity-all";
	public final static String OSB_URI_OFFLINE_COUNT_PROPERTY = "uri-offline-count";
	public final static String OSB_HIT_COUNT_PROPERTY = "hit-count";
	public final static String OSB_VALIDATION_ERRORS_COUNT_PROPERTY = "validation-errors";
	public final static String OSB_PIPELINE_SEVERITY_WARNING_COUNT_PROPERTY = "pipeline-severity-warning";
	public final static String OSB_PIPELINE_SEVERITY_MAJOR_COUNT_PROPERTY = "pipeline-severity-major";
	public final static String OSB_PIPELINE_SEVERITY_MINOR_COUNT_PROPERTY = "pipeline-severity-minor";
	public final static String OSB_PIPELINE_SEVERITY_NORMAL_COUNT_PROPERTY = "pipeline-severity-normal";
	public final static String OSB_PIPELINE_SEVERITY_FATAL_COUNT_PROPERTY = "pipeline-severity-fatal";
	public final static String OSB_PIPELINE_SEVERITY_CRITICAL_COUNT_PROPERTY = "pipeline-severity-critical";
	public final static String OSB_PIPELINE_SEVERITY_ALL_COUNT_PROPERTY = "pipeline-severity-all";
	// -----------------------------------------------------------------
	
	// -----------------------------------------------------------------
	// StatisticType (INTERVAL)
	// ------------------------
	public final static String OSB_RESPONSE_TIME_INTERVAL_PROPERTY = "response-time";
	public final static String OSB_THROTTLING_TIME_INTERVAL_PROPERTY = "throttling-time";
	public final static String OSB_ELAPSED_TIME_INTERVAL_PROPERTY = "elapsed-time";
	// -----------------------------------------------------------------
	
	// -----------------------------------------------------------------
	// StatisticType (STATUS)
	// ----------------------
	public final static String OSB_STATUS_PROPERTY = "status";
	// -----------------------------------------------------------------
	
	// -----------------------------------------------------------------
	// Property depending of StatisticType
	// -----------------------------------
	public final static String OSB_STATISTIC_TYPE_COUNT_PROPERTY_COUNT = "_count";
	
	public final static String OSB_STATISTIC_TYPE_INTERVAL_PROPERTY_COUNT = "_count";
	public final static String OSB_STATISTIC_TYPE_INTERVAL_PROPERTY_MIN = "_min";
	public final static String OSB_STATISTIC_TYPE_INTERVAL_PROPERTY_MAX = "_max";
	public final static String OSB_STATISTIC_TYPE_INTERVAL_PROPERTY_AVERAGE = "_average";
	public final static String OSB_STATISTIC_TYPE_INTERVAL_PROPERTY_SUM = "_sum";
	
	public final static String OSB_STATISTIC_TYPE_STATUS_PROPERTY_INITIAL = "_init";
	public final static String OSB_STATISTIC_TYPE_STATUS_PROPERTY_CURRENT = "_current";
		
	// -----------------------------------------------------------------
	public final static Map<String, Set<String>> OSB_STATISTIC_TYPE_TREE = new HashMap<String, Set<String>>();
	static {
		
		Set<String> countStatistics = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		Set<String> intervalStatistics = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		Set<String> statusStatistics = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);		
		
		// -----------------------------------------------------------------
		// COUNT
		// -----
		countStatistics.add(OSB_MESSAGE_COUNT_PROPERTY);
		countStatistics.add(OSB_ERROR_COUNT_PROPERTY);
		countStatistics.add(OSB_FAILOVER_COUNT_PROPERTY);
		countStatistics.add(OSB_WSS_ERROR_COUNT_PROPERTY);
		countStatistics.add(OSB_FAILURE_RATE_COUNT_PROPERTY);
		countStatistics.add(OSB_SUCCESS_RATE_COUNT_PROPERTY);
		countStatistics.add(OSB_SEVERITY_ALL);
		countStatistics.add(OSB_SLA_SEVERITY_WARNING_COUNT_PROPERTY);
		countStatistics.add(OSB_SLA_SEVERITY_MAJOR_COUNT_PROPERTY);
		countStatistics.add(OSB_SLA_SEVERITY_MINOR_COUNT_PROPERTY);
		countStatistics.add(OSB_SLA_SEVERITY_NORMAL_COUNT_PROPERTY);
		countStatistics.add(OSB_SLA_SEVERITY_FATAL_COUNT_PROPERTY);
		countStatistics.add(OSB_SLA_SEVERITY_CRITICAL_COUNT_PROPERTY);
		countStatistics.add(OSB_SLA_SEVERITY_ALL_COUNT_PROPERTY);
		countStatistics.add(OSB_URI_OFFLINE_COUNT_PROPERTY);
		countStatistics.add(OSB_HIT_COUNT_PROPERTY);
		countStatistics.add(OSB_VALIDATION_ERRORS_COUNT_PROPERTY);
		countStatistics.add(OSB_PIPELINE_SEVERITY_WARNING_COUNT_PROPERTY);
		countStatistics.add(OSB_PIPELINE_SEVERITY_MAJOR_COUNT_PROPERTY);
		countStatistics.add(OSB_PIPELINE_SEVERITY_MINOR_COUNT_PROPERTY);
		countStatistics.add(OSB_PIPELINE_SEVERITY_NORMAL_COUNT_PROPERTY);
		countStatistics.add(OSB_PIPELINE_SEVERITY_FATAL_COUNT_PROPERTY);
		countStatistics.add(OSB_PIPELINE_SEVERITY_CRITICAL_COUNT_PROPERTY);
		countStatistics.add(OSB_PIPELINE_SEVERITY_ALL_COUNT_PROPERTY);
		// -----------------------------------------------------------------
		
		// -----------------------------------------------------------------
		// INTERVAL
		// --------
		intervalStatistics.add(OSB_RESPONSE_TIME_INTERVAL_PROPERTY);
		intervalStatistics.add(OSB_THROTTLING_TIME_INTERVAL_PROPERTY);
		intervalStatistics.add(OSB_ELAPSED_TIME_INTERVAL_PROPERTY);
		// -----------------------------------------------------------------
		
		// -----------------------------------------------------------------
		// STATUS
		// ------
		statusStatistics.add(OSB_STATUS_PROPERTY);
		// -----------------------------------------------------------------
		
		OSB_STATISTIC_TYPE_TREE.put(OSB_STATISTIC_TYPE_COUNT, countStatistics);
		OSB_STATISTIC_TYPE_TREE.put(OSB_STATISTIC_TYPE_INTERVAL, intervalStatistics);
		OSB_STATISTIC_TYPE_TREE.put(OSB_STATISTIC_TYPE_STATUS, statusStatistics);
	}
	// -----------------------------------------------------------------
}
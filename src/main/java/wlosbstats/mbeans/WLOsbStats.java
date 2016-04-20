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

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

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
	//private static final int PERCENT = 100;
	private static final int BYTES_PER_MEGABYTE = 1024*1024;
		
	// Members 
	private final NonCatalogLogger log;
		
	/**
	 * Main constructor
	 * 
	 * @param netInterfaceNames Comma separated list of names of the preferred network interface to try to monitor
	 */
	public WLOsbStats() {
		log = new NonCatalogLogger(WL_OSB_APP_NAME);
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
	public double getHeapMemoryInit() {
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
	    return memoryMXBean.getHeapMemoryUsage().getInit() / BYTES_PER_MEGABYTE;
	}
	
	/**
	 * 
	 */
	public double getHeapMemoryUsed() {
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
	    return memoryMXBean.getHeapMemoryUsage().getUsed() / BYTES_PER_MEGABYTE;
	}
	
	/**
	 * 
	 */
	public double getHeapMemoryCommitted() {
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
	    return memoryMXBean.getHeapMemoryUsage().getCommitted() / BYTES_PER_MEGABYTE;
	}
	
	/**
	 * 
	 */
	public double getHeapMemoryMax() {
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
	    return memoryMXBean.getHeapMemoryUsage().getMax() / BYTES_PER_MEGABYTE;
	}
}

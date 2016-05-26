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

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Responsible for registering and de-registering the WLHostMachineStats MBean
 * on the current WebLogic server.
 */
public class WLOsbStatsMBeanRegistrar {
	
	/**
	 * Registers the WLJvmStats MBean on the current server.
	 * 
	 * @throws NamingException Indicates problem looking up MBean Server
	 * @throws MalformedObjectNameException Indicates invalid MBean name
	 * @throws InstanceAlreadyExistsException Indicates duplicate MBean already exists
	 * @throws MBeanRegistrationException Indicates general problem occurred when trying to register MBean
	 * @throws NotCompliantMBeanException Indicates that the MBean is not constructed correctly
	 */
	public void register() throws NamingException, MalformedObjectNameException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
		
		InitialContext ctx = new InitialContext();
		MBeanServer mbs = (MBeanServer) ctx.lookup(RUNTIME_MBEAN_SERVER_JNDI_KEY);
		
		WLOsbStats mbean = new WLOsbStats();
		ObjectName mbeanObjName = new ObjectName(WL_OSB_STATS_MBEAN_NAME);
				
		mbs.registerMBean(mbean, mbeanObjName);
		ctx.close();
	}

	/**
	 * De-registers the WLJvmStats MBean from the current server.
	 * 
	 * @throws NamingException Indicates problem looking up MBean Server
	 * @throws MalformedObjectNameException Indicates invalid MBean name
	 * @throws InstanceAlreadyExistsException Indicates duplicate MBean already exists
	 * @throws MBeanRegistrationException Indicates general problem occurred when trying to de-register MBean
	 */
	public void deregister() throws NamingException, MBeanRegistrationException, InstanceNotFoundException, MalformedObjectNameException {
		
		InitialContext ctx = new InitialContext(); 
		MBeanServer mbs = (MBeanServer) ctx.lookup(RUNTIME_MBEAN_SERVER_JNDI_KEY);
		ObjectName mbeanObjName = new ObjectName(WL_OSB_STATS_MBEAN_NAME);
		
		if ((mbeanObjName != null) && (mbs.isRegistered(mbeanObjName))) { 
			mbs.unregisterMBean(mbeanObjName);
		}
		ctx.close();
	}

	// Constants
	private final static String RUNTIME_MBEAN_SERVER_JNDI_KEY = "java:comp/env/jmx/runtime";
	private final static String WL_OSB_STATS_MBEAN_NAME = "wlosbstats:name=WLOsbStats";
}

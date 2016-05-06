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
package wlosbstats.lifecycle;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import wlosbstats.mbeans.WLOsbStatsMBeanRegistrar;

/**
 * Main web-app startup-shutdown/deploy-undeploy event listener which is 
 * responsible for registering and de-registering the HostMachineStats MBean
 * on the current WebLogic server.
 */
public class AppStartupShutdownListener implements ServletContextListener {
	
	/**
	 * Registers the WlJvmStats MBean on the current server.
	 * 
	 * @param event The servlet context event
	 */
	public void contextInitialized(ServletContextEvent event) {
        try {
        	wlOsbStatsMBeanRegistrar.register();
		} catch (Exception e) {
			throw new RuntimeException("Unable to initialise application. Cause: " + e, e);
		} 
	}

	/**
	 * De-registers the WlJvmStats MBean from the current server/
	 * 
	 * @param event The servlet context event
	 */
	public void contextDestroyed(ServletContextEvent event) {
		try {
			wlOsbStatsMBeanRegistrar.deregister();
		} catch (Exception e) {
			throw new RuntimeException("Unable to destroy application. Cause: " + e, e);
		} 
	}

	// Members
	private WLOsbStatsMBeanRegistrar wlOsbStatsMBeanRegistrar = new WLOsbStatsMBeanRegistrar();
}

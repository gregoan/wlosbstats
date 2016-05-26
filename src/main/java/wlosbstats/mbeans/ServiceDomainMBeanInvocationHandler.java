package wlosbstats.mbeans;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import weblogic.management.jmx.MBeanServerInvocationHandler;
import wlosbstats.util.AppLog;

import com.bea.wli.monitoring.ServiceDomainMBean;

/**
 * Invocation handler class for ServiceDomainMBean class.
 */
public class ServiceDomainMBeanInvocationHandler implements InvocationHandler {

	private Object actualMBean = null;
	private MBeanServerConnection conn;

	/**
	 * 
	 * @param conn
	 */
	public ServiceDomainMBeanInvocationHandler(MBeanServerConnection conn) {
		this.conn = conn;				
	}

	/**
	 * Invokes specified method with specified params on specified object.
	 * 
	 * @param proxy
	 * @param method
	 * @param args
	 * @return
	 * @throws Throwable
	 */
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			if (actualMBean == null) {
				actualMBean = findServiceDomain(conn);
			}
			
			if(actualMBean == null) {
				String errorMessage = "ServiceDomainMBeanInvocationHandler::invoke() - actualMBean is null - Something weird happened ...";
				AppLog.getLogger().error(errorMessage);
				throw new Exception(errorMessage);
			}
			
			Object returnValue = null;
			//Object returnValue = method.invoke(actualMBean, args);
			try {
				returnValue = method.invoke(actualMBean, args);
			} catch (Exception ex) {}
			
			return returnValue;
		} catch (Exception ex) {
			
			AppLog.getLogger().error("Error during execution of invoke method - The message is [" + ex.getMessage() + "]");
			throw ex;
		}
	}

	/**
	 * Finds the specified MBean object
	 *
	 * @param connection
	 *            - A connection to the MBeanServer.
	 * @param mbeanName
	 *            - The name of the MBean instance.
	 * @param mbeanType
	 *            - The type of the MBean.
	 * @param parent
	 *            - The name of the parent Service. Can be NULL.
	 * @return Object - The MBean or null if the MBean was not found.
	 */
	public Object findServiceDomain(MBeanServerConnection connection) {
		ServiceDomainMBean serviceDomainbean = null;
		try {
			ObjectName on = new ObjectName(ServiceDomainMBean.OBJECT_NAME);			
			serviceDomainbean = (ServiceDomainMBean) MBeanServerInvocationHandler.newProxyInstance(connection, on);
		} catch (MalformedObjectNameException ex) {
			AppLog.getLogger().error("Problem during execution of findServiceDomain() method");
			//ex.printStackTrace();
			return null;
		}
		return serviceDomainbean;
	}
}
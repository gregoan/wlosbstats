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
package domainhealth.backend.retriever;

import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import wlosbstats.mbeans.WLOsbStats;

import commonj.work.WorkItem;
import commonj.work.WorkManager;

import domainhealth.core.env.AppLog;
import domainhealth.core.env.ContextAwareWork;

public class RetrieverBackgroundService {
	
	//private final static String CAPTURE_THREADS_WORK_MGR_JNDI = "java:comp/env/DomainHealth_IndividualServerStatCapturerWorkMngr";
	private final static String CAPTURE_THREADS_WORK_MGR_JNDI = "java:comp/env/DomainHealth_OSB_IndividualServerStatCapturerWorkMngr";
	
	private final static int ONE_SECOND_MILLIS = 1000;
	private final static float MIN_POLL_FACTOR = 0.1F;
	private final static float MAX_POLL_FACTOR = 0.9F;
	
	private final WorkManager captureThreadsWkMgr;
	//private final int minPollIntervalMillis;
	private final int maxPollIntervalMillis;
	private final int queryIntervalMillis;
	
	/**
	 * Create new service with the root path to write CSV file to
	 *  
	 * @param appProps The system/application key/value pairs
	 */
	public RetrieverBackgroundService() {
		
		int queryIntervalSecs = 30;
		
		queryIntervalMillis = queryIntervalSecs * ONE_SECOND_MILLIS;
		//minPollIntervalMillis = (int) (MIN_POLL_FACTOR * queryIntervalMillis);
		maxPollIntervalMillis = (int) (MAX_POLL_FACTOR * queryIntervalMillis);
		
		WorkManager localCaptureThreadsWkMgr = null;
		try {
			localCaptureThreadsWkMgr = getWorkManager(CAPTURE_THREADS_WORK_MGR_JNDI);
		} catch (NamingException nEx) {
			throw new IllegalStateException(getClass() + " cannot be instantiateed because Work Manager [" + CAPTURE_THREADS_WORK_MGR_JNDI + "] cannot be located. " + nEx.getMessage());
		}
		
		this.captureThreadsWkMgr = localCaptureThreadsWkMgr;
		
	}
	
	/**
	 * Start the continuously repeating sleep-gather-schedule background 
	 * daemon thread.
	 */
	public void startup() {
		try {
			Thread backgroundThread = new Thread(new CaptureRunnable(), this.getClass().getName());
			backgroundThread.setDaemon(true);
			backgroundThread.start();
			AppLog.getLogger().debug("Created background Java daemon thread to drive OSB extension for DomainHealt application");
		} catch (Exception e) {
			AppLog.getLogger().critical("Retriever Background Service has been disabled. Reason: " + e.toString());
			throw new RuntimeException(e);
		}
	}

	/**
	 * Send signal to the continuously repeating sleep-gather-schedule 
	 * background process should terminate as soon as possible.
	 */
	public void shutdown() {
		AppLog.getLogger().info("Retriever Background Service shutting down");
	}

	/**
	 * Runnable which will be spawned as a background daemon thread 
	 * responsible for initiating the capture process and
	 * then repeating the cycle over and over again.
	 */
	private class CaptureRunnable implements Runnable {
		public void run() {
			
			runNormalProcessing();
			
			/*
			// Trying 10 times to initialize the application (-> register the MBean)
			for (int index = 0; index < 10; index ++ ) {
				try {					
					// Sleep 10 seconds to wait WL server's startup
					Thread.sleep(10000);
					
					AppLog.getLogger().notice("Trying to register the OSB extension MBean");
					new WLOsbStatsMBeanRegistrar().register();
					
				} catch (Exception ex) {
					
AppLog.getLogger().notice("Registring of MBean failed due to [" + ex.getMessage() +"]");
					
					try {
						AppLog.getLogger().notice("About to sleep and then perform another processing run");
						
						// Sleep 10 seconds ...
						Thread.sleep(10000);
					} catch(Exception inEx) {
					
						AppLog.getLogger().warning("Retriever Background Service has failed anormally for this run. Reason: " + inEx.getMessage());
						break;
					}
				}
			}
			*/
		}
	}
	
	/**
	 * Retrieves a named work manager from the local JNDI tree. 
	 * 
	 * @param wkMgrName The name of the work manager to retrieve
	 * @return The found work manager
	 * @throws NamingException Indicates that the work manager could not be located
	 */
	private WorkManager getWorkManager(String wkMgrName) throws NamingException {
		InitialContext ctx = null;
		
		try {
		    ctx = new InitialContext();
		    return (WorkManager) ctx.lookup(wkMgrName);
		} finally {
		    try { ctx.close(); } catch (Exception e) {}
		}
	}
	
	/**
	 * Runs the normal statistic capture process, by iterating through the 
	 * list of currently running servers in the domain, and for each of 
	 * these, schedule the query of the servers stats in a separate work item
	 * to run in parallel in the WebLogic thread pool.
	 */
	private void runNormalProcessing() {
				
		try {
			AppLog.getLogger().debug("Statistics Retriever Background Service running another iteration to capture and log stats");
			List<WorkItem> pollerWorkItemList = new ArrayList<WorkItem>();
			
			pollerWorkItemList.add(captureThreadsWkMgr.schedule(new ContextAwareWork() {
				public void doRun() {
					
					// Trying 10 times to initialize the application (-> register the MBean)
					for (int index = 0; index < 10; index ++ ) {
						try {					
							// Sleep 10 seconds to wait WL server's startup
							Thread.sleep(10000);
							
							AppLog.getLogger().notice("");
							AppLog.getLogger().notice("----------------------------------------------");
							AppLog.getLogger().notice("Trying to register the OSB extension MBean ...");
							AppLog.getLogger().notice("----------------------------------------------");
							AppLog.getLogger().notice("");
							
							//new WLOsbStatsMBeanRegistrar().register();							
							WLOsbStats mbean = new WLOsbStats();
							
							AppLog.getLogger().notice("");
							AppLog.getLogger().notice("---------------------------------------------------");
							AppLog.getLogger().notice("IF THIS MESSAGE IS VISIBLE -> IT'S WORKING FINE ...");
							AppLog.getLogger().notice("---------------------------------------------------");
							AppLog.getLogger().notice("");
							
						} catch (Exception ex) {
							
							AppLog.getLogger().notice("Registring of MBean failed due to [" + ex.getMessage() +"]");
							try {
								AppLog.getLogger().notice("About to sleep and then perform another processing run");
								
								// Sleep 10 seconds ...
								//Thread.sleep(10000);
								Thread.sleep(5000);
							} catch(Exception inEx) {
							
								AppLog.getLogger().warning("Retriever Background Service has failed anormally for this run. Reason: " + inEx.getMessage());
								break;
							}
						}
					}
				}					
			}));	
			
			//boolean allCompletedSuccessfully = captureThreadsWkMgr.waitForAll(pollerWorkItemList, maxPollIntervalMillis);
			
			AppLog.getLogger().info("Statistics Retriever Background Service completing another iteration successfully");
		} catch (Exception e) {
			AppLog.getLogger().error(e.toString());
			e.printStackTrace();
			AppLog.getLogger().error("Retriever Background Service - unable to retrieve ... for this iteration");
		}
		
		/*
		DomainRuntimeServiceMBeanConnection conn = null;
		
		try {
			AppLog.getLogger().debug("Statistics Retriever Background Service running another iteration to capture and log stats");
			conn = new DomainRuntimeServiceMBeanConnection();
			ObjectName[] serverRuntimes = conn.getAllServerRuntimes();			
			int length = serverRuntimes.length;
			List<WorkItem> pollerWorkItemList = new ArrayList<WorkItem>();
			
			for (int i = 0; i < length; i++) {

				pollerWorkItemList.add(captureThreadsWkMgr.schedule(new ContextAwareWork() {
					public void doRun() {
						try {
							// ...
						} catch (Exception e) {
							AppLog.getLogger().error("Retriever Background Service - unable to retrieve statistics for specific server [" + serverName + "] for this iteration");
						}
					}					
				}));				
			}			
			
			boolean allCompletedSuccessfully = captureThreadsWkMgr.waitForAll(pollerWorkItemList, maxPollIntervalMillis);
			AppLog.getLogger().info("Statistics Retriever Background Service completing another iteration successfully");
		} catch (Exception e) {
			AppLog.getLogger().error(e.toString());
			e.printStackTrace();
			AppLog.getLogger().error("Retriever Background Service - unable to retrieve ... for this iteration");
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
		*/
	}
}
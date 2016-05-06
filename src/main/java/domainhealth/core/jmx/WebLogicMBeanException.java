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
package domainhealth.core.jmx;

/**
 * Encapsulates a problem when connecting to a WebLogic JMX MBean server
 */
public class WebLogicMBeanException extends Exception {
	/**
	 * Create an WebLogic JMX exception
	 * 
	 * @param message The exception message
	 * @param cause The root cause
	 */
	public WebLogicMBeanException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * Create an WebLogic JMX exception
	 * 
	 * @param message The exception message
	 */
	public WebLogicMBeanException(String message) {
		super(message);
	}

	/**
	 * Create an WebLogic JMX exception
	 * 
	 * @param cause The root cause
	 */
	public WebLogicMBeanException(Throwable cause) {
		super(cause);
	}
	
	// Constants
	private static final long serialVersionUID = 1L;
}

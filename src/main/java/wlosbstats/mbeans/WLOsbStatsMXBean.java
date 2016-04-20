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

/**
 * MBean exposing statistics for the JVM running this WebLogic
 * Server instances. Provides read-only attributes for useful JVM usages statistics.
 *  
 * @see javax.management.MXBean
 */
public interface WLOsbStatsMXBean {
	
	public double getHeapMemoryInit();
	public double getHeapMemoryUsed();
	public double getHeapMemoryCommitted();
	public double getHeapMemoryMax();
}
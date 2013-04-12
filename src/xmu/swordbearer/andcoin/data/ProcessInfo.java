package xmu.swordbearer.andcoin.data;

public class ProcessInfo {
	private String processName;
	private int pid;
	private int memSize;// 占用内存大小
	private int netSize;// 网络流量

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getMemSize() {
		return memSize;
	}

	public void setMemSize(int memSize) {
		this.memSize = memSize;
	}

	public int getNetSize() {
		return netSize;
	}

	public void setNetSize(int netSize) {
		this.netSize = netSize;
	}

}

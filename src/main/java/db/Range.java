package db;

public class Range {
	private String ec2Id;
	private String startRange;
	private String endRange;
	private String status;
	
	public String getEc2Id() {
		return ec2Id;
	}
	public void setEc2Id(String ec2Id) {
		this.ec2Id = ec2Id;
	}
	public String getStartRange() {
		return startRange;
	}
	public void setStartRange(String startRange) {
		this.startRange = startRange;
	}
	public String getEndRange() {
		return endRange;
	}
	public void setEndRange(String endRange) {
		this.endRange = endRange;
	}
	 
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
}

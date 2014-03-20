package com.OOBDeviceTest.helper;

import java.io.IOException;
import java.io.Serializable;

public class TestCase implements Serializable {
	public static enum RESULT {
		OK, FAIL, SKIP, UNDEF, RETEST,
	}

	private int testNo;
	private boolean needtest;
	private String testName;
	private String className;
	private RESULT result = RESULT.UNDEF;
	private String detail;
	private boolean showResult;
	public String startTime = null;
	public String finishTime = null;
	
	public boolean isShowResult() {
		return showResult;
	}

	public void setShowResult(boolean showResult) {
		this.showResult = showResult;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public TestCase(int testNo, String testName, String className) {
		this.testNo = testNo;
		this.testName = testName;
		this.className = className;
		this.needtest = true;
		showResult = true;
	}
	public boolean getneedtest(){
		return this.needtest;
	}
	public void setneedtest(boolean tmp){
		this.needtest = tmp;
	}
	public String getClassName() {
		return className;
	}

	public RESULT getResult() {
		return result;
	}

	public String getTestName() {
		return testName;
	}

	public int getTestNo() {
		return testNo;
	}

	public void setResult(RESULT result) {
		this.result = result;
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		out.writeObject(className);
		out.writeInt(result.ordinal());
		out.writeObject(detail);
		out.writeBoolean(showResult);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		className = in.readObject().toString();
		result = RESULT.values()[in.readInt()];
		detail = in.readObject().toString();
		showResult = in.readBoolean();
	}
}

package com.OOBDeviceTest.helper;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.res.XmlResourceParser;
import android.os.SystemClock;
import android.util.Log;
import android.util.Xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import com.OOBDeviceTest.DeviceTest;

public class XmlDeal {

	private static final String TAG = "XmlDeal";
	/**/
	private static final String XML_ROOT_TAG = "TestCaseList";
	private static final String XML_NODE_TAG = "TestCase";

	private static final String CLASS_NAME_TAG = "class_name";
	private static final String TEST_NAME_TAG = "test_name";
	private static final String RESULT_TAG = "result";
	private static final String TEST_GROUP_TAG = "test_group";
	private static final String TEST_FIRST = "first_test";

	public List<TestCase> mTestCases = null;
	public Map<String, List<TestCase>> mCaseGroups = null;

	public XmlDeal(InputStream is) {
		mTestCases = new ArrayList<TestCase>();
		mCaseGroups = new HashMap<String, List<TestCase>>();
		if (!ParseXml(is)) {
			throw new RuntimeException();
		}
	}

	private boolean ParseXml(InputStream is) {

		DocumentBuilderFactory docBuilderFactory = null;
		DocumentBuilder docBuilder = null;
		Document doc = null;
		try {
			docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilder = docBuilderFactory.newDocumentBuilder();

			doc = docBuilder.parse(is);
			NodeList nodeList = doc.getElementsByTagName(XML_ROOT_TAG);

			int length = nodeList.getLength();
			List<TestCase> caseGroup = null;
			for (int i = 0; i < length; i++) {
				Node item = nodeList.item(i);

				int testNo = 0;
				caseGroup = null;
				for (Node node = item.getFirstChild(); node != null; node = node
						.getNextSibling()) {
					if (node.getNodeType() == Node.ELEMENT_NODE) {

						String testName = null;
						String className = null;
						boolean isfirsttest = false;
						for (int j = 0; j < node.getAttributes().getLength(); j++) {
							String attrValue = node.getAttributes().item(j)
									.getNodeValue();
							String attrName = node.getAttributes().item(j)
									.getNodeName();
							if (attrName.equals(CLASS_NAME_TAG)) {
								className = attrValue;
							} else if (attrName.equals(TEST_GROUP_TAG)) {
								caseGroup = mCaseGroups.get(attrValue);
								if (caseGroup == null) {
									caseGroup = new ArrayList<TestCase>();
									mCaseGroups.put(attrValue, caseGroup);
								}
							}else if(attrName.equals(TEST_FIRST)){
								isfirsttest = true;
							}
						}
						testName = node.getFirstChild().getNodeValue();
						Log.i(TAG, "-----getTestItemName:" + testName + "    isfirsttest = " + isfirsttest);
						TestCase testCase = new TestCase(testNo, testName,
								className);
						testCase.setneedtest(isfirsttest);
						mTestCases.add(testCase);
						if(caseGroup != null) {
							caseGroup.add(testCase);
						}
						testNo++;
					}
				}
			}

		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return false;
		} finally {
			doc = null;
			docBuilder = null;
			docBuilderFactory = null;
		}

		if (mTestCases.size() == 0) {
			return false;
		}

		Log.i(TAG, "The cases count is :" + mTestCases.size());
		return true;
	}

}

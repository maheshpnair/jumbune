package org.jumbune.common.utils.locators;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jumbune.common.job.JobConfig;
import org.jumbune.common.utils.RemotingUtil;

public class CDHLocator extends AbstractDistributionLocator {

	private static final String error_message = "Failed to detect Hadoop! Did you miss setting some environment variables?";
	
	private static final String LS_CDH_POSTFIX_PART = " -Rl | grep 'conf ->'";
	
	private static final String expectedConf = "/etc/hadoop";
	
	public static final Logger LOGGER = LogManager
			.getLogger(CDHLocator.class);

	@Override
	public String getHadoopConfDirPath(JobConfig jobConfig) {

		String absoluteDirPath = null;
		absoluteDirPath = getAbsoluteConfDirPath(expectedConf, jobConfig);
		if (absoluteDirPath == null || absoluteDirPath.isEmpty()) {
			throw new IllegalArgumentException(
					"Failed to get configuration directory. Expected to get a linked configuration from "
							+ expectedConf);
		}
		LOGGER.debug("Final linked Hadoop conf path:" + absoluteDirPath);
		absoluteDirPath = absoluteDirPath.trim();
		checkEmptyDir(absoluteDirPath);
		return absoluteDirPath;
	}

	private boolean checkEmptyDir(String possibleDirList) {
		if (possibleDirList == null || possibleDirList.isEmpty()) {
			return true;
		}
		return false;
	}

	private String getAbsoluteConfDirPath(String dir, JobConfig jobConfig) {
		String result = null, response = null;
		if (dir == null || dir.trim().isEmpty() || !dir.contains("/")) {
			throw new IllegalArgumentException(error_message);
		}
		response = RemotingUtil.executeCommand(jobConfig, LS_PREFIX_PART + dir
				+ LS_CDH_POSTFIX_PART);
		if (response != null && !response.isEmpty() && response.indexOf(">")!=-1) {
	    result = response.substring((response.indexOf(">") + 1),
					response.length());
		result = result.endsWith(File.separator)?result:result.trim()+File.separator;
		}
		LOGGER.debug("Found linked Hadoop conf path:"+result);
		if(result!=null){
			String recursiveResponse = getAbsoluteConfDirPath(result, jobConfig);
			if(recursiveResponse!=null){
				result = recursiveResponse;
			}
		}
		return result;
	}

}

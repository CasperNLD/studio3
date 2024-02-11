/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Platform;
import org.junit.Test;

import com.aptana.core.util.PlatformUtil.ProcessItem;

/**
 * @author Max Stepanov
 */
@SuppressWarnings("nls")
public class PlatformUtilTest
{

	private static String COMSPEC = System.getenv("ComSpec");
	@Test
	public void testGetRunningProcesses()
	{
		ProcessItem[] processes = PlatformUtil.getRunningProcesses();
		assertTrue(processes.length > 0);
		
		String myname = ProcessHandle.current().info().command().get();
		long pid = ProcessHandle.current().pid();
		assertTrue(Stream.of(processes).anyMatch(p -> p.getPid() == pid && p.getExecutableName().contentEquals(myname)));
	}

	@Test
	public void testGetRunningChildProcesses() throws IOException
	{
		String cmd = Platform.OS_WIN32.equals(Platform.getOS()) ? COMSPEC : (Platform.OS_MACOSX.equals(Platform
				.getOS()) ? "sleep 5s" : "sh");
		String[] command = Platform.OS_WIN32.equals(Platform.getOS()) ? new String[] { COMSPEC, "/C", "ping -n 10 127.0.0.1" }
				: new String[] { "sh", "-c", "sleep 5s" };
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.redirectError(Redirect.INHERIT);
		processBuilder.start();
		ProcessItem[] processes = PlatformUtil.getRunningChildProcesses();
		assertTrue(processes.length > 0);
		
		boolean passed = Stream.of(processes).anyMatch(i ->
		{
			String lowerCaseProcessName = i.getExecutableName().toLowerCase();
			if (lowerCaseProcessName.equalsIgnoreCase(cmd))
			{
				return true;
			}
			// Sometimes linux shows "[sh]" as process name
			if ("sh".equals(cmd))
			{
				if (lowerCaseProcessName.endsWith("[sh]"))
				{
					return true;
				}
			}
			return false;
		});
		assertTrue("Expected child process \"" + cmd + "\" not found in " + Arrays.asList(processes).toString(), passed);
	}

	@Test
	public void testKillProcesses() throws IOException
	{
		String cmd = Platform.OS_WIN32.equals(Platform.getOS()) ? COMSPEC : (Platform.OS_MACOSX.equals(Platform
				.getOS()) ? "sleep 5s" : "sh");
		String[] command = Platform.OS_WIN32.equals(Platform.getOS()) ? new String[] { COMSPEC, "/C", "ping -n 10 127.0.0.1" }
				: new String[] { "sh", "-c", "sleep 5s" };
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.redirectError(Redirect.INHERIT);
		Process process = processBuilder.start();
		ProcessItem[] processes = PlatformUtil.getRunningChildProcesses();
		assertTrue(processes.length > 0);

		PlatformUtil.killProcess((int) process.pid());
		processes = PlatformUtil.getRunningChildProcesses();
		assertTrue("Expected process did not terminate on kill command",Stream.of(processes).noneMatch(i ->
			i.getPid() == process.pid()));
	}

	@Test
	public void testExpandEnvironmentStrings()
	{
		assertEquals("abc/d", PlatformUtil.expandEnvironmentStrings("abc/d"));
		expandAndCompareEnvironmentStrings("~", "/test");
		expandAndCompareEnvironmentStrings(PlatformUtil.HOME_DIRECTORY, "/test");
		if (Platform.OS_WIN32.equals(Platform.getOS()))
		{
			expandAndCompareEnvironmentStrings(PlatformUtil.DESKTOP_DIRECTORY, "/test");
			expandAndCompareEnvironmentStrings(PlatformUtil.DESKTOP_DIRECTORY, "/test");
			expandAndCompareEnvironmentStrings(PlatformUtil.COMMON_APPDATA, "/test");
			expandAndCompareEnvironmentStrings(PlatformUtil.LOCAL_APPDATA, "/test");
		}
		else if (Platform.OS_MACOSX.equals(Platform.getOS()))
		{
			expandAndCompareEnvironmentStrings(PlatformUtil.DESKTOP_DIRECTORY, "/test");
			expandAndCompareEnvironmentStrings(PlatformUtil.DOCUMENTS_DIRECTORY, "/test");
		}
	}

	private void expandAndCompareEnvironmentStrings(String token, String path)
	{
		String expanded = PlatformUtil.expandEnvironmentStrings(token + path);
		assertNotSame(token + path, expanded);
		assertTrue(expanded.endsWith(path));
	}

}

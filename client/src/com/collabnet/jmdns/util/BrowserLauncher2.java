/*
 * CollabNet Subversion Edge
 * Copyright (C) 2010, CollabNet Inc. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.collabnet.jmdns.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;


/**
 *  Provides a static method to start a browser.
 *  Relies on the original BrowserLauncher implementation,
 *  but provides additional support for JNLP.
 */
public class BrowserLauncher2
{
	
	static final String[] browsers = { 
		"google-chrome"
		, "firefox"
		, "opera"
		, "konqueror"
		, "epiphany"
		, "seamonkey"
		, "galeon"
		, "kazehakase"
		, "mozilla" 
		};
	   static final String errMsg = "Error attempting to launch web browser";

	   /**
	    * Opens the specified web page in the user's default browser
	    * @param url A web address (URL) of a web page (ex: "http://www.google.com/")
	    * @throws IOException 
	    */
	   public static void openURL2(String url) throws IOException {
		   try { //attempt to use Desktop library from JDK 1.6+ (even if on 1.5) 
			   // mimic via reflection: 
			   // java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));			
			   Class<?> desktop = Class.forName("java.awt.Desktop"); 
			   Method getDesktop = desktop.getDeclaredMethod("getDesktop");
			   Object desktopObj = getDesktop.invoke(null);
			   Method browse = desktop.getDeclaredMethod("browse", new Class[] {java.net.URI.class});
			   browse.invoke(desktopObj, new Object[] {java.net.URI.create(url)});
			   return;
		   }
		   catch (Exception ignore) { //library not available or failed
			   String osName = System.getProperty("os.name");
			   try {
				   if (osName.startsWith("Mac OS")) {
					   Class.forName("com.apple.eio.FileManager").getDeclaredMethod(
							   "openURL", new Class[] {String.class}).invoke(null,
									   new Object[] {url});
				   }
				   else if (osName.startsWith("Windows"))
					   Runtime.getRuntime().exec(
							   "rundll32 url.dll,FileProtocolHandler " + url);
				   else { //assume Unix or Linux
					   boolean found = false;
					   for (String browser : browsers)
						   if (!found) {
							   found = Runtime.getRuntime().exec(
									   new String[] {"which", browser}).waitFor() == 0;
							   if (found)
								   Runtime.getRuntime().exec(new String[] {browser, url});
						   }
					   if (!found)
						   throw new Exception(Arrays.toString(browsers));
				   }
			   }
			   catch (Exception e) {
				   openURL(url);
			   }
		   }
	   }
		   
	/**
	 *  Open an url in a browser.
	 */
	private static void openURL(String url) throws IOException
	{
		try
		{
			BrowserLauncher.openURL(url);
		}

		// In case of security exception try applet or JNLP.
		catch(SecurityException e)
		{
			try
			{
				// Use reflection to avoid compile-time dependency to JNLP.

				// BasicService	basicservice	= (BasicService)ServiceManager.lookup("javax.jnlp.BasicService"); 
				Class<?> servicemanagerclass = Class.forName("javax.jnlp.ServiceManager");
				Method lookup = servicemanagerclass.getMethod("lookup", new Class[]{String.class});
				Object basicservice = lookup.invoke(null, new Object[]{"javax.jnlp.BasicService"});

				// basicservice.showDocument(new URL(url));
				Class<?> basicserviceclass = Class.forName("javax.jnlp.BasicService");
				Method showdocument = basicserviceclass.getMethod("showDocument", new Class[]{URL.class});
				showdocument.invoke(basicservice, new Object[]{new URL(url)});
			}
			catch(InvocationTargetException e2)
			{
				if(e2.getTargetException() instanceof IOException)
				{
					throw (IOException)e2.getTargetException();
				}
				else
				{
					StringWriter	sw	= new StringWriter();
					e2.printStackTrace(new PrintWriter(sw));
					throw new RuntimeException(sw.toString());
				}
			}
			catch(Exception e2)
			{
				StringWriter	sw	= new StringWriter();
				e2.printStackTrace(new PrintWriter(sw));
				throw new IOException(sw.toString());
			}
		}
	}
}
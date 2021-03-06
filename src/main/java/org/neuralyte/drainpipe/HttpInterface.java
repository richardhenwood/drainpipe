/*
    Project Drainpipe: A RESTful sound tracker
    Copyright (C) 2012  Richard Henwood rjhenwod@yahoo.co.uk

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neuralyte.drainpipe;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.sound.sampled.LineUnavailableException;

//import com.sun.grizzly.http.SelectorThread;
//import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import com.sun.grizzly.http.servlet.ServletAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
//import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
//import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class HttpInterface {

	public static void main( String[] args ) throws LineUnavailableException, IOException {

		final String baseUri = "http://localhost:<port>/";
		//final Map<String, String> initParams = new HashMap<String, String>();
		
		int port = 9998;
		String staticRoot = null;
		String outfile = "";
		if (args.length > 1) {
			staticRoot = args[0];
			port = Integer.parseInt(args[1]);
		}
		else {
			System.out.println("give me a path to the UI and a port number or fuck off.");
			System.exit(1);
		}

		GrizzlyWebServer gws = new GrizzlyWebServer(port);

		System.out.println("Starting grizzly...");
		System.out.println(String.format("Jersey app started with WADL available at %sapplication.wadl\nPoint your firefox browser at: %sdrainpipe\nHit anykey to stop it...", baseUri, baseUri));
		System.out.println("port = " + port);
		System.out.println("");
		System.out.println("        ,---------.   "); 
		System.out.println("       /           \\   "); 
		System.out.println("      (  Neuralyte  )   "); 
		System.out.println("   ,---+  Tracker  / ___   "); 
		System.out.println("  /                +'   '.   "); 
		System.out.println(" {                        \\   "); 
		System.out.println(" {........................ }   "); 
		System.out.println("  `xxxxxxxxxxxxxxxxxxxxxxx'   "); 
		System.out.println("    \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\   "); 
		System.out.println("     \\\\\\\\\\Cloud Utility\\\\\\\\\\   "); 
		System.out.println("      \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\   ");
		System.out.println(" To run as Cloud Utility, now that the program is running");
		System.out.println(" you need to route audio out to a pulse audio pipe: ");
		System.out.println(" NOTE: You probably have to press play first.");
		System.out.println(" # parec -r | oggenc -r -o - - | ./sighttpd -f ./oggout.conf");
		System.out.println("");
		System.out.println(String.format("Browse: http://localhost:%s/drainpipe", port));
		System.out.println("");
		
		ServletAdapter sa = new ServletAdapter();
	
		sa.addInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
		sa.addInitParameter("com.sun.jersey.config.property.packages", "org.neuralyte.drainpipe.resources");
		sa.addInitParameter("org.neuralyte.drainpipe.staticRoot", staticRoot);
		sa.addInitParameter("org.neuralyte.drainpipe.outfile", outfile);

		sa.setServletInstance(new ServletContainer());
		
		gws.addGrizzlyAdapter(sa, new String [] {"/drainpipe"});

		try {
			gws.start();
		}
		catch (Exception e) {
			System.out.println("Fuckit: " + e);
		}
		
		//comment out the next two lines so listing it is easy to background.
		// i.e. codes doen't wait fror stdin input.//
		//Scanner sc = new Scanner(System.in);
		//sc.nextLine();
		//threadSelector.stopEndpoint();
		gws.start();
		//gws.stop();
		//gws.addGrizzlyAdapter((GrizzlyAdapter) threadSelector.getAdapter());
		//System.exit(0);
	}
}

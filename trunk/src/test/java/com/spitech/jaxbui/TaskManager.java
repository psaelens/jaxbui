package com.spitech.jaxbui;

import java.io.*;
import java.util.*;

public class TaskManager {
	
	public static List listRunningProcesses() {
		List<String> processes = new ArrayList<String>();
		try {
			String line;
//			/svc /nh
			System.out.println(Runtime.getRuntime().totalMemory());
			Process p = Runtime.getRuntime().exec("tasklist.exe /nh /FI MEMUSAGE");
			BufferedReader input = new BufferedReader(new InputStreamReader(p
					.getInputStream()));
			while ((line = input.readLine()) != null) {
//				if (!line.trim().equals("")) {
//					// keep only the process name
//					processes.add(line.substring(0, line.indexOf(" ")));
//				}
				System.out.println(line);
			}
			input.close();
		} catch (Exception err) {
			err.printStackTrace();
		}
		return processes;
	}

	public static void main(String[] args) {
		List<String> processes = listRunningProcesses();
		String result = "";

		// display the result
//		Iterator<String> it = processes.iterator();
////		int i = 0;
//		while (it.hasNext()) {
//			result += it.next() + ",";
////			i++;
////			if (i == 10) {
//				result += "\n";
////				i = 0;
////			}
//		}
//		System.out.println("Running processes :\n " + result + "\n Total No of Processes: "
//				+ processes.size());

	}
}

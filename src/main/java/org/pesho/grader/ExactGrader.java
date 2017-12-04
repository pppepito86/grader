package org.pesho.grader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;

public class ExactGrader {

	public static void main(String[] args) {
		try (BufferedReader out = new BufferedReader(new FileReader(new File(args[1])));
				BufferedReader sol = new BufferedReader(new FileReader(new File(args[2])));) {

			Iterator<String> it1 = out.lines().iterator();
			Iterator<String> it2 = sol.lines().iterator();

			while (it1.hasNext() && it2.hasNext()) {
				if (!it1.next().equals(it2.next())) {
					System.out.println(0);
					System.exit(0);
				}
			}

			if (!it1.hasNext() && !it2.hasNext()) {
				System.out.println(1);
			} else {
				System.out.println(0);
			}
		} catch (Exception e) {
			System.out.println(0);
			e.printStackTrace();
			System.exit(1);
		}

	}

}

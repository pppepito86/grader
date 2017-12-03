import java.io.File;

import org.apache.commons.io.FileUtils;

public class ExactGrader2 {

	public static void main(String[] args) {
		try {
			File out = new File(args[1]);
			File sol = new File(args[2]);
			boolean equal = FileUtils.contentEquals(out, sol);
			int result = equal ? 1 : 0;
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(0);
		}

	}

}

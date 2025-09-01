package org.pesho.grader.compile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;


import org.apache.commons.io.FileUtils;
import org.pesho.grader.step.Verdict;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

public class ZipLaTexCompileStep extends CompileStep {

	public static final String UNZIP_COMMAND_PATTERN = "/usr/bin/unzip %s";
	public static final String COMPILE_COMMAND_PATTERN = "/usr/bin/latexmk -pdf -pdflatex=%s -interaction=nonstopmode -jobname=%s %s";
	public static final String FAILED_COMPILE_COMMAND_PATTERN = "/usr/bin/latexmk -pdf -pdflatex=%s -interaction=nonstopmode";
	public static final String NOTEX_COMMAND_PATTERN = "/usr/bin/latexmk -pdf -interaction=nonstopmode";
	private String texMode;
	private File texFile = null;
	private boolean failed = false;
	private boolean noTex = false;

	public static final String SOURCE_FILE_ENDING = ".zip";
	public static final String BINARY_FILE_ENDING = ".pdf";

	public ZipLaTexCompileStep(File sourceFile, File graderDir, Map<String, Double> time, Map<String, Integer> memory) {
		super(sourceFile, graderDir, time, memory);
	}

	public void setTexMode (String texMode) {
		this.texMode = texMode;
	}
	
	@Override
	public String[] getCommands() {
		if (noTex) return new String[] { NOTEX_COMMAND_PATTERN };
		if (failed) {
			String failedCommand = String.format(FAILED_COMPILE_COMMAND_PATTERN, texMode);
			return new String[] { failedCommand };
		}
		if (texFile == null) {
			String unzipCommand = String.format(UNZIP_COMMAND_PATTERN, sourceFile.getName());
			return new String[] { unzipCommand };
		}
		String compileCommand = String.format(COMPILE_COMMAND_PATTERN, texMode, sourceFile.getName().replaceAll(SOURCE_FILE_ENDING + "$", ""), texFile.getName());
		return new String[] { compileCommand };
	}
	
	@Override
	public void execute() {
		super.execute();
		if (getVerdict() != Verdict.OK) {
			FileUtils.deleteQuietly(sandboxDir);
			return ;
		}
		Path currentDir = sandboxDir.toPath();
		try {
			int cnt = 0;
			for (Path tex : Files.newDirectoryStream(currentDir, "*.tex")) {
				cnt++;
				String mainTex = Files.lines(tex)
					.filter(line -> line.contains("begin{document}"))
					.findFirst()
					.orElse(null);
				if (mainTex == null) continue;
				texFile = tex.toFile();
				super.execute();
				//if (getVerdict() == Verdict.OK) {
					FileUtils.deleteQuietly(sandboxDir);
					return ;
				//}
			}
			if (cnt == 0) {
				noTex = true;
				super.execute();
				FileUtils.deleteQuietly(sandboxDir);
				return ;
			}
		} catch (IOException e) {
			FileUtils.deleteQuietly(sandboxDir);
			e.printStackTrace();
		}
		failed = true;
		super.execute();
		FileUtils.deleteQuietly(sandboxDir);
	}

	public String getBinaryFileName() {
		return sourceFile.getName().replaceAll(SOURCE_FILE_ENDING + "$", BINARY_FILE_ENDING);
	}

	@Override
	public List<String> getTrustedDirectories () {
		if (failed || texFile != null) {
			try {
				ProcessResult result = new ProcessExecutor().command("kpsewhich", "--var-value=TEXMFCACHE")
					.readOutput(true)
					.execute();
				return Arrays.stream(result.getOutput().getString().trim().split(":")).collect(Collectors.toList());
			} catch (Exception e) {
				e.printStackTrace();
				return new ArrayList<>();
			}
		}
		return new ArrayList<>();
	}
}

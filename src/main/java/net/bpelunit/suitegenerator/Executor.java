package net.bpelunit.suitegenerator;

import java.io.File;

import net.bpelunit.suitegenerator.config.Config;

public class Executor {

	private static boolean createRecommendations = true;
	private static boolean createNewTestCases = true;

	public static void main(String[] args) {
		File folder = new File("files/suite/");
		Generator g = new Generator(new File(folder, Config.get().getClassificationTableName()));
		g.generate(folder, createRecommendations, createNewTestCases);
	}

}
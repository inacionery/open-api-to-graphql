
package com.inacionery.openapitographql;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * @author Inácio Nery
 */
public class Main {

	public static void main(String[] args) throws IOException {
		String content = _readFile("rest-openapi.yaml");

		_writeFile(content, "rest-openapi.graphql");
	}

	private static String _readFile(String pathname) throws IOException {
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();

		File file = new File(classLoader.getResource(pathname).getFile());

		return new String(Files.readAllBytes(file.toPath()));
	}

	private static void _writeFile(String content, String pathname)
		throws IOException {
		File file = new File(pathname);

		Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
	}
}
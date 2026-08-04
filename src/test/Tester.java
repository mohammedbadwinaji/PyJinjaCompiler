package test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Tester {

    /**
     * 🌟 NEW METHOD: Instantly executes a test strategy on ONE specific file path.
     * Bypasses folder loops entirely while reusing your polymorphic test engine.
     *
     * @param filePath     The exact path to the file (e.g., "input/jinja/variables.html")
     * @param testStrategy The specific testing module instance (e.g., new JinjaParserTest())
     */
    public static void testFile(String filePath, CompilerTest testStrategy) {
        Path targetFile = Paths.get(filePath);

        // Fail-fast safeguard check if file path points to a ghost target location
        if (!Files.exists(targetFile) || !Files.isRegularFile(targetFile)) {
            System.err.println("❌ Error: Target file does not exist or is invalid: " + filePath);
            return;
        }

        System.out.println("=====================================================================");
        System.out.println("🎯 TARGETED SINGLE FILE TEST | Strategy: " + testStrategy.getClass().getSimpleName());
        System.out.println("📍 Path: " + filePath);
        System.out.println("=====================================================================");

        try {
            // Polymorphically execute the signature method matching your exact design contract
            testStrategy.test(filePath);
            System.out.println("✅ Single file test executed successfully.");
        } catch (Exception e) {
            System.err.println("❌ Critical failure parsing file [" + targetFile.getFileName() + "]: " + e.getMessage());
            e.printStackTrace(); // Output detail tracking matrix logs
        }
        System.out.println("=====================================================================\n");
    }

    /**
     * Dynamically scans a folder and executes the passed CompilerTest strategy on matching files.
     */
    public static void testFolder(String folderPath, String extension, CompilerTest testStrategy) {
        Path targetDir = Paths.get(folderPath);

        if (!Files.exists(targetDir) || !Files.isDirectory(targetDir)) {
            System.err.println("Error: Target directory does not exist or is invalid: " + folderPath);
            return;
        }

        System.out.println("=====================================================================");
        System.out.println("🚀 BATCH TESTING IN: " + folderPath + " | Strategy: " + testStrategy.getClass().getSimpleName());
        System.out.println("=====================================================================");

        AtomicInteger fileCounter = new AtomicInteger(1);

        try (Stream<Path> stream = Files.list(targetDir)) {

            List<Path> targetFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(extension.toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());

            for (Path filePath : targetFiles) {
                System.out.println("\n------------------------------------------------------------");
                System.out.println("📄 File " + fileCounter.getAndIncrement() + " : " + filePath.getFileName());
                System.out.println("------------------------------------------------------------");

                try {
                    // 🌟 POLYMORPHIC CALL: Runs the specific implementation passed to the method
                    testStrategy.test(filePath.toString());
                } catch (Exception e) {
                    System.err.println("❌ Failure in [" + filePath.getFileName() + "]: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Failed to read files from directory: " + e.getMessage());
        }

        System.out.println("\n=====================================================================");
        System.out.println("✅ COMPLETED. Processed " + (fileCounter.get() - 1) + " files.");
        System.out.println("=====================================================================");
    }
}

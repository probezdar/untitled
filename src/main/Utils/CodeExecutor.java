package main.Utils;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;


public final class CodeExecutor {

    private CodeExecutor() {
        throw new UnsupportedOperationException("Утилитный класс");
    }

    public static String compileAndRun(String className, String sourceCode){
        if (className == null || className.isBlank()) {
            return "Ошибка: имя класса не может быть пустым.";
        }
        if (sourceCode == null || sourceCode.isBlank()) {
            return "Ошибка: исходный код не может быть пустым.";
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return "Ошибка: компилятор Java не найден.\n" +
                    "Убедитесь, что запуск производится через JDK, а не JRE.";
        }

        Path tempDir;
        try{
            tempDir = Files.createTempDirectory("cyberPractice_");
        } catch (IOException e) {
            return "Ошибка создания временной директории:" + e.getMessage();
        }

        try{
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            JavaFileObject file = new JavaSourceFromString(className, sourceCode);
            Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);
            Iterable<String> options = Arrays.asList("-d", tempDir.toString());

            JavaCompiler.CompilationTask task = compiler.getTask(
                    null, null, diagnostics, options, null, compilationUnits
            );

            boolean success = task.call();
            if (!success) {
                StringBuilder sb = new StringBuilder("Ошибка компиляции:\n");
                for (Diagnostic<?> d : diagnostics.getDiagnostics()) {
                    if (d.getKind() == Diagnostic.Kind.ERROR) {
                        sb.append("Строка ").append(d.getLineNumber())
                                .append(": ").append(d.getMessage(null)).append("\n");
                    }
                }
                return sb.toString();
            }

            try (URLClassLoader classLoader = URLClassLoader.newInstance(
                    new URL[]{tempDir.toUri().toURL()}
            )) {
                Class<?> cls = classLoader.loadClass(className);
                Method method = cls.getMethod("main", String[].class);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream oldOut = System.out;
                PrintStream oldErr = System.err;
                try {
                    System.setOut(new PrintStream(baos));
                    System.setErr(new PrintStream(baos));
                    method.invoke(null, (Object) new String[]{});
                } finally {
                    System.setOut(oldOut);
                    System.setErr(oldErr);
                }

                String output = baos.toString();
                return output.isBlank() ? "[Программа выполнена, вывод отсутствует]" : output;
            }

        } catch (Exception e) {
            return "Ошибка выполнения: " + e.getMessage();
        } finally {
            cleanupTempDir(tempDir);
        }
    }

    private static void cleanupTempDir(Path tempDir) {
        try {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())  // сначала файлы, потом папки
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            System.err.println("Не удалось удалить временные файлы: " + e.getMessage());
        }
    }

    static class JavaSourceFromString extends SimpleJavaFileObject {
        final String code;

        JavaSourceFromString(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') +
                    Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
}
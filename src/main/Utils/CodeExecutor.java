package main.Utils;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;


public class CodeExecutor {
    public static String compileAndRun(String className, String sourceCode){
        try{
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            JavaFileObject file = new JavaSourceFromString(className,sourceCode);
            Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);
            JavaCompiler.CompilationTask task = compiler.getTask(null,null, diagnostics, null, null, compilationUnits);
            boolean success =  task.call();
            if (!success){
                StringBuilder sb = new StringBuilder("Ошибка компиляции:\n");
                for (Diagnostic<?> d : diagnostics.getDiagnostics()){
                    sb.append(d.toString()).append("\n");
                }
                return sb.toString();
            }
            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{new File("./").toURI().toURL()});
            Class<?> cls = classLoader.loadClass(className);
            Method method = cls.getMethod("main", String[].class);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream oldOut = System.out;
            System.setOut(ps);
            method.invoke(null, (Object) new String[]{});
            System.setOut(oldOut);
            return baos.toString();

        } catch (Exception e) {
            return "Ошибка выполнения: " + e.getMessage();
        }
    }

    static class JavaSourceFromString extends SimpleJavaFileObject{
        final String code;
        JavaSourceFromString(String name, String code){
            super(URI.create("string:///" + name.replace('.','/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors){
            return code;
        }
    }
}


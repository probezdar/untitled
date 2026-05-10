package main.Utils;

public final class SecurityUtils {
    private SecurityUtils() {
        throw new UnsupportedOperationException("Утилитный класс");
    }

    public static String safeSqlInjection(String query, String parameter){
        if (query == null) throw new IllegalArgumentException("Query не может быть null");
        if (parameter == null) parameter = "";

        String escaped = parameter.replace("'", "''");

        return query.replace("?", "'" + escaped + "'") +
                "\n[Параметр экранирован: спецсимволы не интерпретируются как SQL]";
    }

    public static String escapeHtml(String input){
        if (input == null) return "";
        if (input.isEmpty()) return "";

        StringBuilder sb = new StringBuilder(input.length());
        for (char c : input.toCharArray()){
            switch (c){
                case '<'  -> sb.append("&lt;");
                case '>'  -> sb.append("&gt;");
                case '&'  -> sb.append("&amp;");
                case '"'  -> sb.append("&quot;");
                case '\'' -> sb.append("&#x27;");
                case '/'  -> sb.append("&#x2F;");
                default   -> sb.append(c);
            }
        }
        return sb.toString();
    }
}

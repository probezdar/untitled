package main.Utils;

public class SecurityUtils {
    // Эмуляция PreparedStatement
    public static String safeSqlInjection(String query, String parameter){
        // Здесь был бы вызов PreparedStatement.setString()
        return query + "параметр экранирован: '" + parameter.replace("'","''")+ "')";
    }

    public static String escapeHtml(String input){
        if (input == null) return null;
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()){
            switch (c){
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '&': sb.append("&amp;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&#x27;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
}

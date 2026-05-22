package main.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.*;


public class CodePlayground extends JFrame {
    private final UndoManager undoManager = new UndoManager();
    private CodeAutoComplete autoComplete;
    private final JFrame parentWindow;
    private JTextArea codeArea;
    private JTextArea outputArea;
    private JLabel statusLabel;
    private JButton btnRun;
    private JButton btnStop;

    private SwingWorker<String, String> currentWorker = null;
    private Future<?> currentTask = null;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final int TIMEOUT_SECONDS = 5;

    private static final String TEMPLATE_SQL = """
            public class Example {
                public static void main(String[] args) {
                    //Симуляция SQL-инъекции
                    String userInput = "admin' OR '1'='1' --";
                    String query = "SELECT * FROM users WHERE username = '" + userInput + "'";
                    \s
                    System.out.println("=== SQL Injection ===");
                    System.out.println("Запрос: " + query);
                    System.out.println("Результат: все пользователи получены!");
                }
            }
            """;

    private static final String TEMPLATE_XSS = """
            public class Example {
                public static void main(String[] args) {
                    // Симуляция XSS
                    String userComment = "<script>alert('XSS')</script>";
                   \s
                    // Уязвимый вывод
                    String vulnerable = "<div>" + userComment + "</div>";
                   \s
                    // Безопасный вывод (экранирование)
                    String safe = userComment
                        .replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;");
                   \s
                    System.out.println("=== XSS Демонстрация ===");
                    System.out.println("Уязвимо: " + vulnerable);
                    System.out.println("Безопасно: <div>" + safe + "</div>");
                }
            }
            """;

    private static final String TEMPLATE_EMPTY = """
            public class Example {
                public static void main(String[] args) {
                    // Напишите ваш код здесь
                    System.out.println("Привет, CyberPractice!");
                }
            }
            """;

    public CodePlayground(JFrame parentWindow) {
        this.parentWindow = parentWindow;

        JFrame.setDefaultLookAndFeelDecorated(true);
        setTitle("CyberPractice - Песочница");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new java.awt.event.WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                int res = JOptionPane.showConfirmDialog(
                        CodePlayground.this,
                        "Вернуться в главное меню?",
                        "Выход из песочницы",
                        JOptionPane.YES_NO_OPTION
                );
                if (res == JOptionPane.YES_OPTION){
                    stopExecution();
                    executor.shutdown();
                    dispose();
                    parentWindow.setVisible(true);
                }
            }
        });

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.width * 0.7);
        int height = (int) (screenSize.height * 0.7);
        setPreferredSize(new Dimension(width, height));
        setLocation(
                (screenSize.width - width) / 2,
                (screenSize.height - height) / 2
        );

        initComponents();
        pack();
        setVisible(true);
    }

    private void initComponents(){
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30,30,46));
        headerPanel.setBorder(new EmptyBorder(12,16,8,16));

        JLabel titleLabel = new JLabel("Песочница - напишите и запустите Java-код");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(137,180,250));

        JLabel hintLabel = new JLabel(
                "Ctrl+Enter — запустить | Tab — автодополнение | Время выполнения: "
                        + TIMEOUT_SECONDS + " сек."
        );

        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hintLabel.setForeground(new Color(166,173,200));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(hintLabel, BorderLayout.SOUTH);

        // ШАБЛОНЫ
        JPanel templatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        templatePanel.setBackground(new Color(40,40, 58));
        templatePanel.setBorder(new EmptyBorder(2,8,2,8));

        JLabel templateLabel = new JLabel("Шаблоны:");
        templateLabel.setForeground(new Color(166,173,200));
        templateLabel.setFont(new Font("Segou UI", Font.PLAIN,13));

        JButton tplEmpty = createTemplateButton("Пустой", TEMPLATE_EMPTY);
        JButton tplSql = createTemplateButton("SQL Injection", TEMPLATE_SQL);
        JButton tplXss = createTemplateButton("XSS", TEMPLATE_XSS);

        templatePanel.add(templateLabel);
        templatePanel.add(tplEmpty);
        templatePanel.add(tplSql);
        templatePanel.add(tplXss);

        // РЕДАКТОР КОДА
        codeArea = new JTextArea(TEMPLATE_EMPTY);
        codeArea.getDocument().addUndoableEditListener(undoManager);
        autoComplete = new CodeAutoComplete(codeArea);

        codeArea.setFont(new Font("JetBrains Mono", Font.PLAIN,14));
        codeArea.setBackground(new Color(36,36,52));
        codeArea.setForeground(new Color(220,220,235));
        codeArea.setCaretColor(Color.WHITE);
        codeArea.setTabSize(4);
        codeArea.setBorder(new EmptyBorder(10,12,10,12));
        codeArea.setSelectionColor(new Color(86,130,200,150));

        codeArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                int pos = codeArea.getCaretPosition();
                String text = codeArea.getText();

                switch (c) {
                    case '(' -> insertPair(text, pos, e, '(', ')');
                    case '{' -> insertPair(text,pos, e, '{', '}');
                    case '[' -> insertPair(text,pos,e,'[',']');
                    case '"' -> insertPair(text,pos, e, '"', '"');
                    case '\'' -> insertPair(text,pos, e, '\'', '\'');
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                int pos =  codeArea.getCaretPosition();
                String text = codeArea.getText();

                if (code == KeyEvent.VK_Z && e.isControlDown() &&  !e.isShiftDown()) {
                    e.consume();
                    if (undoManager.canUndo()){
                        undoManager.undo();
                    }
                    return;
                }

                if (code == KeyEvent.VK_Z && e.isControlDown() &&  e.isShiftDown()) {
                    e.consume();
                    if (undoManager.canRedo()){
                        undoManager.redo();
                    }
                    return;
                }

                if (code == KeyEvent.VK_TAB && e.isShiftDown()){
                    e.consume();
                    removeIndent(codeArea,pos);
                    return;
                }

                if (code == KeyEvent.VK_TAB){
                    e.consume();
                    if (autoComplete != null && autoComplete.isPopupVisible()){
                        autoComplete.applyCurrentSelection();
                        return;
                    }
                    insertText(codeArea,pos, "    ");
                    return;
                }

                if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown()){
                    e.consume();
                    runCode();
                }

                if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_CLOSE_BRACKET){
                    if (pos < text.length()){
                        char next = text.charAt(pos);
                        char expected = switch(codeArea.getCaret().getMagicCaretPosition() != null ? text.charAt(pos) : ' ') {
                            case ')' -> ')';
                            case '}' -> '}';
                            case ']' -> ']';
                            case '"' -> '"';
                            case '\'' -> '\'';
                            default -> ' ';
                        };
                        if (next == expected && (code == KeyEvent.VK_RIGHT)){
                            e.consume();
                            codeArea.setCaretPosition(pos + 1);
                        }
                    }
                }

                if (code == KeyEvent.VK_ENTER) {
                    e.consume();
                    if (autoComplete != null && autoComplete.isPopupVisible()){
                        autoComplete.applyCurrentSelection();
                        return;
                    }
                    smartEnter(codeArea,text,pos);
                    return;
                }

                if (code == KeyEvent.VK_BACK_SPACE) {
                    e.consume();

                    if (codeArea.getSelectedText() != null) {
                        codeArea.replaceSelection("");
                        return;
                    }
                    smartBackspace(codeArea, text, pos);
                    return;
                }

                if (code == KeyEvent.VK_DELETE){
                    e.consume();
                    if (codeArea.getSelectedText() != null) {
                        codeArea.replaceSelection("");
                        return;
                    }
                    smartDelete(codeArea, text, pos);
                    return;
                }


                if (e.getKeyCode() == KeyEvent.VK_TAB){
                    e.consume();
                    int caretPos = codeArea.getCaretPosition();
                    try{
                        codeArea.getDocument().insertString(caretPos, "    ", null);
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        JTextArea lineNumbers = new JTextArea("1");
        lineNumbers.setBackground(new Color(28, 28, 42));
        lineNumbers.setForeground(new Color(100, 110, 140));
        lineNumbers.setFont(new Font("JetBrains Mono", Font.PLAIN, 14));
        lineNumbers.setEditable(false);
        lineNumbers.setBorder(new EmptyBorder(10, 6, 10, 6));

        codeArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLines();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateLines();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateLines();
            }

            private void updateLines(){
                SwingUtilities.invokeLater(() -> {
                    int lines = codeArea.getLineCount();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i <= lines; i++){
                        sb.append(i);
                        if (i < lines) sb.append("\n");
                    }
                    lineNumbers.setText(sb.toString());
                });
            }
        });

        JScrollPane codeScroll = new JScrollPane(codeArea);
        codeScroll.setRowHeaderView(lineNumbers);
        codeScroll.setBorder(
                BorderFactory.createLineBorder(new Color(86,130,200),1)
        );

        JLabel codeLabel = new JLabel("  Редактор кода  [Ctrl+Enter — запустить]");
        codeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        codeLabel.setForeground(new Color(137, 180, 250));
        codeLabel.setBackground(new Color(40, 40, 56));
        codeLabel.setOpaque(true);
        codeLabel.setBorder(new EmptyBorder(6, 8, 6, 8));
        codeScroll.setColumnHeaderView(codeLabel);

        //ОБЛАСТЬ ВЫВОДА
        outputArea = new JTextArea("Результат выполнения появится здесь...");
        outputArea.setEditable(false);
        outputArea.setFont(new Font("JetBrains Mono", Font.PLAIN,14));
        outputArea.setBackground(new Color(24,24,36));
        outputArea.setForeground(new Color(166,227,161));
        outputArea.setCaretColor(Color.WHITE);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setBorder(new EmptyBorder(10,12,10,12));

        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createLineBorder(new Color(64,160,110),1));

        JLabel outputLabel = new JLabel("Вывод программы");
        outputLabel.setFont(new Font("Segou UI", Font.BOLD,13));
        outputLabel.setForeground(new Color(166,227,161));
        outputLabel.setBackground(new Color(30,40,36));
        outputLabel.setOpaque(true);
        outputLabel.setBorder(new EmptyBorder(6,8,6,8));
        outputScroll.setColumnHeaderView(outputLabel);

        //РАЗДЕЛИТЕЛЬ РЕДАКТОРА И ВЫВОДА

        JSplitPane splitPane= new JSplitPane(
          JSplitPane.HORIZONTAL_SPLIT, codeScroll,outputScroll
        );
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerSize(5);
        splitPane.setBackground(new Color(30,30,46));

        //КНОПКИ УПРАВЛЕНИЯ
        btnRun = createActionButton("\u25BA Запустить", new Color(64,160,110));
        btnStop = createActionButton("■ Остановить", new Color(200,80,80));
        JButton btnClear = createActionButton(" Очистить вывод", new Color(100,100,140));

        btnStop.setEnabled(false);
        btnStop.setOpaque(true);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,8));
        buttonPanel.setBackground(new Color(30,30,46));
        buttonPanel.add(btnClear);
        buttonPanel.add(btnRun);
        buttonPanel.add(btnStop);

        //СТАТУС БАР
        statusLabel = new JLabel("  Готов к запуску. Нажмите \u25BA Запустить.");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(166, 173, 200));
        statusLabel.setBackground(new Color(24, 24, 36));
        statusLabel.setOpaque(true);
        statusLabel.setBorder(new EmptyBorder(4, 8, 4, 8));

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(new Color(30, 30, 46));
        southPanel.add(buttonPanel, BorderLayout.EAST);
        southPanel.add(statusLabel, BorderLayout.WEST);

        //СБОРКА
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(30, 30, 46));
        topPanel.add(headerPanel,  BorderLayout.NORTH);
        topPanel.add(templatePanel, BorderLayout.SOUTH);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 6));
        contentPanel.setBackground(new Color(30, 30, 46));
        contentPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
        contentPanel.add(topPanel,   BorderLayout.NORTH);
        contentPanel.add(splitPane,  BorderLayout.CENTER);
        contentPanel.add(southPanel, BorderLayout.SOUTH);

        setContentPane(contentPanel);

        //ДЕЙСТВИЯ
        btnRun.addActionListener(e -> runCode());
        btnStop.addActionListener(e -> stopExecution());
        btnClear.addActionListener(e -> {
            outputArea.setText("");
            outputArea.setForeground(new Color(166, 227, 161));
            undoManager.discardAllEdits();
            setStatus("Вывод очищен.");
        });
    }

    private void runCode() {
        String code = codeArea.getText().trim();
        if (code.isEmpty()) {
            setStatus("Напишите код перед запуском.");
            return;
        }

        // Сбрасываем вывод перед каждым запуском
        outputArea.setText("");
        outputArea.setForeground(new Color(166, 227, 161));

        setRunningState(true);
        setStatus("Компиляция...");

        currentWorker = new SwingWorker<>() {

            @Override
            protected String doInBackground() throws Exception {
                setStatus("Выполнение...");

                currentTask = executor.submit(() ->
                        CodeExecutor.compileAndRun("Example", code)
                );

                try {

                    return (String) ((Future<?>) currentTask).get(
                            TIMEOUT_SECONDS, TimeUnit.SECONDS
                    );
                } catch (TimeoutException e) {
                    currentTask.cancel(true); // прерываем поток
                    return "TIMEOUT: Выполнение прервано — превышено время ожидания ("
                            + TIMEOUT_SECONDS + " сек).\n\n"
                            + "Возможные причины:\n"
                            + "  • Бесконечный цикл (while(true), for(;;))\n"
                            + "  • Очень долгие вычисления\n\n"
                            + "Совет: проверьте условие выхода из цикла.";
                } catch (CancellationException e) {
                    return "Выполнение остановлено пользователем.";
                } catch (ExecutionException e) {
                    return "Ошибка выполнения: " + e.getCause().getMessage();
                }
            }

            @Override
            protected void done() {
                if (isCancelled()) {
                    outputArea.setForeground(new Color(243, 139, 168));
                    outputArea.setText("Выполнение остановлено пользователем.");
                    setStatus("Остановлено.");
                    setRunningState(false);
                    return;
                }

                try {
                    String result = get();
                    outputArea.setText(result);
                    outputArea.setCaretPosition(0);

                    if (result.startsWith("TIMEOUT")) {
                        outputArea.setForeground(new Color(250, 179, 135)); // оранжевый
                        setStatus("Превышено время выполнения (" + TIMEOUT_SECONDS + " сек).");
                    } else if (result.startsWith("Ошибка")
                            || result.startsWith("ошибка")
                            || result.contains("Ошибка компиляции")) {
                        outputArea.setForeground(new Color(243, 139, 168)); // красный
                        setStatus("Ошибка выполнения.");
                    } else {
                        outputArea.setForeground(new Color(166, 227, 161)); // зелёный
                        setStatus("Выполнено успешно.");
                    }
                } catch (Exception e) {
                    outputArea.setText("Внутренняя ошибка: " + e.getMessage());
                    setStatus("Внутренняя ошибка.");
                } finally {
                    setRunningState(false);
                }
            }
        };

        currentWorker.execute();
    }

    private void stopExecution() {
        if (currentWorker != null && !currentWorker.isDone()){
            currentWorker.cancel(true);
        }
        if (currentTask != null && !currentTask.isDone()){
            currentTask.cancel(true);
        }
        outputArea.setForeground(new Color(243,139,168));
        outputArea.setText("Выполнение остановлено пользователем.");
        setStatus("Остановлено.");
        setRunningState(false);
    }

    private void setRunningState(boolean running) {
        btnRun.setEnabled(!running);
        btnStop.setEnabled(running);
        codeArea.setEditable(!running);
        if (running) {
            codeArea.setBackground(new Color(30,30,44));
        } else{
            codeArea.setBackground(new Color(36,36,52));
        }
    }

    private void setStatus(String text){
        statusLabel.setText("  " + text);
    }

    private JButton createTemplateButton(String text, String template){
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN,12));
        btn.setForeground(new Color(220,220,235));
        btn.setBackground(new Color(60,62,80));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e ->{
            codeArea.setText(template);
            setStatus("Шаблон загружен. Нажмите \u25BA Запустить.");
        });
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(86,130,200));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(60,62,80));
            }
        });
        return btn;
    }

    private JButton createActionButton(String text, Color accent){
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(49,50,68));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150,38));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(accent);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(49,50,68));
            }
        });
        return btn;
    }

    //ВСТАВКА ПАРЫ СКОБОК
    private void insertPair(String text, int pos, KeyEvent e, char open, char close){
        e.consume();
        if ((open == '"' || open == '\'') && pos < text.length()  && text.charAt(pos) == open){
            codeArea.setCaretPosition(pos + 1);
            return;
        }

        String insert = String.valueOf(open) + close;
        try {
            codeArea.getDocument().insertString(pos,insert,null);
            codeArea.setCaretPosition(pos+1);
        } catch (Exception ignored) {}
    }


    //УМНЫЙ ENTER
    private void smartEnter (JTextArea area, String text, int pos) {
        try {
            int lineStart = text.lastIndexOf('\n', pos - 1) + 1;
            String currentLine = text.substring(lineStart, pos);

            StringBuilder indent = new StringBuilder();
            for (char c : currentLine.toCharArray()) {
                if (c == ' ') indent.append(' ');
                else if (c == '\t') indent.append("    ");
                else break;
            }

            char lastNonSpace = getLastNonSpace(text, pos);
            char nextNonSpace = getNextNonSpace(text, pos);

            String extra = "";
            boolean addExtraEnd = false;

            if (lastNonSpace == '{' || lastNonSpace == ':' || lastNonSpace == '(') {
                extra = "    ";
                addExtraEnd = true;
            }
            StringBuilder insert = new StringBuilder("\n");
            insert.append(indent);

            if (addExtraEnd) {
                insert.append(extra);
                // Если после курсора стоит }, добавляем \n+отступ перед ней
                if (nextNonSpace == '}') {
                    String closingLine = "\n" + indent;
                    String insertStr = insert.toString() + "\n" + indent;
                    area.getDocument().insertString(pos, insertStr, null);
                    area.setCaretPosition(pos + insert.length());
                    return;
                }
            }

            area.getDocument().insertString(pos, insert.toString(), null);
            area.setCaretPosition(pos + insert.length());
        } catch (Exception ignored) {}
    }

    private void smartBackspace(JTextArea area, String text, int pos) {
        if (pos == 0) return;

        try {
            // Проверяем символы перед курсором
            int spacesBefore = 0;
            int checkPos = pos - 1;

            while (checkPos >= 0 && text.charAt(checkPos) == ' ') {
                spacesBefore++;
                checkPos--;
            }

            // Если перед курсором ровно кратное число пробелов (4, 8, 12...)
            // и мы стоим на границе отступа — удаляем весь отступ
            if (spacesBefore > 0 && spacesBefore % 4 == 0
                    && (checkPos < 0 || text.charAt(checkPos) == '\n')) {
                // Удаляем весь блок отступа
                area.getDocument().remove(pos - spacesBefore, spacesBefore);
                return;
            }

            // Проверяем удаление парных скобок
            char before = text.charAt(pos - 1);
            char after = pos < text.length() ? text.charAt(pos) : '\0';

            if (isPair(before, after)) {
                // Удаляем обе скобки сразу
                area.getDocument().remove(pos - 1, 2);
                return;
            }

            // Обычное удаление одного символа
            area.getDocument().remove(pos - 1, 1);

        } catch (Exception ignored) {}
    }

    private void smartDelete(JTextArea area, String text, int pos) {
        if (pos >= text.length()) return;

        try {
            char before = pos > 0 ? text.charAt(pos - 1) : '\0';
            char after = text.charAt(pos);

            if (isPair(before, after)) {
                area.getDocument().remove(pos - 1, 2);
                area.setCaretPosition(pos - 1);
                return;
            }

            area.getDocument().remove(pos, 1);

        } catch (Exception ignored) {}
    }

    private void removeIndent(JTextArea area, int pos) {
        try {
            int lineStart = area.getText().lastIndexOf('\n', pos - 1) + 1;
            int spaces = 0;
            while (lineStart + spaces < pos
                    && area.getText().charAt(lineStart + spaces) == ' ') {
                spaces++;
            }
            int toRemove = Math.min(4, spaces);
            if (toRemove > 0) {
                area.getDocument().remove(lineStart, toRemove);
            }
        } catch (Exception ignored) {}
    }

    private void insertText(JTextArea area, int pos, String text) {
        try {
            area.getDocument().insertString(pos, text, null);
            area.setCaretPosition(pos + text.length());
        } catch (Exception ignored) {}
    }

    private boolean isPair(char a, char b) {
        return (a == '(' && b == ')')
                || (a == '{' && b == '}')
                || (a == '[' && b == ']')
                || (a == '"' && b == '"')
                || (a == '\'' && b == '\'');
    }

    private char getLastNonSpace(String text, int pos) {
        int i = pos - 1;
        while (i >= 0 && text.charAt(i) == ' ') i--;
        return i >= 0 ? text.charAt(i) : '\0';
    }

    private char getNextNonSpace(String text, int pos) {
        int i = pos;
        while (i < text.length() && text.charAt(i) == ' ') i++;
        return i < text.length() ? text.charAt(i) : '\0';
    }
}

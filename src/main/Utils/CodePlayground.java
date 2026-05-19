package main.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutionException;

public class CodePlayground extends JFrame {
    private final JFrame parentWindow;
    private JTextArea codeArea;
    private JTextArea outputArea;
    private JLabel statusLabel;

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
                if (res == JOptionPane.YES_NO_OPTION){
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

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(titleLabel, BorderLayout.SOUTH);

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
        codeArea.setFont(new Font("JetBrains Mono", Font.PLAIN,14));
        codeArea.setBackground(new Color(36,36,52));
        codeArea.setForeground(new Color(220,220,235));
        codeArea.setCaretColor(Color.WHITE);
        codeArea.setTabSize(4);
        codeArea.setBorder(new EmptyBorder(10,12,10,12));

        JScrollPane codeScroll = new JScrollPane(codeArea);
        codeScroll.setBorder(BorderFactory.createLineBorder(new Color(86,130,200),1));
        JLabel codeLabel = new JLabel("Редактор кода");
        codeLabel.setFont(new Font("Segou UI", Font.BOLD,13));
        codeLabel.setForeground(new Color(137,180,250));
        codeLabel.setBackground(new Color(40,40,56));
        codeLabel.setOpaque(true);
        codeLabel.setBorder(new EmptyBorder(6,8,6,8));
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
        JButton btnRun = createActionButton("▶  Запустить", new Color(64,160,110));
        JButton btnClear = createActionButton("🗑  Очистить", new Color(100,100,140));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,8));
        buttonPanel.setBackground(new Color(30,30,46));
        buttonPanel.add(btnClear);
        buttonPanel.add(btnRun);

        //СТАТУС БАР
        statusLabel = new JLabel("  Готов к запуску. Нажмите ▶ Запустить.");
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
        btnClear.addActionListener(e -> {
            outputArea.setText("");
            setStatus("Вывод очищен.");
        });
    }

    private void runCode(){
        String code = codeArea.getText().trim();
        if (code.isEmpty()){
            setStatus("Напишите код перед запуском.");
            return;
        }

        setStatus("Компиляция и выполнение...");
        outputArea.setText("");

        SwingWorker<String,Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return CodeExecutor.compileAndRun("Example", code);
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    outputArea.setText(result);
                    outputArea.setCaretPosition(0);

                    if (result.startsWith("Ошибка")) {
                        outputArea.setForeground(new Color(243, 139, 168));
                        setStatus("Ошибка выполнения.");
                    }
                    else {
                        outputArea.setForeground(new Color(166,227,161));
                        setStatus("Выполнено успешно.");
                    }

                } catch (Exception e) {
                    outputArea.setText("Ошибка: " + e.getMessage());
                    setStatus("Внутренняя ошибка.");
                }
            }
        };
        worker.execute();
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
            setStatus("Шаблон загружен. Нажмите ▶ Запустить.");
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
}

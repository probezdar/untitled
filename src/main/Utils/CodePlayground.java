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

    private static final String TEMPLATE_EMPTY = """
            public class Example {
                public static void main(String[] args) {
                    // Напишите ваш код здесь
                    System.out.println("Привет, CyberPractice!");
                }
            }
            """;

    private static final String TEMPLATE_SQL_VULNERABLE = """
        public class Example {
            public static void main(String[] args) {
                // Симуляция уязвимого SQL запроса
                String username = "admin' OR '1'='1' --";
                String password = "anything";
               \s
                // УЯЗВИМО: прямая конкатенация строк
                String query = "SELECT * FROM users "
                             + "WHERE username = '" + username + "' "
                             + "AND password = '" + password + "'";
               \s
                System.out.println("=== SQL Injection — Уязвимый код ===");
                System.out.println("Введённые данные:");
                System.out.println("  Логин:  " + username);
                System.out.println("  Пароль: " + password);
                System.out.println();
                System.out.println("Сформированный запрос:");
                System.out.println("  " + query);
                System.out.println();
               \s
                // Анализ — что произошло с запросом
                if (query.contains("--")) {
                    System.out.println("РЕЗУЛЬТАТ: Проверка пароля закомментирована!");
                    System.out.println("  Всё после -- игнорируется базой данных.");
                    System.out.println("  Вход выполнен без знания пароля!");
                }
                if (query.toLowerCase().contains("or '1'='1'")) {
                    System.out.println("РЕЗУЛЬТАТ: Условие всегда истинно!");
                    System.out.println("  OR '1'='1' возвращает ВСЕ строки таблицы.");
                }
            }
        }
        """;

    private static final String TEMPLATE_SQL_SAFE = """
        public class Example {
            public static void main(String[] args) {
                // Симуляция безопасного SQL запроса (PreparedStatement)
                String username = "admin' OR '1'='1' --";
                String password = "anything";
               \s
                // БЕЗОПАСНО: используем шаблон с параметрами
                String queryTemplate = "SELECT * FROM users "
                                     + "WHERE username = ? "
                                     + "AND password = ?";
               \s
                // Экранируем параметры (эмуляция PreparedStatement)
                String safeUsername = username.replace("'", "''");
                String safePassword = password.replace("'", "''");
               \s
                System.out.println("=== SQL Injection — Безопасный код ===");
                System.out.println("Введённые данные:");
                System.out.println("  Логин:  " + username);
                System.out.println("  Пароль: " + password);
                System.out.println();
                System.out.println("Шаблон запроса (компилируется заранее):");
                System.out.println("  " + queryTemplate);
                System.out.println();
                System.out.println("Параметры после экранирования:");
                System.out.println("  Параметр 1: '" + safeUsername + "'");
                System.out.println("  Параметр 2: '" + safePassword + "'");
                System.out.println();
                System.out.println("Итог: SQL символы воспринимаются как текст.");
                System.out.println("Инъекция невозможна — структура запроса");
                System.out.println("зафиксирована до подстановки данных!");
            }
        }
        """;

    private static final String TEMPLATE_XSS_VULNERABLE = """
        public class Example {
            public static void main(String[] args) {
                // Симуляция XSS — вывод без экранирования
                String userComment = "<script>alert('XSS атака!')</script>";
               \s
                // УЯЗВИМО: вставляем ввод напрямую в HTML
                String html = "<div>Комментарий: " + userComment + "</div>";
               \s
                System.out.println("=== XSS — Уязвимый код ===");
                System.out.println("Ввод пользователя:");
                System.out.println("  " + userComment);
                System.out.println();
                System.out.println("Сгенерированный HTML:");
                System.out.println("  " + html);
                System.out.println();
                System.out.println("Что видит браузер:");
                System.out.println("  Тег <script> — это код!");
                System.out.println("  Браузер ВЫПОЛНИТ скрипт!");
                System.out.println();
               \s
                // Демонстрация других XSS векторов
                String[] payloads = {
                    "<script>document.cookie</script>",
                    "<img src=x onerror=alert('XSS')>",
                    "<svg onload=alert('XSS')>",
                    "javascript:alert('XSS')"
                };
               \s
                System.out.println("Другие варианты XSS payload:");
                for (String payload : payloads) {
                    System.out.println("  " + payload);
                }
            }
        }
        """;

    private static final String TEMPLATE_XSS_SAFE = """
        public class Example {
            public static void main(String[] args) {
                String userComment = "<script>alert('XSS атака!')</script>";
               \s
                // БЕЗОПАСНО: экранируем спецсимволы HTML
                String escaped = escapeHtml(userComment);
                String html = "<div>Комментарий: " + escaped + "</div>";
               \s
                System.out.println("=== XSS — Безопасный код ===");
                System.out.println("Ввод пользователя:");
                System.out.println("  " + userComment);
                System.out.println();
                System.out.println("После escapeHtml():");
                System.out.println("  " + escaped);
                System.out.println();
                System.out.println("Сгенерированный HTML:");
                System.out.println("  " + html);
                System.out.println();
                System.out.println("Что видит браузер:");
                System.out.println("  Теги < > заменены на &lt; &gt;");
                System.out.println("  Браузер показывает их как ТЕКСТ");
                System.out.println("  Скрипт НЕ выполняется!");
            }
           \s
            // Метод экранирования HTML символов
            static String escapeHtml(String input) {
                if (input == null) return "";
                return input
                    .replace("&",  "&amp;")
                    .replace("<",  "&lt;")
                    .replace(">",  "&gt;")
                    .replace("\\"", "&quot;")
                    .replace("'",  "&#x27;");
            }
        }
        """;

    private static final String TEMPLATE_TRAVERSAL = """
        public class Example {
            public static void main(String[] args) {
                String baseDir  = "/var/www/app/files/";
               \s
                // Варианты ввода: обычный и вредоносный
                String[] inputs = {
                    "report.pdf",             // обычный запрос
                    "../secret.txt",          // выход на уровень выше
                    "../../etc/passwd",       // системный файл
                    "....//....//etc/passwd"  // обход простых фильтров
                };
               \s
                System.out.println("=== Directory Traversal — Демонстрация ===");
                System.out.println("Разрешённая папка: " + baseDir);
                System.out.println();
               \s
                for (String input : inputs) {
                    System.out.println("Ввод: " + input);
               \s
                    // УЯЗВИМЫЙ путь — простая конкатенация
                    String vulnerable = baseDir + input;
                    System.out.println("  Уязвимый путь:   " + vulnerable);
               \s
                    // Нормализованный путь (как видит ОС)
                    java.io.File file = new java.io.File(vulnerable);
                    String normalized = file.getAbsolutePath()
                        .replace("\\\\", "/");
                    System.out.println("  После normalize: " + normalized);
               \s
                    // Проверка безопасности
                    boolean isSafe = normalized.startsWith(baseDir);
                    System.out.println("  Безопасно? " + (isSafe ? "ДА" : "НЕТ — АТАКА!"));
                    System.out.println();
                }
               \s
                // Демонстрация правильной проверки
                System.out.println("=== Правильная проверка (getCanonicalPath) ===");
                String userInput = "../../etc/passwd";
                try {
                    java.io.File base      = new java.io.File(baseDir);
                    java.io.File requested = new java.io.File(base, userInput);
                    String canonicalBase   = base.getCanonicalPath();
                    String canonicalReq    = requested.getCanonicalPath();
               \s
                    System.out.println("Канонический BASE:    " + canonicalBase);
                    System.out.println("Канонический запрос:  " + canonicalReq);
                    System.out.println("Доступ разрешён? " +
                        canonicalReq.startsWith(canonicalBase));
                } catch (Exception e) {
                    System.out.println("Результат: " + e.getMessage());
                }
            }
        }
        """;


    private static final String TEMPLATE_PRIVILEGE = """
        public class Example {
            public static void main(String[] args) throws Exception {
                System.out.println("=== Privilege Escalation — Демонстрация ===");
                System.out.println();
               \s
                // Симуляция проверки прав пользователя
                String currentUser = "www-data";
                String sudoConfig  = "www-data ALL=(ALL) NOPASSWD: ALL";
               \s
                System.out.println("Текущий пользователь: " + currentUser);
                System.out.println("Конфигурация sudo:");
                System.out.println("  " + sudoConfig);
                System.out.println();
               \s
                // Анализ конфигурации
                boolean isVulnerable = sudoConfig.contains("NOPASSWD: ALL");
                System.out.println("Анализ конфигурации:");
               \s
                if (isVulnerable) {
                    System.out.println("  КРИТИЧНО: NOPASSWD: ALL обнаружен!");
                    System.out.println("  Пользователь может выполнить ЛЮБУЮ команду");
                    System.out.println("  без ввода пароля!");
                    System.out.println();
                    System.out.println("Симуляция атаки:");
                    System.out.println("  $ sudo su -");
                    System.out.println("  # whoami");
                    System.out.println("  root  <- получены права администратора!");
                }
               \s
                System.out.println();
                System.out.println("=== Безопасная конфигурация ===");
                String safeConfig = "www-data ALL=(ALL) NOPASSWD: /usr/bin/systemctl restart nginx";
                System.out.println("  " + safeConfig);
                System.out.println("  Разрешена только одна конкретная команда.");
               \s
                // Симуляция проверки PATH
                System.out.println();
                System.out.println("=== PATH Hijacking — Проверка ===");
                String path = System.getenv("PATH");
                System.out.println("Текущий PATH: " + path);
               \s
                String[] pathDirs = path != null ? path.split(
                    System.getProperty("os.name")
                        .toLowerCase().contains("win") ? ";" : ":"
                ) : new String[]{};
               \s
                for (String dir : pathDirs) {
                    boolean writable = new java.io.File(dir).canWrite();
                    System.out.println("  " + dir +
                        (writable ? " <- ОПАСНО: доступна для записи!" : " [OK]"));
                }
            }
        }
        """;

    private static final String TEMPLATE_HARDCODED = """
        public class Example {
            // УЯЗВИМО: секреты прямо в коде!
            static final String DB_PASSWORD = "SuperSecret123!";
            static final String API_KEY     = "sk-prod-abc123xyz";
            static final String AWS_KEY     = "AKIAIOSFODNN7EXAMPLE";
           \s
            public static void main(String[] args) {
                System.out.println("=== Hardcoded Credentials — Сравнение ===");
                System.out.println();
               \s
                // УЯЗВИМЫЙ вариант
                System.out.println("--- УЯЗВИМО: секреты в коде ---");
                System.out.println("DB_PASSWORD = \\"" + DB_PASSWORD + "\\"");
                System.out.println("API_KEY     = \\"" + API_KEY     + "\\"");
                System.out.println("AWS_KEY     = \\"" + AWS_KEY     + "\\"");
                System.out.println();
                System.out.println("Проблемы:");
                System.out.println("  1. Виден всем кто видит код");
                System.out.println("  2. Остаётся в Git истории навсегда");
                System.out.println("  3. JAR декомпилируется за секунды");
                System.out.println("  4. Нельзя сменить без пересборки");
                System.out.println();
               \s
                // БЕЗОПАСНЫЙ вариант
                System.out.println("--- БЕЗОПАСНО: переменные окружения ---");
                String safePassword = System.getenv("DB_PASSWORD");
                String safeApiKey   = System.getenv("API_KEY");
               \s
                System.out.println("DB_PASSWORD = " +
                    (safePassword != null ? "****** (загружен из окружения)" : "не задана!"));
                System.out.println("API_KEY     = " +
                    (safeApiKey   != null ? "****** (загружен из окружения)" : "не задана!"));
                System.out.println();
               \s
                // Сканер — ищем секреты в коде (как truffleHog)
                System.out.println("=== Сканер секретов ===");
                String[] codeLines = {
                    "String pass = \\"admin123\\";",
                    "String url  = \\"jdbc:mysql://prod:3306/db\\";",
                    "String key  = System.getenv(\\"API_KEY\\");",
                    "String token = \\"sk_live_abc123xyz\\";",
                    "int port = 8080;"
                };
               \s
                for (String line : codeLines) {
                    boolean suspicious =
                        line.matches(".*\\"[^\\"]*(pass|key|secret|token|pwd)[^\\"].*\\".*") ||
                        line.contains("sk_live_") ||
                        line.contains("jdbc:") ||
                        (line.contains("\\"") && line.toLowerCase()
                            .contains("pass"));
                    System.out.println(
                        (suspicious ? "[!] НАЙДЕН СЕКРЕТ: " : "[OK]             ") +
                        line
                    );
                }
            }
        }
        """;

    private static final String TEMPLATE_MITM = """
        import java.net.*;
        import java.io.*;
       \s
        public class Example {
            public static void main(String[] args) {
                System.out.println("=== Man-in-the-Middle — Демонстрация ===");
                System.out.println();
               \s
                // Сравнение HTTP vs HTTPS
                System.out.println("--- Сравнение протоколов ---");
                System.out.println();
               \s
                String[] urls = {
                    "http://bank.com/login",    // незащищённый
                    "https://bank.com/login"    // защищённый
                };
               \s
                for (String url : urls) {
                    boolean isHttps = url.startsWith("https");
                    System.out.println("URL: " + url);
                    System.out.println("  Протокол:    " + (isHttps ? "HTTPS" : "HTTP"));
                    System.out.println("  Шифрование:  " + (isHttps ? "TLS — данные зашифрованы" : "Нет — данные открыты!"));
                    System.out.println("  MITM атака:  " + (isHttps ? "Злоумышленник видит шифр" : "Злоумышленник видит ВСЁ!"));
                    System.out.println("  Перехват:    " + (isHttps ? "Защищено (сертификат)" : "user=admin&pass=123456"));
                    System.out.println();
                }
               \s
                // Симуляция перехваченного HTTP трафика
                System.out.println("--- Перехваченный HTTP запрос ---");
                String intercepted =
                    "POST /login HTTP/1.1\\n" +
                    "Host: bank.com\\n" +
                    "Content-Type: application/x-www-form-urlencoded\\n" +
                    "\\n" +
                    "username=john&password=MySecret123!&card=4532015112830366";
               \s
                System.out.println(intercepted);
                System.out.println();
                System.out.println("[!] Злоумышленник получил:");
                System.out.println("  Логин:    john");
                System.out.println("  Пароль:   MySecret123!");
                System.out.println("  Карта:    4532015112830366");
                System.out.println();
               \s
                // Защищённый вариант
                System.out.println("--- Тот же запрос через HTTPS ---");
                System.out.println("Злоумышленник видит только зашифрованные данные:");
                System.out.println("  [TLS Record] \\u2593\\u2593\\u2593\\u2593\\u2593\\u2593\\u2593\\u2593\\u2593\\u2593\\u2593\\u2593");
                System.out.println("  Расшифровать без ключа невозможно.");
               \s
                // Проверка доступности хостов
                System.out.println();
                System.out.println("--- Проверка подключения ---");
                String[] hosts = {"google.com", "cloudflare.com"};
                for (String host : hosts) {
                    try {
                        InetAddress addr = InetAddress.getByName(host);
                        System.out.println(host + " -> " + addr.getHostAddress() + " [доступен]");
                    } catch (UnknownHostException e) {
                        System.out.println(host + " -> недоступен (нет интернета)");
                    }
                }
            }
        }
        """;

    private static final String TEMPLATE_DNS = """
        import java.net.*;
       \s
        public class Example {
            public static void main(String[] args) {
                System.out.println("=== DNS Spoofing — Демонстрация ===");
                System.out.println();
               \s
                // Симуляция DNS таблицы (нормальная vs подменённая)
                System.out.println("--- DNS таблица ДО атаки ---");
                String[][] normalDns = {
                    {"bank.com",      "93.184.216.34"},
                    {"google.com",    "142.250.185.46"},
                    {"paypal.com",    "151.101.1.21"},
                    {"vk.com",        "87.240.190.78"}
                };
               \s
                for (String[] entry : normalDns) {
                    System.out.printf("  %-20s -> %s%n", entry[0], entry[1]);
                }
               \s
                System.out.println();
                System.out.println("--- DNS таблица ПОСЛЕ отравления ---");
                String hackerIp = "192.168.1.100";
                String[][] poisonedDns = {
                    {"bank.com",      hackerIp},           // подменён!
                    {"google.com",    "142.250.185.46"},   // не тронут
                    {"paypal.com",    hackerIp},           // подменён!
                    {"vk.com",        "87.240.190.78"}     // не тронут
                };
               \s
                for (String[] entry : poisonedDns) {
                    boolean spoofed = entry[1].equals(hackerIp);
                    System.out.printf("  %-20s -> %-18s %s%n",
                        entry[0], entry[1],
                        spoofed ? "<- ПОДМЕНЁН! (IP хакера)" : "[OK]"
                    );
                }
               \s
                // Реальный DNS резолв для сравнения
                System.out.println();
                System.out.println("--- Реальный DNS резолв (ваша система) ---");
                String[] domainsToCheck = {"google.com", "cloudflare.com", "github.com"};
               \s
                for (String domain : domainsToCheck) {
                    try {
                        InetAddress addr = InetAddress.getByName(domain);
                        System.out.printf("  %-20s -> %s%n",
                            domain, addr.getHostAddress());
                    } catch (UnknownHostException e) {
                        System.out.printf("  %-20s -> не удалось разрешить%n", domain);
                    }
                }
               \s
                // Обнаружение подмены
                System.out.println();
                System.out.println("--- Обнаружение DNS Spoofing ---");
                System.out.println("Сравниваем ответы разных DNS серверов:");
                System.out.println();
               \s
                String[][] comparison = {
                    {"Локальный DNS",    "bank.com", "192.168.1.100", "ПОДОЗРИТЕЛЬНО!"},
                    {"Google 8.8.8.8",  "bank.com", "93.184.216.34", "OK"},
                    {"Cloudflare 1.1.1.1","bank.com","93.184.216.34", "OK"}
                };
               \s
                for (String[] row : comparison) {
                    System.out.printf("  %-25s -> %-18s [%s]%n",
                        row[0], row[2], row[3]);
                }
               \s
                System.out.println();
                System.out.println("Вывод: локальный DNS возвращает другой IP!");
                System.out.println("Признак DNS Spoofing атаки.");
                System.out.println();
                System.out.println("Защита: DNSSEC + DNS over HTTPS (DoH)");
                System.out.println("  Даже при подмене DNS — сертификат сайта");
                System.out.println("  не совпадёт → браузер покажет предупреждение.");
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
        JPanel templatePanel = new JPanel();
        templatePanel.setLayout(new BoxLayout(templatePanel, BoxLayout.Y_AXIS));
        templatePanel.setBackground(new Color(40,40,58));
        templatePanel.setBorder(new EmptyBorder(6,8,6,8));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT,8,4));
        row1.setBackground(new Color(40,40,58));

        JLabel lblBase = createCategoryLabel("Базовые:");
        JButton tplEmpty = createTemplateButton("Пустой", TEMPLATE_EMPTY);

        row1.add(lblBase);
        row1.add(tplEmpty);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT,8,4));
        row2.setBackground(new Color(40,40,58));

        JLabel lblWeb = createCategoryLabel("Веб");
        JButton tplSqlVuln = createTemplateButton("SQL - уязвимый", TEMPLATE_SQL_VULNERABLE);
        JButton tplSqlSafe = createTemplateButton("SQL - безопасный", TEMPLATE_SQL_SAFE);
        JButton tplXssVuln = createTemplateButton("XSS - уязвимый", TEMPLATE_XSS_VULNERABLE);
        JButton tplXssSafe = createTemplateButton("XSS - безопасный",  TEMPLATE_XSS_SAFE);
        JButton tplTraversal = createTemplateButton("Directory Traversal", TEMPLATE_TRAVERSAL);

        row2.add(lblWeb);
        row2.add(tplSqlVuln);
        row2.add(tplSqlSafe);
        row2.add(tplXssVuln);
        row2.add(tplXssSafe);
        row2.add(tplTraversal);

        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT,8,4));
        row3.setBackground(new Color(40,40,58));

        JLabel lblSys = createCategoryLabel("Системные");
        JButton tplPrivilege = createTemplateButton("Privilege Escalation", TEMPLATE_PRIVILEGE);
        JButton tplHardcoded = createTemplateButton("Hardcoded Credentials", TEMPLATE_HARDCODED);

        row3.add(lblSys);
        row3.add(tplPrivilege);
        row3.add(tplHardcoded);

        JPanel row4  = new JPanel(new FlowLayout(FlowLayout.LEFT,8,4));
        row4.setBackground(new Color(40,40,58));

        JLabel lblNet = createCategoryLabel("Сетевые:");
        JButton tplMitm = createTemplateButton("MITM", TEMPLATE_MITM);
        JButton tplDns = createTemplateButton("DNS Spoofing", TEMPLATE_DNS);

        row4.add(lblNet);
        row4.add(tplMitm);
        row4.add(tplDns);

        templatePanel.add(row1);
        templatePanel.add(row2);
        templatePanel.add(row3);
        templatePanel.add(row4);

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

    private JLabel createCategoryLabel(String text){
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD,12));
        label.setForeground(new Color(137,180,250));
        label.setPreferredSize(new Dimension(90,24));
        return label;
    }
}

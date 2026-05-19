package main.modules;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.function.Supplier;

public class Window extends JFrame{
    private final List<Vulnerability> modules;
    private final JFrame parentWindow;
    private DefaultListModel<String> listModel;
    private JList<String> moduleList;
    private JTextArea outputArea;
    private JLabel statusLabel;


    public Window (List<Vulnerability> modules, JFrame parentWindow){
        this.modules = modules;
        this.parentWindow = parentWindow;

        JFrame.setDefaultLookAndFeelDecorated(true);
        setTitle("CyberPractice - Обучение");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override                                                               //ЗАКРЫТИЕ ОКНА
            public void windowClosing(WindowEvent e) {
                int res = JOptionPane.showConfirmDialog(Window.this,
                        "Вернуться в главное меню?",
                        "Выход из обучения",
                        JOptionPane.YES_NO_OPTION);
                if (res == JOptionPane.YES_OPTION)
                    dispose();
                    parentWindow.setVisible(true);
            }
        });

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = screenSize.width / 2;
        int h = screenSize.height / 2;
        setPreferredSize(new Dimension(w, h));
        setMinimumSize(new Dimension(700,400));
        setLocation((screenSize.width - w)/2, (screenSize.height - h)/2);


        initComponents();
        pack();
        setVisible(true);
    }

    private void initComponents(){                              //РЕАЛИЗАЦИЯ ВКЛАДКИ
        listModel = new DefaultListModel<>();
        for (Vulnerability module : modules){
            listModel.addElement("  "+ module.getTitle());
        }
        moduleList = new JList<>(listModel);
        moduleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        moduleList.setBackground(new Color(49, 50, 68));
        moduleList.setForeground(Color.WHITE);
        moduleList.setSelectionBackground(new Color(86, 130, 200));
        moduleList.setSelectionForeground(Color.WHITE);
        moduleList.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        moduleList.setFixedCellHeight(45);
        moduleList.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel listHint = new JLabel(" Двойной клик — описание модуля");
        listHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        listHint.setForeground(new Color(166, 173, 200));
        listHint.setBorder(new EmptyBorder(4, 8, 4, 4));
        listHint.setBackground(new Color(40, 40, 56));
        listHint.setOpaque(true);

        JPanel listHeader = new JPanel(new BorderLayout());
        listHeader.setBackground(new Color(40, 40, 56));
        JLabel modulesLabel = new JLabel("  Модули");
        modulesLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        modulesLabel.setForeground(new Color(137, 180, 250));
        modulesLabel.setBorder(new EmptyBorder(8, 8, 8, 8));
        modulesLabel.setBackground(new Color(40, 40, 56));
        modulesLabel.setOpaque(true);
        listHeader.add(modulesLabel, BorderLayout.NORTH);
        listHeader.add(listHint, BorderLayout.SOUTH);

        JScrollPane listScroll = new JScrollPane(moduleList);
        listScroll.setPreferredSize(new Dimension(220, 0));
        listScroll.setColumnHeaderView(listHeader);
        listScroll.setBorder(BorderFactory.createLineBorder(new Color(86, 130, 200), 1));

        // ── ДВОЙНОЙ КЛИК → описание ────────────────────────────
        moduleList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showDescription();
                }
            }
        });

        // ── При выборе модуля показываем подсказку ─────────────
        moduleList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = moduleList.getSelectedIndex();
                if (index != -1) {
                    Vulnerability module = modules.get(index);
                    outputArea.setText(
                            "Выбран модуль: " + module.getTitle() + "\n\n" +
                                    "Используйте кнопки ниже для изучения материала.\n" +
                                    " Совет: начните с описания, затем изучите примеры кода.\n\n" +
                                    "• [Описание]           — что такое эта уязвимость\n" +
                                    "• [Уязвимый код]       — как выглядит небезопасный код\n" +
                                    "• [Исправленный код]   — как правильно защититься\n" +
                                    "• [Симулировать]       — живая демонстрация атаки"
                    );
                }
            }
        });
        // ── ОБЛАСТЬ ВЫВОДА ─────────────────────────────────────
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        outputArea.setBackground(new Color(36, 36, 52));
        outputArea.setForeground(new Color(220, 220, 235));
        outputArea.setCaretColor(Color.WHITE);
        outputArea.setBorder(new EmptyBorder(12, 14, 12, 14));
        outputArea.setText(
                "Добро пожаловать в модуль обучения!\n\n" +
                        "Выберите тему из списка слева, чтобы начать.\n\n" +
                        "  Подсказка: дважды кликните по теме для просмотра описания."
        );

        JScrollPane textScroll = new JScrollPane(outputArea);
        textScroll.setBorder(BorderFactory.createLineBorder(new Color(86, 130, 200), 1));

        // ── КНОПКИ ─────────────────────────────────────────────
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        buttonPanel.setBackground(new Color(30, 30, 46));
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnDesc     = createActionButton("Описание",         new Color(86, 130, 200));
        JButton btnVul      = createActionButton("Уязвимый код",     new Color(200, 80, 80));
        JButton btnFixed    = createActionButton("Исправленный код",  new Color(64, 160, 110));
        JButton btnSimulate = createActionButton("Симулировать",      new Color(180, 100, 40));

        buttonPanel.add(btnDesc);
        buttonPanel.add(btnVul);
        buttonPanel.add(btnFixed);
        buttonPanel.add(btnSimulate);

        // ── СТАТУС-БАР ─────────────────────────────────────────
        statusLabel = new JLabel("  Выберите модуль из списка слева");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(166, 173, 200));
        statusLabel.setBackground(new Color(24, 24, 36));
        statusLabel.setOpaque(true);
        statusLabel.setBorder(new EmptyBorder(4, 8, 4, 8));

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buttonPanel, BorderLayout.CENTER);
        southPanel.add(statusLabel, BorderLayout.SOUTH);

        // ── РАЗДЕЛИТЕЛЬ ────────────────────────────────────────
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, listScroll, textScroll
        );
        splitPane.setDividerLocation(220);
        splitPane.setDividerSize(4);
        splitPane.setBackground(new Color(30, 30, 46));

        // ── СБОРКА ─────────────────────────────────────────────
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(30, 30, 46));
        contentPanel.add(splitPane, BorderLayout.CENTER);
        contentPanel.add(southPanel, BorderLayout.SOUTH);

        setContentPane(contentPanel);

        // ── ПОДВЯЗКА КНОПОК ────────────────────────────────────
        btnDesc.addActionListener(e -> showDescription());
        btnVul.addActionListener(e -> showVulnerable());
        btnFixed.addActionListener(e -> showFixed());
        btnSimulate.addActionListener(e -> simulateThreat());
    }

// ── ФАБРИЧНЫЙ МЕТОД ДЛЯ КНОПОК ────────────────────────────
private JButton createActionButton(String text, Color accent) {
    JButton btn = new JButton(text);
    btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
    btn.setForeground(Color.WHITE);
    btn.setBackground(new Color(49, 50, 68));
    btn.setBorderPainted(false);
    btn.setFocusPainted(false);
    btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    btn.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            btn.setBackground(accent);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            btn.setBackground(new Color(49, 50, 68));
        }
    });
    return btn;
}

    private Vulnerability getSelectedModule() {
        int index = moduleList.getSelectedIndex();
        if (index == -1) {
            outputArea.setText("Сначала выберите модуль из списка слева.");
            setStatus("Модуль не выбран");
            return null;
        }
        return modules.get(index);
    }

    private void setStatus(String text) {
        statusLabel.setText("  " + text);
    }

    private void showDescription(){                             //КНОПКА ВЫВОДА ОПИСАНИЯ
        Vulnerability module = getSelectedModule();
        if (module == null) return;
        outputArea.setText(module.getDescription());
        outputArea.setCaretPosition(0);
        setStatus("Описание: " + module.getTitle());
    }

    private void showVulnerable(){                  //КНОПКА ВЫВОДА УЯЗВИМОСТИ
        Vulnerability module = getSelectedModule();
        if (module == null) return;
        outputArea.setText(module.getVulnerableExample());
        outputArea.setCaretPosition(0);
        setStatus("Уязвимый код: " + module.getTitle());
    }

    private void showFixed(){                                   //КНОПКА ВЫВОДА ПРИМЕРА ИСПРАВЛЕНИЯ
        Vulnerability module = getSelectedModule();
        if (module == null) return;
        outputArea.setText(module.getFixedExample());
        outputArea.setCaretPosition(0);
        setStatus("Исправленный код: " + module.getTitle());
    }

    private String captureOutput(Supplier<String> action){                      //ФУНКЦИЯ ПЕРЕХВАТА ВВОДА В КОНСОЛЬ
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.PrintStream old = System.out;
        System.setOut(new java.io.PrintStream(baos));
        try{
            System.setOut(new java.io.PrintStream(baos));
            action.get();
        } finally {
            System.setOut(old);
        }
        return baos.toString();
    }

    private void simulateThreat(){                                          //ФУНКЦИЯ СИМУЛЯЦИИ УЯЗВИМОСТИ
        Vulnerability module = getSelectedModule();
        if (module == null) return;
        setStatus("Симуляция: " + module.getTitle() + "...");
        module.simulate();
        setStatus("Симуляция завершена: " + module.getTitle());
    }
}


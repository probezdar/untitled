package main.modules;

import main.Utils.CodePlayground;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class StartWindow extends JFrame {

    private final List<Vulnerability> modules;

    public StartWindow(List<Vulnerability> modules) {
        this.modules = modules;
        JFrame.setDefaultLookAndFeelDecorated(true);
        setTitle("CyberPractice — Главное меню");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                int res = JOptionPane.showConfirmDialog(
                        StartWindow.this,
                        "Выйти из программы?",
                        "Подтверждение выхода",
                        JOptionPane.YES_NO_OPTION
                );
                if (res == JOptionPane.YES_OPTION)
                    System.exit(0);
            }
        });

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = 800;
        int height = 600;
        setPreferredSize(new Dimension(width, height));
        setMinimumSize(new Dimension(700, 500));
        setLocation(
                (screenSize.width - width) / 2,
                (screenSize.height - height) / 2
        );

        initComponents();
        pack();
        setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(30, 30, 46));

        // ── ШАПКА ──────────────────────────────────────────────
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 30, 46));
        headerPanel.setBorder(new EmptyBorder(30, 20, 10, 20));

        JLabel titleLabel = new JLabel("CyberPractice", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        titleLabel.setForeground(new Color(137, 180, 250));

        JLabel subtitleLabel = new JLabel(
                "Программа повышения квалификации в сфере информационной безопасности",
                SwingConstants.CENTER
        );
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(166, 173, 200));

        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // ── КНОПКИ ─────────────────────────────────────────────
        JPanel buttonGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        buttonGrid.setBackground(new Color(30, 30, 46));
        buttonGrid.setBorder(new EmptyBorder(30, 60, 20, 60));

        MenuButton btnLearn = new MenuButton(
                "  Начать обучение",
                "Изучите уязвимости и способы защиты",
                new Color(64, 160, 110)
        );
        MenuButton btnSandbox = new MenuButton(
                "  Песочница",
                "Пишите и запускайте Java-код",
                new Color(140, 100, 200)

        );
        MenuButton btnCert = new MenuButton(
                "  Получить сертификат",
                "Завершите обучение и тест",
                new Color(180, 130, 60)
        );
        MenuButton btnAbout = new MenuButton(
                "  О программе",
                "Информация о CyberPractice",
                new Color(100, 100, 140)
        );

        buttonGrid.add(btnLearn);
        buttonGrid.add(btnSandbox);
        buttonGrid.add(btnCert);
        buttonGrid.add(btnAbout);

        // ── ФУТЕР ──────────────────────────────────────────────
        JLabel footerLabel = new JLabel(
                "© 2026 CyberPractice  |  Версия 1.0",
                SwingConstants.CENTER
        );
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(new Color(100, 100, 120));
        footerLabel.setBorder(new EmptyBorder(10, 0, 15, 0));

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(buttonGrid, BorderLayout.CENTER);
        mainPanel.add(footerLabel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // ── ДЕЙСТВИЯ КНОПОК ────────────────────────────────────
        btnLearn.addActionListener(e -> openLearning());
        btnSandbox.addActionListener(e -> {
            setVisible(false);
            new CodePlayground(this);
        });
        btnCert.addActionListener(e -> openCertificate());
        btnAbout.addActionListener(e -> openAbout());
    }

    private void openLearning() {
        setVisible(false);
        new Window(modules, this); // передаём себя чтобы вернуться
    }

    private void openTest() {
        JOptionPane.showMessageDialog(
                this,
                "Модуль тестирования находится в разработке.\nЗавершите обучение для доступа.",
                "Тестирование",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void openCertificate() {
        JOptionPane.showMessageDialog(
                this,
                "Сертификат будет доступен после\nпрохождения обучения и тестирования.",
                "Получение сертификата",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void openAbout() {
        JOptionPane.showMessageDialog(
                this,
                "CyberPractice v1.0\n\n" +
                        "Программа для обучения основам\n" +
                        "информационной безопасности.\n\n" +
                        "Модули:\n" +
                        "  • SQL Injection\n" +
                        "  • XSS (Cross-Site Scripting)\n" +
                        "  • Directory Traversal\n\n" +
                        "Используйте знания только в законных целях.",
                "О программе",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ── КАСТОМНАЯ КНОПКА ───────────────────────────────────────
    private static class MenuButton extends JButton {

        MenuButton(String title, String subtitle, Color accentColor) {
            setLayout(new BorderLayout());
            setBackground(new Color(49, 50, 68));
            setForeground(Color.WHITE);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
            titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
            titleLbl.setForeground(accentColor);

            JLabel subLbl = new JLabel(subtitle, SwingConstants.CENTER);
            subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            subLbl.setForeground(new Color(166, 173, 200));

            JPanel inner = new JPanel(new GridLayout(2, 1, 0, 6));
            inner.setOpaque(false);
            inner.setBorder(new EmptyBorder(20, 10, 20, 10));
            inner.add(titleLbl);
            inner.add(subLbl);

            add(inner, BorderLayout.CENTER);

            // Подсветка при наведении
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    setBackground(new Color(69, 71, 90));
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    setBackground(new Color(49, 50, 68));
                }
            });
        }
    }
}
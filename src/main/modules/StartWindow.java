package main.modules;

import main.Utils.CodePlayground;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class StartWindow extends JFrame {

    private final List<Vulnerability> modules;
    private final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private final int sw = screenSize.width;
    private final int sh = screenSize.height;

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

        int width  = clamp((int)(sw * 0.45), 700, 1100);
        int height = clamp((int)(sh * 0.60), 480, 850);

        setPreferredSize(new Dimension(width, height));
        setMinimumSize(new Dimension(600, 420));
        setLocation(
                (sw - width)  / 2,
                (sh - height) / 2
        );

        initComponents();
        pack();
        setVisible(true);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(30, 30, 46));

        // ── АДАПТИВНЫЕ РАЗМЕРЫ ─────────────────────────────────
        int titleFont    = clamp((int)(sh * 0.038), 22, 42);
        int subtitleFont = clamp((int)(sh * 0.013), 10, 16);
        int footerFont   = clamp((int)(sh * 0.011), 9,  13);

        int padTopHeader = clamp((int)(sh * 0.025), 14, 34);
        int padSideHeader= clamp((int)(sw * 0.018), 12, 28);

        int gapBtn       = clamp((int)(sh * 0.014), 8,  22);
        int padSideBtn   = clamp((int)(sw * 0.040), 28, 80);
        int padTopBtn    = clamp((int)(sh * 0.020), 14, 34);

        int btnTitleFont = clamp((int)(sh * 0.020), 12, 20);
        int btnSubFont   = clamp((int)(sh * 0.012), 9,  14);
        int btnPadInner  = clamp((int)(sh * 0.016), 10, 24);

        // ✅ Максимальная высота сетки кнопок
        int maxGridHeight = clamp((int)(sh * 0.35), 280, 450);

        // ── ШАПКА ──────────────────────────────────────────────
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 30, 46));
        headerPanel.setBorder(new EmptyBorder(
                padTopHeader, padSideHeader,
                padTopHeader / 2, padSideHeader
        ));

        JLabel titleLabel = new JLabel("CyberPractice", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, titleFont));
        titleLabel.setForeground(new Color(137, 180, 250));

        JLabel subtitleLabel = new JLabel(
                "Программа повышения квалификации в сфере информационной безопасности",
                SwingConstants.CENTER
        );
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, subtitleFont));
        subtitleLabel.setForeground(new Color(166, 173, 200));

        headerPanel.add(titleLabel,    BorderLayout.CENTER);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // ── КНОПКИ ─────────────────────────────────────────────
        MenuButton btnLearn = new MenuButton(
                "  Начать обучение",
                "Изучите уязвимости и способы защиты",
                new Color(64, 160, 110),
                btnTitleFont, btnSubFont, btnPadInner
        );
        MenuButton btnSandbox = new MenuButton(
                "  Песочница",
                "Пишите и запускайте Java-код",
                new Color(140, 100, 200),
                btnTitleFont, btnSubFont, btnPadInner
        );
        MenuButton btnCert = new MenuButton(
                "  Получить сертификат",
                "Завершите обучение и тест",
                new Color(180, 130, 60),
                btnTitleFont, btnSubFont, btnPadInner
        );
        MenuButton btnAbout = new MenuButton(
                "  О программе",
                "Информация о CyberPractice",
                new Color(100, 100, 140),
                btnTitleFont, btnSubFont, btnPadInner
        );

        JPanel buttonGrid = new JPanel(new GridLayout(2, 2, gapBtn, gapBtn));
        buttonGrid.setBackground(new Color(30, 30, 46));
        buttonGrid.setBorder(new EmptyBorder(
                padTopBtn, padSideBtn,
                padTopBtn / 2, padSideBtn
        ));
        // ✅ Ограничиваем максимальную высоту сетки
        buttonGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, maxGridHeight));

        buttonGrid.add(btnLearn);
        buttonGrid.add(btnSandbox);
        buttonGrid.add(btnCert);
        buttonGrid.add(btnAbout);

        // ✅ Обёртка с BoxLayout — центрирует сетку по вертикали
        JPanel buttonsWrapper = new JPanel();
        buttonsWrapper.setLayout(new BoxLayout(buttonsWrapper, BoxLayout.Y_AXIS));
        buttonsWrapper.setBackground(new Color(30, 30, 46));
        buttonsWrapper.add(Box.createVerticalGlue());
        buttonsWrapper.add(buttonGrid);
        buttonsWrapper.add(Box.createVerticalGlue());

        // ── ФУТЕР ──────────────────────────────────────────────
        JLabel footerLabel = new JLabel(
                "© 2026 CyberPractice  |  Версия 1.0",
                SwingConstants.CENTER
        );
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, footerFont));
        footerLabel.setForeground(new Color(100, 100, 120));
        footerLabel.setBorder(new EmptyBorder(
                clamp((int)(sh * 0.008), 6, 14),
                0,
                clamp((int)(sh * 0.012), 8, 18),
                0
        ));

        // ── СБОРКА ─────────────────────────────────────────────
        mainPanel.add(headerPanel,    BorderLayout.NORTH);
        mainPanel.add(buttonsWrapper, BorderLayout.CENTER); // ← обёртка
        mainPanel.add(footerLabel,    BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // ── ДЕЙСТВИЯ ───────────────────────────────────────────
        btnLearn.addActionListener(e -> openLearning());
        btnSandbox.addActionListener(e -> {
            setVisible(false);
            new CodePlayground(this);
        });
        btnCert.addActionListener(e -> openCertificate());
        btnAbout.addActionListener(e -> openAbout());
    }

    // ── ВСПОМОГАТЕЛЬНЫЙ МЕТОД ──────────────────────────────────
    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private void openLearning() {
        setVisible(false);
        new Window(modules, this);
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
                        "  • Directory Traversal\n" +
                        "  • Privilege Escalation\n" +
                        "  • Hardcoded Credentials\n" +
                        "  • Man-in-the-Middle (MITM)\n" +
                        "  • DNS Spoofing\n\n" +
                        "Используйте знания только в законных целях.",
                "О программе",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private static class MenuButton extends JButton {

        MenuButton(String title, String subtitle, Color accentColor,
                   int titleFontSize, int subFontSize, int padInner) {
            setLayout(new BorderLayout());
            setBackground(new Color(49, 50, 68));
            setForeground(Color.WHITE);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
            titleLbl.setFont(new Font("Segoe UI", Font.BOLD, titleFontSize));
            titleLbl.setForeground(accentColor);

            JLabel subLbl = new JLabel(subtitle, SwingConstants.CENTER);
            subLbl.setFont(new Font("Segoe UI", Font.PLAIN, subFontSize));
            subLbl.setForeground(new Color(166, 173, 200));

            JPanel inner = new JPanel(new GridLayout(2, 1, 0, 4));
            inner.setOpaque(false);

            inner.setBorder(new EmptyBorder(padInner, 10, padInner, 10));
            inner.add(titleLbl);
            inner.add(subLbl);

            add(inner, BorderLayout.CENTER);

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
package main.modules;

import main.Utils.CodePlayground;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class StartWindow extends JFrame {

    private final List<Vulnerability> modules;
    private final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private final int sw = screenSize.width;
    private final int sh = screenSize.height;
    private static final int [] SECRET_CODE = {
            KeyEvent.VK_SHIFT, KeyEvent.VK_G, KeyEvent.VK_A, KeyEvent.VK_Y,
    };

    private static final int [] SECRET_CODE_ANDREW = {
            KeyEvent.VK_SHIFT, KeyEvent.VK_L, KeyEvent.VK_O, KeyEvent.VK_H
    };

    private int photoIndex = 0;
    private int videoIndex = 0;
    private boolean easterActive = false;
    private javax.swing.Timer easterTimer = null;

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
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e ->{
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                checkEasterKey(e.getKeyCode());
            }
            return false;
        });
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(30, 30, 46));

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

        int maxGridHeight = clamp((int)(sh * 0.35), 280, 450);

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

        buttonGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, maxGridHeight));

        buttonGrid.add(btnLearn);
        buttonGrid.add(btnSandbox);
        buttonGrid.add(btnCert);
        buttonGrid.add(btnAbout);


        JPanel buttonsWrapper = new JPanel();
        buttonsWrapper.setLayout(new BoxLayout(buttonsWrapper, BoxLayout.Y_AXIS));
        buttonsWrapper.setBackground(new Color(30, 30, 46));
        buttonsWrapper.add(Box.createVerticalGlue());
        buttonsWrapper.add(buttonGrid);
        buttonsWrapper.add(Box.createVerticalGlue());

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


        mainPanel.add(headerPanel,    BorderLayout.NORTH);
        mainPanel.add(buttonsWrapper, BorderLayout.CENTER); // ← обёртка
        mainPanel.add(footerLabel,    BorderLayout.SOUTH);

        setContentPane(mainPanel);

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
        startEasterEggListening();
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

    private void startEasterEggListening() {
        videoIndex   = 0;
        photoIndex   = 0;
        easterActive = true;

        if (easterTimer != null) easterTimer.stop();
        easterTimer = new javax.swing.Timer(5000, e -> {
            easterActive = false;
            videoIndex   = 0;
            photoIndex   = 0;
            System.out.println("[DEV] Easter egg timeout");
        });
        easterTimer.setRepeats(false);
        easterTimer.start();

        System.out.println("[DEV] Easter egg listening started...");
    }

    private void checkEasterKey(int keyCode){
        if (!easterActive) {
            return;
        }

        if (keyCode == SECRET_CODE[videoIndex]) {
            videoIndex++;
            System.out.println("[DEV] Видео прогресс: " +  videoIndex + "/" + SECRET_CODE.length);

            if (videoIndex == SECRET_CODE.length) {
                easterActive = false;
                if (easterTimer != null) easterTimer.stop();
                videoIndex = 0;
                photoIndex = 0;
                triggerEasterEgg("video");
                return;
            }
        } else {
            videoIndex = 0;
        }

        if (keyCode == SECRET_CODE_ANDREW[photoIndex]){
            photoIndex++;
            System.out.println("[DEV] Фото прогресс: " +   photoIndex + "/" + SECRET_CODE_ANDREW.length);

            if (photoIndex == SECRET_CODE_ANDREW.length) {
                easterActive = false;
                if (easterTimer != null) easterTimer.stop();
                videoIndex = 0;
                photoIndex = 0;
                triggerEasterEgg("photo");
                return;
            }
        } else{
            photoIndex = 0;
        }
    }

    private void triggerEasterEgg(String type) {
        JDialog loading = new JDialog(this, "", false);
        loading.setUndecorated(true);
        loading.setBackground(new Color(0, 0, 0, 0));

        JLabel label = new JLabel("...", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 32));
        label.setForeground(new Color(137, 180, 250));
        label.setBorder(new EmptyBorder(20, 40, 20, 40));
        label.setOpaque(true);
        label.setBackground(new Color(30, 30, 46));

        loading.add(label);
        loading.pack();
        loading.setLocationRelativeTo(this);
        loading.setVisible(true);

        // Анимация текста
        String[] frames = {"...", "  Вы нашли пасхалку!  "};
        javax.swing.Timer anim = new javax.swing.Timer(600, null);
        int[] frame = {0};

        anim.addActionListener(e -> {
            label.setText(frames[frame[0] % frames.length]);
            frame[0]++;
            if (frame[0] > 4) {
                anim.stop();
                loading.dispose();
                if (type.equals("video")){
                    openEasterVideo();
                } else {
                    openEasterPhoto();
                }
            }
        });
        anim.start();
    }


    private void openEasterVideo() {
        tryOpenLocalVideo();
    }
    private void tryOpenLocalVideo() {
        try {
            // 1. Ищем видео ВНУТРИ нашего app.jar (в папке resources)
            InputStream is = getClass().getResourceAsStream("/easter.mp4");

            if (is != null) {
                // 2. Создаём невидимый временный файл в %TEMP%
                Path tempVideo = Files.createTempFile("cyber_easter_", ".mp4");

                // 3. Приказываем ОС удалить этот файл, когда программа закроется
                tempVideo.toFile().deleteOnExit();

                // 4. Копируем видео из JAR во временный файл
                Files.copy(is, tempVideo, StandardCopyOption.REPLACE_EXISTING);
                is.close();

                // 5. Запускаем видео
                Desktop.getDesktop().open(tempVideo.toFile());
                showEasterMessage("Сюрприз запущен! 🎬");
                return;
            } else {
                System.err.println("[DEV] Видео не найдено внутри JAR.");
            }
        } catch (Exception e) {
            System.err.println("[DEV] Ошибка извлечения видео: " + e.getMessage());
        }

    }

    private void openEasterPhoto(){tryOpenLocalPhoto();}
    private void tryOpenLocalPhoto(){
        try{
            InputStream is = getClass().getResourceAsStream("/easter_photo.jpg");

            if (is != null) {
                System.out.println("[DEV] Фото не найдено внутри JAR");
                showEasterPhotoFallback();
                return;
            }

            Path tempPhoto = Files.createTempFile("cache_img_", ".tmp");
            tempPhoto.toFile().deleteOnExit();

            try (InputStream input = is){
                Files.copy(input, tempPhoto, StandardCopyOption.REPLACE_EXISTING);
            }

            System.out.println("[DEV] Фото распаковано в: " + tempPhoto);

            Desktop.getDesktop().open(tempPhoto.toFile());
            showEasterMessage("Пасхалкооо");

        } catch (Exception e) {
            System.err.println("[DEV] Ошибка извлечения видео: " + e.getMessage());
            showEasterPhotoFallback();
        }
    }

    private void showEasterPhotoFallback() {
        try {
            InputStream is = StartWindow.class
                    .getResourceAsStream("/easter_photo.jpg");

            if (is == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Поздравляем! Вы нашли вторую пасхалку!\n\n" +
                                "Но файл фото не найден в ресурсах.",
                        "Easter Egg #2",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            BufferedImage original = ImageIO.read(is);

            BufferedImage rotated = rotateImage(original, 90);

            int maxW = clamp((int)(sw * 0.4), 300, 700);
            int maxH = clamp((int)(sh * 0.7), 400, 900);

            double scaleX = (double) maxW / rotated.getWidth();
            double scaleY = (double) maxH / rotated.getHeight();
            double scale  = Math.min(scaleX, scaleY);
            scale = Math.min(scale, 1.0);

            int newW = (int)(rotated.getWidth()  * scale);
            int newH = (int)(rotated.getHeight() * scale);

            Image scaled = rotated.getScaledInstance(
                    newW, newH, Image.SCALE_SMOOTH
            );
            ImageIcon scaledIcon = new ImageIcon(scaled);

            JDialog photoDialog = new JDialog(this, "Easter Egg #2", true);
            photoDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            photoDialog.getContentPane().setBackground(new Color(20, 20, 32));

            JPanel panel = new JPanel(new BorderLayout(0, 10));
            panel.setBackground(new Color(20, 20, 32));
            panel.setBorder(new EmptyBorder(16, 16, 16, 16));

            JLabel titleLbl = new JLabel(
                    "Поздравляем! Вы нашли вторую пасхалку!",
                    SwingConstants.CENTER
            );
            titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
            titleLbl.setForeground(new Color(137, 180, 250));

            JLabel photoLbl = new JLabel(scaledIcon, SwingConstants.CENTER);
            photoLbl.setBorder(BorderFactory.createLineBorder(
                    new Color(86, 130, 200), 2
            ));

            JButton closeBtn = new JButton("Закрыть");
            closeBtn.setBackground(new Color(86, 130, 200));
            closeBtn.setForeground(Color.WHITE);
            closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
            closeBtn.setBorderPainted(false);
            closeBtn.setFocusPainted(false);
            closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            closeBtn.addActionListener(e -> photoDialog.dispose());

            panel.add(titleLbl,  BorderLayout.NORTH);
            panel.add(photoLbl,  BorderLayout.CENTER);
            panel.add(closeBtn,  BorderLayout.SOUTH);

            photoDialog.add(panel);
            photoDialog.pack();
            photoDialog.setLocationRelativeTo(this);
            photoDialog.setVisible(true);

        } catch (Exception e) {
            System.err.println("[DEV] Ошибка: " + e.getMessage());
        }
    }

    private BufferedImage rotateImage(BufferedImage source, double degrees) {
        double radians = Math.toRadians(degrees);
        double sin     = Math.abs(Math.sin(radians));
        double cos     = Math.abs(Math.cos(radians));

        int originalW = source.getWidth();
        int originalH = source.getHeight();

        int newW = (int) Math.floor(originalW * cos + originalH * sin);
        int newH = (int) Math.floor(originalH * cos + originalW * sin);

        BufferedImage rotated = new BufferedImage(
                newW, newH, BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2d = rotated.createGraphics();
        g2d.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2d.translate(newW / 2.0, newH / 2.0);
        g2d.rotate(radians);
        g2d.translate(-originalW / 2.0, -originalH / 2.0);
        g2d.drawImage(source, 0, 0, null);
        g2d.dispose();

        return rotated;
    }

            private void showEasterMessage(String extra) {
        JOptionPane.showMessageDialog(this,
                "Пасхалка!\n\n" + extra,
                "Easter egg",
                JOptionPane.INFORMATION_MESSAGE);
    };


}
package main.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class CodeAutoComplete {
    private final JTextArea codeArea;
    private JWindow popupWindow;
    private JList<String> suggestionList;
    private DefaultListModel<String> listModel;
    private boolean isApplyingCompletion = false;


    // СЛОВАРЬ ПОДСКАЗОК
    private static final List<String> KEYWORDS = List.of(
            // Java ключевые слова
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "continue",
            "default", "do", "double", "else", "enum",
            "extends", "final", "finally", "float", "for",
            "if", "implements", "import", "instanceof", "int",
            "interface", "long", "new", "null", "package",
            "private", "protected", "public", "return", "short",
            "static", "super", "switch", "this", "throw",
            "throws", "try", "void", "volatile", "while",
            "true", "false",

            // Часто используемые классы
            "String", "System", "Math", "Object", "Integer",
            "Double", "Boolean", "ArrayList", "HashMap", "List",
            "Map", "Set", "Arrays", "Collections", "StringBuilder",
            "Scanner", "Exception", "RuntimeException",

            // Часто используемые методы/конструкции
            "System.out.println(", "System.out.print(",
            "System.err.println(",
            "public static void main(String[] args) {",
            "public class Example {",

            // Шаблоны
            "for (int i = 0; i < ; i++) {",
            "while () {",
            "if () {",
            "else {",
            "try {",
            "catch (Exception e) {",
            "finally {"
    );

    public CodeAutoComplete(JTextArea codeArea) {
        this.codeArea = codeArea;
        initPopup();
        attachListeners();
    }

    public boolean isPopupVisible(){
        return popupWindow.isVisible();
    }

    public boolean isApplyingCompletion() {
        return isApplyingCompletion;
    }

    public void applyCurrentSelection() {
        applySelection();
    }


    //  ИНИЦИАЛИЗАЦИЯ POPUP
    private void initPopup() {
        popupWindow = new JWindow();
        popupWindow.setFocusableWindowState(false);

        listModel = new DefaultListModel<>();
        suggestionList = new JList<>(listModel);
        suggestionList.setBackground(new Color(40, 42, 58));
        suggestionList.setForeground(new Color(220, 220, 235));
        suggestionList.setSelectionBackground(new Color(86, 130, 200));
        suggestionList.setSelectionForeground(Color.WHITE);
        suggestionList.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        suggestionList.setFixedCellHeight(24);
        suggestionList.setBorder(new EmptyBorder(2, 6, 2, 6));

        JScrollPane scroll = new JScrollPane(suggestionList);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(86, 130, 200), 1));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        popupWindow.add(scroll);
    }

    // ПОДКЛЮЧЕНИЕ СЛУШАТЕЛЕЙ
    private void attachListeners() {

        // Обновляем список при вводе
        codeArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) {
                if (!isApplyingCompletion){
                    SwingUtilities.invokeLater(() -> updateSuggestions());
                }
            }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) {
                if (!isApplyingCompletion){
                    SwingUtilities.invokeLater(() -> updateSuggestions());
                }
            }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        //  Клавиши управления popup
        codeArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!popupWindow.isVisible()) return;

                switch (e.getKeyCode()) {

                    // ↓ вниз по списку
                    case KeyEvent.VK_DOWN -> {
                        e.consume();
                        int next = suggestionList.getSelectedIndex() + 1;
                        if (next < listModel.getSize()) {
                            suggestionList.setSelectedIndex(next);
                            suggestionList.ensureIndexIsVisible(next);
                        }
                    }

                    // ↑ вверх по списку
                    case KeyEvent.VK_UP -> {
                        e.consume();
                        int prev = suggestionList.getSelectedIndex() - 1;
                        if (prev >= 0) {
                            suggestionList.setSelectedIndex(prev);
                            suggestionList.ensureIndexIsVisible(prev);
                        }
                    }

                    // Escape — закрыть popup
                    case KeyEvent.VK_ESCAPE -> {
                        e.consume();
                        hidePopup();
                    }
                }
            }
        });

        // Двойной клик мышью по подсказке
        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    applySelection();
                }
            }
        });

        // Скрываем popup если потерян фокус
        codeArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                SwingUtilities.invokeLater(() -> {
                    if (!suggestionList.hasFocus()) {
                        hidePopup();
                    }
                });
            }
        });
    }

    //ОБНОВЛЕНИЕ СПИСКА ПОДСКАЗОК
    private void updateSuggestions() {
        String prefix = getCurrentWord();


        if (prefix.length() < 2) {
            hidePopup();
            return;
        }

        List<String> matches = new ArrayList<>();
        String lowerPrefix = prefix.toLowerCase();

        for (String keyword : KEYWORDS) {
            if (keyword.toLowerCase().startsWith(lowerPrefix)
                    && !keyword.equals(prefix)) {
                matches.add(keyword);
            }
        }

        if (matches.isEmpty()) {
            hidePopup();
            return;
        }

        // Обновляем модель списка
        listModel.clear();
        for (String match : matches) {
            listModel.addElement(match);
        }
        suggestionList.setSelectedIndex(0);

        // Показываем popup рядом с курсором
        showPopup(matches.size());
    }

    // ПОКАЗ POPUP
    private void showPopup(int itemCount) {
        try {
            int pos = codeArea.getCaretPosition();
            Rectangle rect = codeArea.modelToView2D(pos).getBounds();
            Point screenPos = codeArea.getLocationOnScreen();

            int x = screenPos.x + rect.x;
            int y = screenPos.y + rect.y + rect.height + 2;

            // Ограничиваем высоту popup
            int visibleItems = Math.min(itemCount, 8);
            int popupHeight = visibleItems * 24 + 8;
            int popupWidth = 320;

            popupWindow.setSize(popupWidth, popupHeight);
            popupWindow.setLocation(x, y);
            popupWindow.setVisible(true);

        } catch (Exception ignored) {}
    }

    // СКРЫТИЕ POPUP
    public void hidePopup() {
        popupWindow.setVisible(false);
    }

    //ПРИМЕНЕНИЕ ВЫБРАННОЙ ПОДСКАЗКИ
    public void applySelection() {
        String selected = suggestionList.getSelectedValue();
        if (selected == null) return;

        isApplyingCompletion = true;

        try {
            String prefix = getCurrentWord();
            int pos = codeArea.getCaretPosition();
            int start = pos - prefix.length();

            codeArea.getDocument().remove(start, prefix.length());
            codeArea.getDocument().insertString(start, selected, null);

            int newPos = start + selected.length();
            if (selected.contains("()")) {
                codeArea.setCaretPosition(start + selected.indexOf("()") + 1);
            } else {
                codeArea.setCaretPosition(newPos);
            }

        } catch (Exception ignored) {
        } finally {
            isApplyingCompletion = false;
        }

        hidePopup();
    }

    // ПОЛУЧЕНИЕ ТЕКУЩЕГО СЛОВА
    private String getCurrentWord() {
        try {
            int pos = codeArea.getCaretPosition();
            String text = codeArea.getText();

            int start = pos;
            while (start > 0 && isWordChar(text.charAt(start - 1))) {
                start--;
            }

            return text.substring(start, pos);
        } catch (Exception e) {
            return "";
        }
    }

    // Символы которые считаются частью слова
    private boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '.';
    }
}

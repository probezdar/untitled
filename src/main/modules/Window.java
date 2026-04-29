package main.modules;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class Window extends JFrame{
    private final List<Vulnerability> modules;
    private DefaultListModel<String> listModel;
    private JList<String> moduleList;
    private JTextArea outputArea;


    public Window (List<Vulnerability> modules){
        this.modules = modules;
        JFrame.setDefaultLookAndFeelDecorated(true);
        setTitle("CyberPractice");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override                                                               //ЗАКРЫТИЕ ОКНА
            public void windowClosing(WindowEvent e) {
                int res = JOptionPane.showConfirmDialog(null, "Выйти из программы?");
                if (res == JOptionPane.YES_OPTION)
                    System.exit(0);
            }
        });
        double h = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        double w = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        setPreferredSize(new Dimension((int) w / 2,(int) h / 2));
        setLocation(450, 200);


        initComponents();
        pack();
        setVisible(true);
    }

    private void initComponents(){                              //РЕАЛИЗАЦИЯ ВКЛАДКИ
        listModel = new DefaultListModel<>();
        for (Vulnerability module : modules){
            listModel.addElement(module.getTitle());
        }
        moduleList = new JList<>(listModel);
        moduleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScroll = new JScrollPane(moduleList);
        listScroll.setPreferredSize(new Dimension(200,0));

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Segoe UI",Font.PLAIN,16));
        JScrollPane textScroll = new JScrollPane(outputArea);
                                                                        // ДОБАВЛЕНИЕ КНОПКИ В ОКНЕ
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnVul = new JButton("Показать уязвимый код");
        JButton btnFixed = new JButton("Показать исправленный код");
        JButton btnDesc = new JButton("Показать описание");
        JButton btnSimulate = new JButton("Симулировать угрозу");

        buttonPanel.add(btnDesc);
        buttonPanel.add(btnVul);
        buttonPanel.add(btnFixed);
        buttonPanel.add(btnSimulate);
                                                                        //ПОДВЯЗКА ФУНКЦИИ К КНОПКЕ
        btnDesc.addActionListener(e -> showDescription());
        btnVul.addActionListener(e -> showVulnerable());
        btnFixed.addActionListener(e -> showFixed());
        btnSimulate.addActionListener(e -> simulateThreat());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, textScroll);
        splitPane.setDividerLocation(200);

        add(splitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void showDescription(){                             //КНОПКА ВЫВОДА ОПИСАНИЯ
        int index = moduleList.getSelectedIndex();
        if (index == -1){
            outputArea.setText("Выберите модуль из списка.");
            return;
        }
        Vulnerability module = modules.get(index);
        outputArea.setText(module.getDescription());
        outputArea.append(captureOutput(module::getVulnerableExample));
    }

    private void showVulnerable(){                  //КНОПКА ВЫВОДА УЯЗВИМОСТИ
        int index = moduleList.getSelectedIndex();
        if (index == -1){
            outputArea.setText("Выберите модуль из списка.");
            return;
        }
        Vulnerability module = modules.get(index);
        outputArea.setText(module.getVulnerableExample());
        outputArea.append(captureOutput(module::getVulnerableExample));
    }

    private void showFixed(){                                   //КНОПКА ВЫВОДА ПРИМЕРА ИСПРАВЛЕНИЯ
        int index = moduleList.getSelectedIndex();
        if (index == -1){
            outputArea.setText("Выберите модуль из списка.");
            return;
        }
        Vulnerability module = modules.get(index);
        outputArea.setText(module.getFixedExample()+ "\n\n");
        outputArea.append(captureOutput(module::getFixedExample));
    }

    private String captureOutput(Runnable action){                      //ФУНКЦИЯ ПЕРЕХВАТА ВВОДА В КОНСОЛЬ
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.PrintStream old = System.out;
        System.setOut(new java.io.PrintStream(baos));
        action.run();
        System.setOut(old);
        return baos.toString();
    }

    private void simulateThreat(){                                          //ФУНКЦИЯ СИМУЛЯЦИИ УЯЗВИМОСТИ
        int index = moduleList.getSelectedIndex();
        if (index == -1){
            outputArea.setText("Выберите модуль.");
            return;
        }
        Vulnerability module = modules.get(index);
        module.simulate();
    }
}


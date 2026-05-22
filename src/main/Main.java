
import main.modules.*;
import main.modules.VulnerabilityPack.*;
import main.modules.Window;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        List<Vulnerability> modules = List.of(
                new SQLInjection(),
                new XssModule(),
                new DirectoryTraversal(),
                new PrivilegeEscalation(),
                new HardcodedCredentials(),
                new MITMModule()
        );

        try{
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

            Font defaultFont = new Font("Segoe UI", Font.PLAIN, 16);
            UIManager.put("Button.font", defaultFont);
            UIManager.put("Label.font", defaultFont);
            UIManager.put("TextArea.font", new Font("Monospaced", Font.PLAIN,16));
            UIManager.put("List.font",defaultFont);
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Не удалось установить LookAndFeel:" + e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка инициализации UI:" + e.getMessage());
        }
        new StartWindow(modules);
    });
}

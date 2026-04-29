
import main.modules.*;
import main.modules.VulnerabilityPack.DirectoryTraversal;
import main.modules.VulnerabilityPack.SQLInjection;
import main.modules.VulnerabilityPack.XssModule;
import main.modules.Window;

import javax.swing.*;
import java.awt.*;
import java.util.List;

void main() {
    SwingUtilities.invokeLater(() -> {
        List<Vulnerability> modules = List.of(
                new SQLInjection(),
                new XssModule(),
                new DirectoryTraversal()
        );

        try{
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

            Font defaultFont = new Font("Segoe UI", Font.PLAIN, 16);
            UIManager.put("Button font", defaultFont);
            UIManager.put("Label.font", defaultFont);
            UIManager.put("TextArea.font", new Font("Monospaced", Font.PLAIN,16));
            UIManager.put("List.font",defaultFont);
        } catch (Exception e) {e.printStackTrace();}
        new Window(modules);
    });
}

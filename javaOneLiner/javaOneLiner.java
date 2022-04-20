import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class javaOneLiner {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception err) {
            err.printStackTrace();
            System.out.println("Got an error, but you still can use it :)");
        }
        JFrame frame = new JFrame("Code Reader Killer");
        frame.setSize(360, 240);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel(null);

        JTextField fileField = new JTextField();
        JButton fileButton = new JButton("Browse");
        JLabel sureLabel = new JLabel("Make sure your code conforms to java writing habits.");
        JLabel tipsLabel = new JLabel("You need to change the name of mainclass.");
        JButton killButton = new JButton("KILL");
        JButton okButton = new JButton("OK");
        JProgressBar killProgress = new JProgressBar();
        fileButton.addActionListener(e -> {
            File choosedFile = chooseFile(frame);
            if (choosedFile.isFile()) {
                fileField.setText(choosedFile.getAbsolutePath());
            }
        });
        killButton.addActionListener(e -> {
            File inputFile;
            if (!(inputFile = new File(fileField.getText())).exists()) {
                fileField.setText("FILE NOT EXISTS!");
                return;
            }
            setComps(false, fileField, fileButton, killButton);
            setComps(true, killProgress, okButton);

            CodeKiller codekiller = new CodeKiller(inputFile, killProgress, okButton);
            codekiller.start();
        });
        okButton.addActionListener(e -> {
            setComps(true, fileField, fileButton, killButton);
            setComps(false, killProgress, okButton);
        });
        killProgress.setVisible(false);
        okButton.setVisible(false);

        panel.add(fileField);
        panel.add(fileButton);
        panel.add(sureLabel);
        panel.add(tipsLabel);
        panel.add(killButton);
        panel.add(okButton);
        panel.add(killProgress);
        fileField.setBounds(5, 10, 245, 30);
        fileButton.setBounds(252, 10, 80, 30);
        sureLabel.setBounds(5, 42, 332, 30);
        tipsLabel.setBounds(5, 60, 332, 30);
        killButton.setBounds(240, 164, 90, 35);
        okButton.setBounds(240, 164, 90, 35);
        killProgress.setBounds(5, 10, 332, 30);

        frame.setContentPane(panel);
        frame.setVisible(true);
    }

    public static class CodeKiller extends Thread {
        private File inputFile;
        private JProgressBar progressBar;
        private JButton button;

        public CodeKiller(File input, JProgressBar jprogressbar, JButton jbutton) {
            inputFile = input;
            progressBar = jprogressbar;
            button = jbutton;
        }

        @Override
        public void run() {
            button.setEnabled(false);

            File outputFile;
            outputFile = new File(inputFile.getAbsolutePath() + "-onelined.java");
            String readedString;
            ArrayList<String> readedList = new ArrayList<String>();
            int firstCharIndex, lastCharIndex;
            String editingString;
            String[] editingList;

            try (BufferedReader readFile = new BufferedReader(new FileReader(inputFile));
                    FileOutputStream writeFile = new FileOutputStream(outputFile)) {
                while ((readedString = readFile.readLine()) != null) { // without \r\n
                    readedList.add(readedString);
                }
                progressBar.setMaximum(readedList.size());
                progressBar.setValue(0);
                for (int i = 0; i < readedList.size(); i++) {
                    progressBar.setValue(progressBar.getValue() + 1);

                    editingString = readedList.get(i);
                    if (Pattern.matches(" *@.*", editingString) || Pattern.matches(" *", editingString))
                        continue;
                    editingList = editingString.split("");
                    firstCharIndex = lastCharIndex = 0;
                    for (int j = 0; j < editingList.length; j++) {
                        if (!editingList[j].equals(" ")) {
                            firstCharIndex = j;
                            break;
                        }
                    }
                    for (int j = editingList.length - 1; j >= 0; j--) {
                        if (!editingList[j].equals(" ")) {
                            lastCharIndex = j;
                            break;
                        }
                    }
                    writeFile.write(
                            pureCode(editingString.substring(firstCharIndex, lastCharIndex + 1)).getBytes());
                }
                writeFile.flush();
            } catch (Exception err) {
                err.printStackTrace();
            }
            button.setEnabled(true);
        }
    }

    public static File chooseFile(Component parent) {
        JFileChooser fileChooser = new JFileChooser(".");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Java file", "java"));

        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return new File("");
    }

    public static void setComps(boolean visible, Component... comps) {
        for (Component c : comps) {
            c.setVisible(visible);
        }
    }

    public static String pureCode(String string) {
        int commentIndex = string.length();
        String reversedString = new StringBuilder(string).reverse().toString();
        if (Pattern.matches(".*//.*", string)) {
            if (!Pattern.matches(".*(\".*//.*\").*", string)) {
                commentIndex -= reversedString.indexOf("/") + 2;
                return pureCode(string.substring(0, commentIndex));
            }
        }
        return string;
    }
}

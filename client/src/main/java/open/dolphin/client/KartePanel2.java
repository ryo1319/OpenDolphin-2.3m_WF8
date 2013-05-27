package open.dolphin.client;

import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/**
 * ２号カルテパネル
 * @author masuda, Masuda Naika
 */
public final class KartePanel2 extends KartePanel {

    private JTextPane pTextPane;
    private JTextPane soaTextPane;

    public KartePanel2(boolean editor) {
        super();
        initComponents(editor);
    }

    @Override
    protected void initComponents(boolean editor) {

        JPanel contentPanel = getContentPanel();

        if (editor) {
            contentPanel.setLayout(new GridLayout(rows, cols, hgap, vgap));
            soaTextPane = createTextPane();
            JScrollPane soaScroll = new JScrollPane(soaTextPane);
            soaScroll.setBorder(null);
            contentPanel.add(soaScroll);
            pTextPane = createTextPane();
            JScrollPane pScroll = new JScrollPane(pTextPane);
            pScroll.setBorder(null);
            contentPanel.add(pScroll);
        } else {
            contentPanel.setLayout(new GridLayout(rows, cols, hgap, vgap));
            soaTextPane = createTextPane();
            contentPanel.add(soaTextPane);
            pTextPane = createTextPane();
            contentPanel.add(pTextPane);
        }
    }

    @Override
    public JTextPane getSoaTextPane() {
        return soaTextPane;
    }

    @Override
    public JTextPane getPTextPane() {
        return pTextPane;
    }
    
    @Override
    public boolean isSinglePane() {
        return false;
    }
    
    // KarteDocumentViewerのBoxLayoutがうまくやってくれるように
    @Override
    public Dimension getPreferredSize() {

        int w = getContainerWidth();
        int h = getTimeStampPanel().getPreferredSize().height;
        h -= 15;    // some adjustment

        int hsoa = soaTextPane.getPreferredSize().height;
        int hp = pTextPane != null
                ? pTextPane.getPreferredSize().height : 0;
        h += Math.max(hp, hsoa);

        return new Dimension(w, h);
    }
}

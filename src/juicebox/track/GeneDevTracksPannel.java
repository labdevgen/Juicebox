/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2018 Broad Institute, Aiden Lab
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package juicebox.track;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
/*from   w w w. j a  v a  2 s  .c om*/
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

import java.io.IOException;

import juicebox.HiC;
import juicebox.HiCGlobals;
import juicebox.data.HiCFileTools;
import juicebox.gui.SuperAdapter;
import juicebox.windowui.layers.Load2DAnnotationsDialog;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.util.List;
import java.util.ArrayList;




public class GeneDevTracksPannel {
    public JFrame frame;
    public JList list;
    private SuperAdapter superAdapter;
    private Runnable repaint1DLayersPanel;

    private static List<String> getAvaliableTracks(String base_url) throws IOException{
        System.out.println("Retrieving files from " + base_url);
        Document doc = Jsoup.connect(base_url).get();
        //System.out.println(doc);
        List<String> result = new ArrayList<>();
        for (Element file : doc.select("td a")) {
            if (! file.text().equals("Parent Directory")) {
                String href = file.attr("href");
                if (href.endsWith("/")) { //it's a directory
                    List deeperResults = getAvaliableTracks(base_url + href);
                    result.addAll(deeperResults);
                }
                else{
                    result.add(base_url + href);
                }
            }
        }
        return (result);
    }
    public void show(SuperAdapter in_superAdapter, Runnable in_repaint1DLayersPanel) {
        superAdapter = in_superAdapter;
        repaint1DLayersPanel = in_repaint1DLayersPanel;
        List<String> urls = new ArrayList<>();
        try {
            urls = getAvaliableTracks("http://genedev.bionet.nsc.ru/site/hic_out/Anopheles/");
        }
        catch (IOException e){
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Cannot connect to server. Please check connecion or try again later");
        }
        frame = new JFrame();
        java.awt.Container pane = frame.getContentPane();
        pane.setLayout(new BoxLayout(pane,BoxLayout.Y_AXIS));
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DefaultListModel model = new DefaultListModel();
        list = new JList(model);
        for (String i: urls) {
            model.addElement(i);
        }
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        pane.add(new JScrollPane(list));
        frame.pack();

        JPanel buttonPannel = new JPanel();
        buttonPannel.setLayout(new GridLayout());
        pane.add(buttonPannel);

        JButton bnAddTracks = new JButton("Add tracks");
        buttonPannel.add(bnAddTracks);
        bnAddTracks.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bnAddTrackClick(e);
            }
        });

        JButton bnFilterByGenome = new JButton("Filter by genome");
        buttonPannel.add(bnFilterByGenome);
        bnFilterByGenome.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String genomeID = superAdapter.getHiC().getDataset().getGenomeId();
                String[] parts = genomeID.split("/");
                System.out.println(parts);
                String last = parts[parts.length - 1];
                if (last.length() >= 5) last = last.substring(0,4);
                System.out.println("Using genomeID " + last);
                List indexes = new ArrayList<>();
                DefaultListModel dlm = (DefaultListModel) list.getModel();
                for (Integer i=list.getModel().getSize() - 1; i>=0; i--){
                    String s =  list.getModel().getElementAt(i).toString();
                    if (!s.toLowerCase().contains(last.toLowerCase()))
                        dlm.removeElementAt(i);
                }
            }
        });

        frame.setVisible(true);
    }


    private void LoadGeneDev1Durl(final String input_url, final Runnable refresh1DLayers) {
        // code based on safeLoadFromURLActionPerformed from superadapter
        final HiC hic = superAdapter.getHiC();
        Runnable runnable = new Runnable() {
            public void run() {
                System.out.println("In");
                if (hic.getDataset() == null) {
                    JOptionPane.showMessageDialog(superAdapter.getMainWindow(),
                            "HiC file must be loaded to load tracks", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String url = input_url;
                System.out.println(input_url);
                if (url != null && url.length() > 0) {
                    if (HiCFileTools.isDropboxURL(url)) {
                        url = HiCFileTools.cleanUpDropboxURL(url);
                    }
                    url = url.trim();
                    hic.unsafeLoadTrack(url);
                }
                refresh1DLayers.run();
            }
        };
        superAdapter.getMainWindow().executeLongRunningTask(runnable, "Load from url");
    }

    private void LoadGeneDev2Durl(final String input_url) {

        Load2DAnnotationsDialog annotationsDialog = superAdapter.getLayersPanel().getLoad2DAnnotationsDialog();
        if (annotationsDialog == null) {
            annotationsDialog = new Load2DAnnotationsDialog(superAdapter.getLayersPanel(),
                    superAdapter);
            superAdapter.getLayersPanel().setLoad2DAnnotationsDialog(annotationsDialog);
        }
        System.out.println("In");
        annotationsDialog.loadFromUrl(input_url);
        //annotationsDialog.setVisible(Boolean.TRUE);
    }


    private void bnAddTrackClick(ActionEvent e){
        java.util.List<String> sval= list.getSelectedValuesList();
        for (String track: sval) {
            if (track.endsWith(".bed") || track.toLowerCase().endsWith(".bedgraph")){
                System.out.println("Adding file " + track);
                LoadGeneDev1Durl(track,repaint1DLayersPanel);
            }
            if (track.endsWith(".2D.ann")) {
                System.out.println("Adding file " + track);
                LoadGeneDev2Durl(track);
            }
        }
        javax.swing.JOptionPane.showMessageDialog(null,"Done!");
        System.out.println(sval);
    }
}


package br.gov.mt.mti.deploy;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ApplicationDeploy extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3086687418102161061L;
	
	JButton gerarButton = new JButton("Gerar");
	JTextField tfNewBranchName = new JTextField();
	JTextArea textAreaArtifacts = new JTextArea();
	JTextArea textAreaRepoLocation = new JTextArea();
	JTextArea output = new JTextArea();
	
	public ApplicationDeploy () {
		initUI();
	}
	
	private void initUI(){
		setTitle("MTI - Preparar Homologa");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        gerarButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent event) {
				
			}
        	
        });
        
        BoxLayout gl = new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS);
        getContentPane().setLayout(gl);
        
        output.setEditable(false);
        output.setColumns(120);
        output.setRows(100);
        
        textAreaArtifacts.setColumns(120);
        textAreaArtifacts.setRows(100);
        addComponent(new JLabel("Local do Repositório"));
        addComponent(textAreaRepoLocation);
        addComponent(new JLabel("Branch Name"));
        addComponent(tfNewBranchName);
        addComponent(new JLabel("Artefatos"));
        addComponent(textAreaArtifacts);
        addComponent(output);
        
        gerarButton.addActionListener((ActionEvent event) -> {
            System.exit(0);
        });
        
        addComponent(gerarButton);
	}
	
	protected void addComponent(JComponent comp){
		getContentPane().add(comp);
	}
	
	public static void main(String[] args){
		EventQueue.invokeLater(() -> {
			ApplicationDeploy ex = new ApplicationDeploy();
            ex.setVisible(true);
        });
	}
}

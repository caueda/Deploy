package br.gov.mt.mti.deploy;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import edu.nyu.cs.javagit.api.DotGit;
import edu.nyu.cs.javagit.api.JavaGitConfiguration;
import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.Ref;

public class ApplicationDeploy extends JFrame {
	
	/**
	 * 
	 * 
	 */
	private static final long serialVersionUID = -3086687418102161061L;
	
	JButton gerarButton = new JButton("Gerar");
	JTextField tfNewBranchName = new JTextField();
	JTextArea artifacts = new JTextArea();
	JScrollPane scrollArtifacts = new JScrollPane(artifacts);
	JTextField textAreaRepoLocation = new JTextField();
	JTextField lastCommit = new JTextField();
	JTextArea output = new JTextArea();
	JScrollPane scrollOutput = new JScrollPane(output);
	
	public ApplicationDeploy () {
		initUI();
	}
	
	private void initUI(){
		setTitle("MTI - Preparar Homologa");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        gerarButton.addActionListener((ActionEvent event) -> {
            //System.exit(0);
        	File repositoryDirectory = new File(textAreaRepoLocation.getText());
        	DotGit doGit = DotGit.getInstance(repositoryDirectory);
			
			try {
				output.setText("");
				output.setText(JavaGitConfiguration.getGitVersion() + "\n");
			} catch (Exception e) {
				e.printStackTrace();
			}
        });
        
        BoxLayout gl = new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS);
        getContentPane().setLayout(gl);
        
        output.setColumns(120);
        output.setRows(100);
        output.setWrapStyleWord(true);
        output.setLineWrap(true);
        
        artifacts.setColumns(120);
        artifacts.setRows(100);
        artifacts.setWrapStyleWord(true);
        artifacts.setLineWrap(true);
        
        addComponent(new JLabel("Local do Repositório"));
        addComponent(textAreaRepoLocation);
        addComponent(new JLabel("Branch Name"));
        addComponent(tfNewBranchName);
        addComponent(new JLabel("Commit"));
        addComponent(lastCommit);
        addComponent(new JLabel("Artefatos"));
        addComponent(scrollArtifacts);
        addComponent(new JLabel("Console"));
        addComponent(scrollOutput);
        
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

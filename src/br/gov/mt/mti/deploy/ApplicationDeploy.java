package br.gov.mt.mti.deploy;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class ApplicationDeploy extends JFrame {
	
	/**
	 * 
	 * 
	 */
	private static final long serialVersionUID = -3086687418102161061L;
	
	JButton gerarButton = new JButton("Gerar");
	JButton repoButton = new JButton("Repositório");
	JTextField tfBranchDestino = new JTextField();
	JTextArea taArtifacts = new JTextArea();
	JScrollPane scrollArtifacts = new JScrollPane(taArtifacts);
	JTextField tfRepoLocation = new JTextField();
	JTextField tfCommit = new JTextField();
	JFileChooser fileChooser = new JFileChooser();
	
	public ApplicationDeploy () {
		initUI();
	}
	
	protected static boolean isStringEmpty(String value){
		return (value == null || value.isEmpty());
	}
	
	protected static boolean isNotStringEmpty(String value){
		return ! isStringEmpty(value);
	}
	
	private void initUI(){
		setTitle("MTI - Preparar Homologa");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        
        repoButton.addActionListener((ActionEvent event) -> {
        	int returnVal = fileChooser.showOpenDialog(this);
        	if (returnVal == JFileChooser.APPROVE_OPTION) {
                tfRepoLocation.setText(fileChooser.getSelectedFile().getAbsolutePath() + 
                		File.separator + ".git");
                //This is where a real application would open the file.
            } 
        });
        
        gerarButton.addActionListener((ActionEvent event) -> {
            //System.exit(0);
        	
        	Repository repo = null;
        	try {
				if(isStringEmpty(tfRepoLocation.getText())){
					JOptionPane.showMessageDialog(this, "O Endereço do Repositório Local deve ser informado.","Aviso",JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				try {
					repo = new FileRepositoryBuilder()
						    .setGitDir(new File(tfRepoLocation.getText()))
						    .build();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, "Não foi possível localizar o repositório local.","Erro",JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				Git git = new Git(repo);
				ObjectId commitId = ObjectId.fromString(tfCommit.getText());
				if(isStringEmpty(tfCommit.getText())){
					JOptionPane.showMessageDialog(this, "O Commit deve ser informado.","Aviso",JOptionPane.WARNING_MESSAGE);
					return;
				}
				RevWalk revWalk = new RevWalk( repo );
				RevCommit commit = revWalk.parseCommit( commitId );
				
				if(isStringEmpty(tfBranchDestino.getText())){
					JOptionPane.showMessageDialog(this, "O Nome do Branch de Origem deve ser informado.","Aviso",JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				if(isStringEmpty(taArtifacts.getText())){
					JOptionPane.showMessageDialog(this, "Os artefato(s) deve(m) ser informado(s).","Aviso",JOptionPane.WARNING_MESSAGE);
					return;
				}
				String[] artefatos = taArtifacts.getText().split("\\n");
				
				git.checkout().setName(tfBranchDestino.getText()).call();
				
				for(String artefato : artefatos){		
					git.checkout()
					   .setStartPoint(commit)
					   .addPath(artefato).call();
				}
				
				JOptionPane.showMessageDialog(this, "Operação executada com sucesso", "Artefatos preparados", JOptionPane.INFORMATION_MESSAGE);
				
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Erro na operação", JOptionPane.INFORMATION_MESSAGE);
			} finally {
				try {
					if(repo != null){
						repo.close();
					}
				} catch(Exception e){}
			}
        });
        
        BoxLayout gl = new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS);
        getContentPane().setLayout(gl);
        
        taArtifacts.setColumns(120);
        taArtifacts.setRows(100);
        taArtifacts.setWrapStyleWord(true);
        taArtifacts.setLineWrap(true);
        
        addComponent(repoButton);
        tfRepoLocation.setEditable(false);
        addComponent(tfRepoLocation);
        addComponent(new JLabel("Branch Destino"));
        addComponent(tfBranchDestino);
        addComponent(new JLabel("Commit"));
        addComponent(tfCommit);
        addComponent(new JLabel("Artefatos"));
        addComponent(scrollArtifacts);
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

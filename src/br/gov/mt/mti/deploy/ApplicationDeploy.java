package br.gov.mt.mti.deploy;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
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
	JTextField tfBranchOrigem = new JTextField();
	JTextField tfBranchDestino = new JTextField();
	JTextArea taArtifacts = new JTextArea();
	JScrollPane scrollArtifacts = new JScrollPane(taArtifacts);
	JTextField tfRepoLocation = new JTextField();
	JTextField tfCommit = new JTextField();
	JTextArea output = new JTextArea();
	JScrollPane scrollOutput = new JScrollPane(output);
	
	public ApplicationDeploy () {
		initUI();
	}
	
	private static boolean isStringEmpty(String value){
		return (value == null || value.isEmpty());
	}
	
	private static boolean isNotStringEmpty(String value){
		return ! isStringEmpty(value);
	}
	
	private void initUI(){
		setTitle("MTI - Preparar Homologa");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        gerarButton.addActionListener((ActionEvent event) -> {
            //System.exit(0);
        	
        	try {
				if(isStringEmpty(tfRepoLocation.getText())){
					JOptionPane.showMessageDialog(this, "O Endereço do Repositório Local deve ser informado.","Aviso",JOptionPane.WARNING_MESSAGE);
					return;
				}
				Repository repo = null;
				
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
				CheckoutCommand checkout = git.checkout();
				
				checkout.setName(tfBranchOrigem.getText());
				
				if(isStringEmpty(tfBranchOrigem.getText())){
					JOptionPane.showMessageDialog(this, "O Nome do Branch de Origem deve ser informado.","Aviso",JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				if(isStringEmpty(tfBranchDestino.getText())){
					JOptionPane.showMessageDialog(this, "O Nome do Branch de Origem deve ser informado.","Aviso",JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				if(isStringEmpty(taArtifacts.getText())){
					JOptionPane.showMessageDialog(this, "Os artefato(s) deve(m) ser informado(s).","Aviso",JOptionPane.WARNING_MESSAGE);
					return;
				}
				String[] artefatos = taArtifacts.getText().split("/n");
				
				checkout.setStartPoint(commit);
				checkout.setCreateBranch(true);
				checkout.call();
				
				
				CheckoutCommand checkoutBranchDestino = git.checkout();
				checkoutBranchDestino.setName(tfBranchDestino.getText());
				checkoutBranchDestino.call();
				
				checkoutBranchDestino.setName(tfBranchOrigem.getText());
				for(String artefato : artefatos){
					checkoutBranchDestino.addPath(artefato);
				}
				checkoutBranchDestino.call();
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (RefAlreadyExistsException e) {
				e.printStackTrace();
			} catch (RefNotFoundException e) {
				e.printStackTrace();
			} catch (InvalidRefNameException e) {
				e.printStackTrace();
			} catch (CheckoutConflictException e) {
				e.printStackTrace();
			} catch (GitAPIException e) {
				e.printStackTrace();
			}
        });
        
        BoxLayout gl = new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS);
        getContentPane().setLayout(gl);
        
        output.setColumns(120);
        output.setRows(100);
        output.setWrapStyleWord(true);
        output.setLineWrap(true);
        
        taArtifacts.setColumns(120);
        taArtifacts.setRows(100);
        taArtifacts.setWrapStyleWord(true);
        taArtifacts.setLineWrap(true);
        
        addComponent(new JLabel("Local do Repositório"));
        addComponent(tfRepoLocation);
        addComponent(new JLabel("Branch Origem"));
        addComponent(tfBranchOrigem);
        addComponent(new JLabel("Branch Destino"));
        addComponent(tfBranchDestino);
        addComponent(new JLabel("Commit"));
        addComponent(tfCommit);
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

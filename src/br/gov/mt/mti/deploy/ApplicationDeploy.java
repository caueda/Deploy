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
        	
        	try {
				Repository repo = new FileRepositoryBuilder()
					    .setGitDir(new File("c:/Java/workspace/Angular2/QuickStart/angular-grrecurso/.git"))
					    .build();
				Git git = new Git(repo);
				ObjectId commitId = ObjectId.fromString("e46a4b067248fffe5a72e243e6f5073d43722518");
				RevWalk revWalk = new RevWalk( repo );
				RevCommit commit = revWalk.parseCommit( commitId );
				CheckoutCommand checkout = git.checkout();
				checkout.setName("Temporario");
				checkout.setStartPoint(commit);
				checkout.setCreateBranch(true);
				checkout.call();
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

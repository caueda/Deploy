package br.gov.mt.mti.deploy;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
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
	
	private static final String KEY_EXCLUDE = "exclude";
	private static final String KEY_REPO_LOCATION = "repo.location";
	
	JButton gerarButton = new JButton("Gerar");
	JButton repoButton = new JButton("Repositório");
	JComboBox<String> jcBranchDestino = new JComboBox<String>();
	JTextArea taArtifacts = new JTextArea();
	JScrollPane scrollArtifacts = new JScrollPane(taArtifacts);
	JTextField tfRepoLocation = new JTextField();
	JTextField tfCommit = new JTextField();
	JFileChooser fileChooser = new JFileChooser();
	FileInputStream fis = null;
	Repository repo = null;
	
	public ApplicationDeploy () throws Exception {
		initUI();
	}
	
	protected static boolean isStringEmpty(String value){
		return (value == null || value.isEmpty());
	}
	
	protected static boolean isNotStringEmpty(String value){
		return ! isStringEmpty(value);
	}
	
	protected List<String> listBranchs(Repository repository) throws GitAPIException{
		List<Ref> call = new Git(repository).branchList().call();
		List<String> lista = new ArrayList<String>();
		for (Ref ref : call) {
			String branchName = null;
			int lastIndexOf = ref.getName().lastIndexOf("/");
			branchName = ref.getName().substring(lastIndexOf + 1);
		    lista.add(branchName);
		}
		return lista;
	}
	
	private void initUI() throws Exception {
		
		String path = System.getProperty("user.dir");
		
		Properties prop = new Properties();
		
		fis = new FileInputStream(path + File.separator + "predeploy.properties");
		
		prop.load(fis);
		
		setTitle("MTI - Preparar Homologa");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);
        
        if(prop.get(KEY_REPO_LOCATION) != null && !prop.get(KEY_REPO_LOCATION).toString().isEmpty()){        	
        	String repoLocation = prop.get(KEY_REPO_LOCATION).toString();
        	if(repoLocation.contains("/.git")) {
        		File repoFolder = new File(repoLocation.replace("/.git",""));
        		if(!repoFolder.exists()) {
        			JOptionPane.showMessageDialog(this, "O repositório indicado no arquivo de properties não existe.", "Erro", JOptionPane.ERROR_MESSAGE);
        		} else {
        			tfRepoLocation.setText(repoLocation);
        			initRepository();		
        		}
        	}			
		} 
        
        repoButton.addActionListener((ActionEvent event) -> {
        	int returnVal = fileChooser.showOpenDialog(this);
        	if (returnVal == JFileChooser.APPROVE_OPTION) {
                tfRepoLocation.setText(fileChooser.getSelectedFile().getAbsolutePath() + 
                		File.separator + ".git");
                //This is where a real application would open the file.
                initRepository();
            } 
        });
        
        gerarButton.addActionListener((ActionEvent event) -> {
            //System.exit(0);
        	
        	try {
				if(isStringEmpty(tfRepoLocation.getText())){
					JOptionPane.showMessageDialog(this, "O Endereço do Repositório Local deve ser informado.","Aviso",JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				try {
					repo = new FileRepositoryBuilder()
						    .setGitDir(new File(tfRepoLocation.getText()))
						    .build();
					listBranchs(repo);
					
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
				
				if(jcBranchDestino.getSelectedItem() == null){
					JOptionPane.showMessageDialog(this, "O Nome do Branch de Destino deve ser informado.","Aviso",JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				if(isStringEmpty(taArtifacts.getText())){
					JOptionPane.showMessageDialog(this, "Os artefato(s) deve(m) ser informado(s).","Aviso",JOptionPane.WARNING_MESSAGE);
					return;
				}
				String[] artefatos = taArtifacts.getText().split("\\n");
				
				git.checkout().setName(jcBranchDestino.getSelectedItem().toString()).call();
				
				String[] excluidos = (prop.getProperty(KEY_EXCLUDE) != null) ? (prop.getProperty(KEY_EXCLUDE).split(",")) : new String[0];
				
				for(String artefato : artefatos){	
					if(artefato != null && !artefato.isEmpty()){
						if(!contains(excluidos, artefato)){
							git.checkout()
							   .setStartPoint(commit)
							   .addPath(artefato).call();
						}
					}
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
				try {
					if(fis != null) fis.close();
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
        addComponent(jcBranchDestino);
        addComponent(new JLabel("Commit"));
        addComponent(tfCommit);
        addComponent(new JLabel("Artefatos"));
        addComponent(scrollArtifacts);
        addComponent(gerarButton);
	}

	private void initRepository() {
		try {
			repo = new FileRepositoryBuilder()
				    .setGitDir(new File(tfRepoLocation.getText()))
				    .build();
			listBranchs(repo);
			DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>( listBranchs(repo).toArray(new String[]{}) );
			jcBranchDestino.setModel(model);	
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Não foi possível localizar o repositório local.","Erro",JOptionPane.ERROR_MESSAGE);
			return;
		}
	}
	
	private boolean contains(String[] excluidos, String artefato){
		if(excluidos == null | excluidos.length == 0) return false;
		if(artefato == null || artefato.isEmpty()) return false;
		for(String excluir : excluidos){
			if(artefato.contains(excluir)) 
				return true;
		}
		return false;
	}
	
	protected void addComponent(JComponent comp){
		getContentPane().add(comp);
	}
	
	public static void main(String[] args){
		EventQueue.invokeLater(() -> {
			ApplicationDeploy ex;
			try {
				ex = new ApplicationDeploy();
				ex.setVisible(true);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Erro ao inicializar aplicação: " + e.getMessage() + "\nVerifique se o arquivo predeploy.properties está presente nas pasta contendo o arquivo *.jar", "Erro", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
        });
	}
}

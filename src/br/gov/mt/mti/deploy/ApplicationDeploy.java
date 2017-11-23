package br.gov.mt.mti.deploy;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
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
	
	JMenuBar menuBar = new JMenuBar();
	JButton gerarButton = new JButton("Gerar");
	JComboBox<String> jcBranchDestino = new JComboBox<String>();
	JTextArea taArtifacts = new JTextArea();
	JScrollPane scrollArtifacts = new JScrollPane(taArtifacts);
	JTextField tfRepoLocation = new JTextField();
	JTextField tfCommit = new JTextField();	
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
		
		setTitle("MTI - Pre Deploy");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setJMenuBar(menuBar);        
        setLocationRelativeTo(null);
        
        if(prop.get(KEY_REPO_LOCATION) != null && !prop.get(KEY_REPO_LOCATION).toString().isEmpty()){        	
        	String repoLocation = prop.get(KEY_REPO_LOCATION).toString();
        	if(repoLocation.contains("/.git")) {
        		File repoFolder = new File(repoLocation.replace("/.git",""));
        		if(!repoFolder.exists()) {
        			JOptionPane.showMessageDialog(this, "O reposit�rio indicado no arquivo de properties n�o existe.", "Erro", JOptionPane.ERROR_MESSAGE);
        		} else {
        			tfRepoLocation.setText(repoLocation);
        			initRepository();		
        		}
        	}			
		} 
        
        gerarButton.addActionListener((ActionEvent event) -> {
        	try {
				if(isStringEmpty(tfRepoLocation.getText())){
					JOptionPane.showMessageDialog(this, "O Endere�o do Reposit�rio Local deve ser informado.","Aviso",JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				try {
					repo = new FileRepositoryBuilder()
						    .setGitDir(new File(tfRepoLocation.getText()))
						    .build();
					listBranchs(repo);
					
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, "N�o foi poss�vel localizar o reposit�rio local.","Erro",JOptionPane.ERROR_MESSAGE);
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
					if(artefato != null && !artefato.trim().isEmpty()){
						if(!contains(excluidos, artefato)){		
							BlameResult result = git.blame().setStartCommit(commit).setFilePath(artefato).call();
							if(result == null) {
								int opcao = 
										JOptionPane.showConfirmDialog(this, "O arquivo " + artefato + " n�o existe na origem. Deseja remov�-lo do branch de destino ?", "Confirmar exclus�o ?", JOptionPane.CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
								if(opcao == JOptionPane.OK_OPTION) {
									File fileArtefatoRemover = new File(repo.getDirectory().getParent(), artefato);
									if(fileArtefatoRemover.exists()) {
										fileArtefatoRemover.delete();
									}
								}								
							}
							git.checkout()
							   .setStartPoint(commit)
							   .addPath(artefato).call();
						}
					}
				}
				
				JOptionPane.showMessageDialog(this, "Opera��o executada com sucesso", "Artefatos preparados", JOptionPane.INFORMATION_MESSAGE);
				
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Erro na opera��o", JOptionPane.INFORMATION_MESSAGE);
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
        
        JMenuItem menuItemRepo = new JMenuItem("Reposit�rio");
        menuItemRepo.setBorder(new javax.swing.border.EtchedBorder());
        menuItemRepo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Runnable runnable = new Runnable() {

					@Override
					public void run() {
						JFileChooser fileChooser = new JFileChooser();
				        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				        fileChooser.setMultiSelectionEnabled(false);
				        
				        int returnVal = fileChooser.showDialog(getOwner(), "Abrir");
			        	if (returnVal == JFileChooser.APPROVE_OPTION) {
			        		File dotGit = new File(fileChooser.getSelectedFile(),".git");
			        		if(!dotGit.exists()) {
			        			JOptionPane.showMessageDialog(getOwner(), "A pasta informada n�o � um reposit�rio do GIT.", "Erro", JOptionPane.ERROR_MESSAGE);
			        		} else {
				                tfRepoLocation.setText(fileChooser.getSelectedFile().getAbsolutePath() + 
				                		File.separator + ".git");
				                initRepository();
			        		}
			            } 
					}
				};
				SwingUtilities.invokeLater(runnable);
			}});
        
        JMenuItem menuItem = new JMenuItem("Sobre");     
        menuItem.setBorder(new javax.swing.border.EtchedBorder());
        menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Runnable runnable = new Runnable() {

					@Override
					public void run() {
						JDialog dialogSobre = new JDialog(getOwner(),"Sobre");
						dialogSobre.setSize(300, 250);
						dialogSobre.setLocationRelativeTo(getOwner());
						dialogSobre.setModal(true);
						dialogSobre.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
						JTextPane textPaneSobre = new JTextPane();
						textPaneSobre.setBorder(new javax.swing.border.EtchedBorder());
						textPaneSobre.setContentType("text/html");
						textPaneSobre.setEditable(false);
						textPaneSobre.setText("<html>"
								+ "<head>"	
								+ "</head>"
								+ "<h1 style='text-align:center'>Pr� Deploy</h1>"
								+ "<p>Este programa realiza o checkout de artefatos espec�ficos <br>de um commit para um branch.</p>"
								+ "<p><b>Informa��es</b></p>"
								+ "<ul>"
								+ "<li><b>Data da release:</b> 23/11/2017</li>"
								+ "<li><b>vers�o:</b> v1.0.1.</li>"
								+ "</ul>"
								+ "</html>");
						dialogSobre.setLayout(new BorderLayout());
						dialogSobre.add(textPaneSobre);
						dialogSobre.setVisible(true);
					}
				};
				SwingUtilities.invokeLater(runnable);
		}});
        
        
        
        menuBar.add(menuItemRepo);
        menuBar.add(menuItem);
        menuBar.setLayout(new FlowLayout());
        
        taArtifacts.setColumns(120);
        taArtifacts.setRows(100);
        taArtifacts.setWrapStyleWord(true);
        taArtifacts.setLineWrap(true);
        
//        addComponent(repoButton);
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
			JOptionPane.showMessageDialog(this, "N�o foi poss�vel localizar o reposit�rio local.","Erro",JOptionPane.ERROR_MESSAGE);
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
			try {
				ApplicationDeploy ex = new ApplicationDeploy();
				ex.setVisible(true);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Erro ao inicializar aplica��o: " + e.getMessage() + "\nVerifique se o arquivo predeploy.properties est� presente nas pasta contendo o arquivo *.jar", "Erro", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
        });
	}
}

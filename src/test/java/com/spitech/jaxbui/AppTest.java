package com.spitech.jaxbui;

import generated.project.Project;
import generated.project.Scenario;
import generated.project.model.node.ScenarioNode;
import generated.project.view.editor.EditorMap;

import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JToolBar;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jdesktop.application.Action;
import org.jdesktop.application.SingleFrameApplication;

import com.spitech.uiskeleton.model.MainModel;
import com.spitech.uiskeleton.model.TreeModelFactory;
import com.spitech.uiskeleton.view.MainPageView;

/**
 * Unit test for simple App.
 */
public class AppTest extends SingleFrameApplication implements TreeModelFactory
{
	final private MainModel mainModel = new MainModel();
	private boolean scenarioEnabled;
	
	// ---------------------------------------------------------------- Actions Behaviour
	@Action
	 public void open() {
		try {
			JFileChooser fc = new JFileChooser(".");
			int returnVal = fc.showOpenDialog(getMainFrame());

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fc.getSelectedFile();
	            //This is where a real application would open the file.
	            System.out.println("Opening: " + file.getName() + ".");
				try {
					JAXBContext context = JAXBContext.newInstance("generated.project");
					Unmarshaller u = context.createUnmarshaller();
					Object o =  u.unmarshal(file);
					
					mainModel.setUserObject(o);
					setScenarioEnabled(true);
				} catch (JAXBException e1) {
					e1.printStackTrace();
				}
	        } else {
	        	 System.out.println("Open command cancelled by user.");
	        }
	        
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Action
	public void save() {
		try {
			JFileChooser fc = new JFileChooser(".");
			int returnVal = fc.showSaveDialog(getMainFrame());
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				//This is where a real application would open the file.
				System.out.println("Saving: " + file.getName() + ".");
				try {
					JAXBContext context = JAXBContext.newInstance("generated.project");
					Marshaller m = context.createMarshaller();
					m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
					m.marshal(mainModel.getUserObject(), new FileOutputStream(file));
					
				} catch (JAXBException e1) {
					e1.printStackTrace();
				}
			} else {
				System.out.println("Save command cancelled by user.");
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Action
	public void print() {
		
	}
	
	@Action(name = "new")
	public void newProject() {
		mainModel.setUserObject(new Project());
		setScenarioEnabled(true);
	}
	
	@Action(enabledProperty = "scenarioEnabled")
	public void addScenario() {
		Scenario scenario = new Scenario();
		((Project)mainModel.getUserObject()).getScenario().add(scenario);
		RootNode rootNode = (RootNode)mainModel.getNavigationModel().getNavigationTreeModel().getRoot();
		rootNode.add(new ScenarioNode(rootNode, scenario));
		((DefaultTreeModel)mainModel.getNavigationModel().getNavigationTreeModel()).nodeStructureChanged(rootNode);
	}
	
	public boolean isScenarioEnabled() {
		return scenarioEnabled;
	}

	public void setScenarioEnabled(boolean scenarioEnabled) {
		boolean oldValue = isScenarioEnabled();
		this.scenarioEnabled = scenarioEnabled;
		firePropertyChange("scenarioEnabled", oldValue, scenarioEnabled);
	}

	// ---------------------------------------------------------------- TreeModelFactory Implementation
	@Override
	public TreeModel createTreeModel(Object o) {
		if (!(o instanceof Project)) {
			throw new IllegalArgumentException("should be an instance of " + generated.po.Project.class);
		}
		RootNode root = new RootNode((Project)o);
		final DefaultTreeModel defaultTreeModel = new DefaultTreeModel(root);
		return defaultTreeModel;
	}
    
	// ---------------------------------------------------------------- Application Implementation
	@Override
	protected void startup() {
		mainModel.getNavigationModel().setTreeModelFactory(this);
		MainPageView mainPageView = new MainPageView(this, mainModel);
		mainPageView.setEditors(EditorMap.getInstance().getEditors());
		JComponent panel = mainPageView.getPanel();
		JToolBar toolBar = mainPageView.getToolBar();
		toolBar.add(getContext().getActionMap().get("addScenario"));
		show(panel);
	}
	
    public static void main( String[] args )
    {
    	launch(AppTest.class, args);
    }
    
    
}

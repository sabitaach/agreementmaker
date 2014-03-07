package am.va.graph;

import java.util.ArrayList;
import javax.swing.SwingUtilities;
import am.va.graph.VAVariables.ontologyType;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;

//import ensemble.Ensemble2;

/**
 * The main panel of the system
 * 
 * @author Yiting
 * 
 */
public class VAPanel {

	private JFXPanel fxPanel;
	private ListView<String> listViewLeft;
	private TreeView<String> listTreeLeft;
	private Group root;
	private Scene myScene;

	static double screenWidth;
	static double screenHeight;

	private Button btnRoot;
	private Button btnUp;
	private Button btnUFB;
	private ToggleButton btnPages[] = new ToggleButton[7];
	// private ChoiceBox<String> cbOntology;
	private RadioButton rbClass;
	private RadioButton rbProperity;
	private Label lblSource1;
	private Label lblTarget1;
	private VASearchBox searchBox;
	private Tooltip pieTooltip;

	private VAPieChart chartLeft1;
	private VAPieChart chartRight1;

	private int stop = -1;

	private VAPanelLogic val;

	public VAPanel(VAPanelLogic v) {
		this.val = v;
	}

	public VAPanelLogic getVal() {
		return val;
	}

	public void setVal(VAPanelLogic val) {
		this.val = val;
	}

	/**
	 * Start showing the panel on the tab
	 */
	public void initButNotShow() {
		fxPanel = new VATab();
		screenWidth = Screen.getPrimary().getVisualBounds().getWidth() - 20;
		screenHeight = Screen.getPrimary().getVisualBounds().getHeight() - 30;
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				InitFX();
			}
		});
	}

	public JFXPanel getFxPanel() {
		return fxPanel;
	}

	/**
	 * Init JavaFx panel, add mouse click Event handler
	 */
	public void InitFX() {
		root = new Group();
		myScene = new Scene(root);
		String sceneCss = VAPanel.class.getResource("VA.css").toExternalForm();
		myScene.getStylesheets().add(sceneCss);
		setLayout();
		fxPanel.setScene(myScene);
		val.updatePreviousGroup(val.getRootGroupLeft1());
		val.updateCurrentGroup(val.getRootGroupLeft1());
		// setUpButton(val.getRootGroupLeft1());
		chartLeft1.updatePieChart(ontologyType.Source);
		generateNewTree();
	}

	// =====================Graphic User Interface========================

	/**
	 * Set the main panel layout, here we use BorderPane
	 */
	private void setLayout() {
		BorderPane borderPane = new BorderPane();
		borderPane.setPrefSize(screenWidth, screenHeight);
		initCenterSplitPanel(borderPane); // pie chart & lists panel
		initRightFlowPane(borderPane); // preview panel
		initTopToolbar(borderPane); // tool bar panel

		root.getChildren().add(borderPane);
	}

	/**
	 * Init center panel, including two lists, two sets of pie charts
	 * 
	 * @param borderPane
	 */
	private void initCenterSplitPanel(BorderPane borderPane) {
		SplitPane splitPane = new SplitPane();
		splitPane.setOrientation(Orientation.HORIZONTAL);
		splitPane.setDividerPosition(0, 0.2);
		splitPane.getItems().addAll(getLeftSplitPane(), getCenterSplitPane());
		borderPane.setCenter(splitPane);
		BorderPane.setAlignment(splitPane, Pos.CENTER);
	}

	/**
	 * Left split panel, contains a treeview and a listview
	 * 
	 * @return
	 */
	private SplitPane getLeftSplitPane() {
		SplitPane splitPane = new SplitPane();
		splitPane.setOrientation(Orientation.VERTICAL);
		splitPane.setDividerPosition(0, 0.5);
		splitPane.getItems().addAll(getTreeView(), getListView());
		return splitPane;
	}

	/**
	 * The treeview (up)
	 * 
	 * @return
	 */
	private TreeView<String> getTreeView() {
		listTreeLeft = new TreeView<String>();
		return listTreeLeft;
	}

	/**
	 * The listview (down)
	 * 
	 * @return
	 */
	private ListView<String> getListView() {
		listViewLeft = new ListView<String>();
		listViewLeft.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		return listViewLeft;
	}

	/**
	 * Center split panel, contains two sets of pie charts
	 * 
	 * @return
	 */
	private SplitPane getCenterSplitPane() {
		SplitPane splitPane = new SplitPane();
		splitPane.setOrientation(Orientation.VERTICAL);
		splitPane.setDividerPosition(0, 0.3);
		splitPane.getItems().addAll(getCenterGroupUp(), getCenterGroupDown());
		return splitPane;
	}

	/**
	 * Up Tile panel, contains the first set of pie charts
	 * 
	 * @return
	 */
	private Group getCenterGroupUp() {
		Group chartGroup = new Group();
		TilePane tilePane = new TilePane();
		tilePane.setPrefColumns(2); // preferred columns

		chartLeft1 = new VAPieChart(val.getRootGroupLeft1(), this);
		chartLeft1.getPieChart().setClockwise(false);
		chartRight1 = new VAPieChart(val.getRootGroupRight1(), this);
		chartRight1.getPieChart().setClockwise(false);
		lblSource1 = new Label("Source ontology", chartLeft1.getPieChart());
		lblSource1.setContentDisplay(ContentDisplay.CENTER);
		lblTarget1 = new Label("Target ontology", chartRight1.getPieChart());
		lblTarget1.setContentDisplay(ContentDisplay.CENTER);

		tilePane.getChildren().addAll(lblSource1, lblTarget1);
		chartGroup.getChildren().add(tilePane);
		initTooltip(chartLeft1);
		return chartGroup;
	}

	/**
	 * Down Tile panel, contains the second set of pie charts
	 * 
	 * @return
	 */
	private Group getCenterGroupDown() {
		Group chartGroup = new Group();
		TilePane tilePane = new TilePane();
		tilePane.setPrefColumns(2); // preferred columns

		chartLeft1 = new VAPieChart(val.getRootGroupLeft1(), this);
		chartLeft1.getPieChart().setClockwise(false);
		chartRight1 = new VAPieChart(val.getRootGroupRight1(), this);
		chartRight1.getPieChart().setClockwise(false);
		lblSource1 = new Label("Source ontology", chartLeft1.getPieChart());
		lblSource1.setContentDisplay(ContentDisplay.CENTER);
		lblTarget1 = new Label("Target ontology", chartRight1.getPieChart());
		lblTarget1.setContentDisplay(ContentDisplay.CENTER);

		tilePane.getChildren().addAll(lblSource1, lblTarget1);
		chartGroup.getChildren().add(tilePane);
		initTooltip(chartLeft1);
		return chartGroup;
	}

	/**
	 * Add tooltip to the chart.
	 */
	private void initTooltip(VAPieChart chartLeft) {
		pieTooltip = new Tooltip("click to view more");
		for (final PieChart.Data currentData : chartLeft.getPieChart()
				.getData()) {
			Tooltip.install(currentData.getNode(), pieTooltip);
		}
	}

	/**
	 * Init Top panel, including a toolbar that consists of buttons, a choice
	 * box and a search box.
	 * 
	 * @param borderPane
	 */
	private void initTopToolbar(BorderPane borderPane) {
		ToolBar toolbar = new ToolBar();

		Region spacer1 = new Region();
		spacer1.setStyle("-fx-padding: 0 20em 0 0;");
		Region spacer2 = new Region();
		spacer2.setStyle("-fx-padding: 0 10em 0 0;");
		Region spacer3 = new Region();
		spacer3.setStyle("-fx-padding: 0 10em 0 0;");

		HBox buttonBar = new HBox();
		initButtons(buttonBar);
		initRadioButtons(buttonBar);
		BorderPane searchboxborderPane = new BorderPane();
		initSearchBox();
		searchboxborderPane.setRight(searchBox);
		HBox.setMargin(searchBox, new Insets(0, 5, 0, 0));
		toolbar.getItems().addAll(spacer1, buttonBar, spacer2,
				searchboxborderPane);
		borderPane.setTop(toolbar);
		BorderPane.setAlignment(searchboxborderPane, Pos.CENTER);
	}

	/**
	 * Init buttons in the HBox
	 * 
	 * @param buttonBar
	 */
	private void initButtons(HBox buttonBar) {
		btnRoot = new Button("Top level");
		btnUp = new Button("Go back");
		btnUFB = new Button("User feedback");
		setToolButtonActions();
		buttonBar.getChildren().addAll(btnRoot, btnUp, btnUFB);
	}

	private void initRadioButtons(HBox buttonBar) {
		ToggleGroup tg = new ToggleGroup();
		rbClass = new RadioButton("Class");
		rbClass.setToggleGroup(tg);
		rbClass.setUserData("Class");
		rbClass.setSelected(true);
		rbProperity = new RadioButton("Properity");
		rbProperity.setToggleGroup(tg);
		rbProperity.setUserData("Properity");
		buttonBar.getChildren().addAll(rbClass, rbProperity);
		setRadioButtonActions(tg);
	}

	/**
	 * Init search box
	 */
	private void initSearchBox() {
		searchBox = new VASearchBox(this);
		searchBox.setId("SearchBox");
	}

	/**
	 * Init right panel, using buttons to represent different matching
	 * algorithms
	 * 
	 * @return
	 */
	public void initRightFlowPane(BorderPane borderPane) {
		FlowPane flow = new FlowPane(Orientation.VERTICAL);
		flow.setPadding(new Insets(5, 0, 5, 0));
		flow.setVgap(8);
		flow.setHgap(4);
		flow.setPrefWrapLength(170); // preferred width allows for two columns
		flow.setStyle("-fx-background-color: DAE6F3;");
		int size = 50;
		final ToggleGroup group = new ToggleGroup();
		for (int i = 0; i < btnPages.length; i++) {
			btnPages[i] = new ToggleButton("AL" + String.valueOf(i + 1));
			btnPages[i].setStyle("-fx-font-size: " + size + "pt;");
			btnPages[i].setToggleGroup(group);
			if (i >= VASyncData.getTotalDisplayNum()) {// init visibility
				btnPages[i].setVisible(false);
				System.out.println("[test]-display num="
						+ VASyncData.getTotalDisplayNum() + ", i=" + i);
			}
			flow.getChildren().add(btnPages[i]);
		}
		setPageButtonActions();
		btnPages[0].setSelected(true);
		borderPane.setRight(flow);
	}

	// ============Pie Chart, tree/list views related logic============

	/**
	 * Update Pie chart
	 * 
	 * @param ontologyType
	 */
	public void updateLeftChart() {
		chartLeft1.updatePieChart(VAVariables.ontologyType.Source);
	}

	/**
	 * Set source pie chart's label
	 * 
	 * @param label
	 * @param empty
	 */
	public void setSourceLabel(String label, int empty) {
		lblSource1.setText(label);
		if (empty == 1) {
			lblSource1.setFont(Font.font("Verdana", 20));
			lblSource1.setTextFill(Color.RED);
		} else {
			lblSource1.setFont(Font.font("Verdana", 15));
			lblSource1.setTextFill(Color.WHITESMOKE);
		}
	}

	/**
	 * Set target pie chart's label
	 * 
	 * @param label
	 */
	public void setTargetLabel(String label) {
		lblTarget1.setText(label);
		lblTarget1.setFont(Font.font("Verdana", 20));
		lblTarget1.setTextFill(Color.RED);
	}

	/**
	 * Get the right pie chart (display only)
	 * 
	 * @return
	 */
	public VAPieChart getRightPie() {
		return chartRight1;
	}

	/**
	 * Generate new Tree view
	 * 
	 * @param parentGroup
	 */
	public void generateNewTree() {
		VAGroup parentGroup = val.generateParentGroup();
		TreeItem<String> listTreeLeftRoot = null;
		String label = "";
		if (parentGroup.getParent() == 0)
			label = "Source Root";
		else
			label = parentGroup.getRootNodeName();
		listTreeLeftRoot = new TreeItem<String>(label);
		ArrayList<VAData> data = parentGroup.getVADataArray();
		for (VAData d : data) {
			listTreeLeftRoot.getChildren().add(
					new TreeItem<String>(d.getNodeName()));
		}

		listTreeLeft.setShowRoot(true);
		listTreeLeft.setRoot(listTreeLeftRoot);
		listTreeLeftRoot.setExpanded(true);
		treeviewAction(data);
	}

	/**
	 * Add tree view actions (refresh pie chart and related data)
	 * 
	 * @param data
	 */
	private void treeviewAction(final ArrayList<VAData> data) {
		listTreeLeft.getSelectionModel().selectedItemProperty()
				.addListener(new ChangeListener<Object>() {

					@Override
					public void changed(ObservableValue<?> observable,
							Object oldValue, Object newValue) {

						TreeItem<String> selectedItem = (TreeItem<String>) newValue;
						if (selectedItem != null) {
							String selected = selectedItem.getValue();
							for (VAData da : data) {
								if (da.getNodeName().equals(selected)) {
									VAGroup newGroup = new VAGroup();
									newGroup.setRootNode(da);
									newGroup.setParent(val.getCurrentGroup1()
											.getGroupID());
									newGroup.setListVAData(VASyncData
											.getChildrenData(da,
													ontologyType.Source));
									updateAllWithNewGroup(newGroup);
								}
							}
						}
					}

				});
	}

	/**
	 * The stop is just a on and off variable
	 * 
	 * @param i
	 */
	public void setStop(int i) {
		stop = i;
	}

	public int getStop() {
		return stop;
	}

	public ListView<String> getlistView() {
		return listViewLeft;
	}

	public void setListView(ListView<String> list) {
		listViewLeft = list;
	}

	/**
	 * Update the pie chart panel with a new group
	 * 
	 * @param newGroup
	 */
	public void updateAllWithNewGroup(VAGroup newGroup) {
		val.updateCurrentGroup(newGroup);
		setUpButton(newGroup);
		chartLeft1.updatePieChart(ontologyType.Source);
		generateNewTree();
		chartLeft1.clearList();
	}

	// ==========Button & List Event==================

	/**
	 * Haven't been used yet
	 * 
	 * @param n
	 */
	public void setButtonVisible(int n) {
		if (n < btnPages.length && btnPages != null && btnPages[n] != null)
			btnPages[n].setVisible(true);
	}

	/**
	 * Enable/Disable go up button
	 * 
	 * @param group
	 */
	public void setUpButton(VAGroup group) {
		if (group != null) {
			if (btnUp.isDisable()) {
				btnUp.setDisable(false);
			}
		}
	}

	private void setRadioButtonActions(final ToggleGroup tg) {
		tg.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			public void changed(ObservableValue<? extends Toggle> ov,
					Toggle old_toggle, Toggle new_toggle) {
				if (tg.getSelectedToggle() != null) {
					String selected = tg.getSelectedToggle().getUserData()
							.toString();
					if (selected.equals("Class")) {
						VAPanelLogic
								.setCurrentNodeType(VAVariables.nodeType.Class);
					} else {
						VAPanelLogic
								.setCurrentNodeType(VAVariables.nodeType.Property);
					}
					val.InitData();
					updateAllWithNewGroup(val.getRootGroupLeft1());
				}
			}
		});
	}

	/**
	 * Add event for page buttons
	 */
	private void setPageButtonActions() {
		int num = btnPages.length;
		for (int i = 0; i < num; i++) {
			final int cur = i + 1;
			btnPages[i].setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent arg0) {
					// TODO Auto-generated method stub
					VASyncData.setCurrentDisplayNum(cur);
					val.InitData();
					updateAllWithNewGroup(val.getRootGroupLeft1());
					// set colors here
				}
			});
		}
	}

	/**
	 * Add event for buttons
	 */
	private void setToolButtonActions() {
		/**
		 * Go to root panel
		 */
		btnRoot.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				updateAllWithNewGroup(val.getRootGroupLeft1());
			}

		});

		/**
		 * Go to previous panel (can only click once)
		 */
		btnUp.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				updateAllWithNewGroup(val.getPreviousGroup1());
				btnUp.setDisable(true);
			}

		});

		btnUFB.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				// VAUserFeedBack ufbFrame = new VAUserFeedBack();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						new VAUserFeedBack();
					}
				});
			}

		});
	}
}

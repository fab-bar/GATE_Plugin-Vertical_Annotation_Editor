/**************************************
 * Copyright 2015 - Universität Hamburg, SIGS.
 * 
 * This file is part of the Vertical Annotation Editor, a plugin for GATE.
 *
 * Vertical Annotation Editor is free software: 
 * you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Vertical Annotation Editor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Der Vertical Annotation Editor ist Freie Software: 
 * Sie können es unter den Bedingungen der GNU General Public License, 
 * wie von der Free Software Foundation, Version 3 der Lizenz oder 
 * (nach Ihrer Wahl) jeder neueren veröffentlichten Version,
 * weiterverbreiten und/oder modifizieren.
 * Der Vertical Annotation Editor wird in der Hoffnung, 
 * dass es nützlich sein wird, aber OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; 
 * sogar ohne die implizite Gewährleistung der MARKTFÄHIGKEIT
 * oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
 * Siehe die GNU General Public License für weitere Details.
 * 
 * Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
 * Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 * 
 */


/**
 * @author Fabian Barteld
 */

package gate.gui.docview;

import gate.Annotation;
import gate.AnnotationSet;
import gate.FeatureMap;
import gate.Gate;
import gate.LanguageResource;
import gate.Resource;
import gate.creole.AnnotationSchema;
import gate.creole.FeatureSchema;
import gate.creole.metadata.CreoleResource;
import gate.event.CreoleEvent;
import gate.event.CreoleListener;
import gate.event.DocumentEvent;
import gate.event.DocumentListener;
import gate.gui.AnnotationFilterPanel;
import gate.swing.XJTable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.ListSelectionModel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.AutoCompleteSupport.AutoCompleteCellEditor;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.DefaultEventTableModel;

/**
 * VerticalAnnotationEditor is a VisualResource for GATE.
 * It shows Annotations of a specific AnnotationType in a JTable.
 * If an AnnotationSchema for the shown Type is loaded,
 * the annotations can be edited.
 *
 */
@CreoleResource 
public class VerticalAnnotationEditor extends AbstractDocumentView { 
 
    protected JSplitPane mainPanel;
    protected XJTable table;
    protected JScrollPane scroller;
    
    protected TextualDocumentView textView;

    private JPanel choicePanel;
    private JComboBox<String> annotationSetComboBox;
    private JComboBox<String> annotationTypeComboBox;
	
    private EventList<Annotation> annotationList;
    private SortedList<Annotation> sortedAnnotations;
    private FilterList<Annotation> visibleAnnotations;
    private DefaultEventTableModel<Annotation> tableModel;
    private TextMatcherEditor<Annotation> textMatcherEditor;

    /**
     * Gets the associated DocumentView and loaded AnnotationSchemas.
     */
    protected void initGateContext() {
	
	// find the DocumentView associated with 
	// from gate.gui.docview.AnnotationStackView - initGUI()
	Iterator<DocumentView> centralViewsIter = owner.getCentralViews().iterator();
	while(textView == null && centralViewsIter.hasNext()){
	    DocumentView aView = centralViewsIter.next();
	    if(aView instanceof TextualDocumentView)
		textView = (TextualDocumentView) aView;
	}	
	
	// load the Schemas
	// from gate.gui.docview.AnnotationEditor - initData()
	schemasByType = new HashMap<String, AnnotationSchema>();
	java.util.List<LanguageResource> schemas =
	    Gate.getCreoleRegister().getLrInstances("gate.creole.AnnotationSchema");
	for(Iterator<LanguageResource> schIter = schemas.iterator(); schIter.hasNext();) {
	    AnnotationSchema aSchema = (AnnotationSchema)schIter.next();
	    schemasByType.put(aSchema.getAnnotationName(), aSchema);
	}
    }

    /**
     * Installs listeners for changes in the GATE-Environment
     */
    protected void initListeners(){
	
	// Listen for movements in the text view
	textView.getTextView().addCaretListener(new CaretListener() {
		public void caretUpdate(CaretEvent e) {
		    updateSelection();
		}
	    });
	// TODO 
	// Listen for changes in the Document
	textView.getDocument().addDocumentListener(new DocumentListener() {

		@Override
		public void annotationSetAdded(DocumentEvent arg0) {
		    // TODO: do something here!
		}

		@Override
		public void annotationSetRemoved(DocumentEvent arg0) {
		    // TODO: do something here!
		    
		}

		@Override
		public void contentEdited(DocumentEvent arg0) {
		    // TODO: do something here!
		}
		
	    });
	// Listen for loading or unloading AnnotationSchemas
	// from gate.gui.docview.AnnotationEditor - initData()
	Gate.getCreoleRegister().addCreoleListener(new CreoleListener() {
		public void resourceLoaded(CreoleEvent e) {
		    Resource newResource = e.getResource();
		    if(newResource instanceof AnnotationSchema) {
			AnnotationSchema aSchema = (AnnotationSchema)newResource;
			schemasByType.put(aSchema.getAnnotationName(), aSchema);
			// Reload the AnnotationTable
			try {
			    SwingUtilities.invokeAndWait(new Runnable() {
				    public void run() {
					initializeTable();
				    }
				});
			}
			catch (Exception ex) {
			    // TODO: should not occur - do something here?
			}		        
		    }
		}
		
		public void resourceUnloaded(CreoleEvent e) {
		    Resource newResource = e.getResource();
		    if(newResource instanceof AnnotationSchema) {
			AnnotationSchema aSchema = (AnnotationSchema)newResource;
			if(schemasByType.containsValue(aSchema)) {
			    schemasByType.remove(aSchema.getAnnotationName());
			    initializeTable();
			}
		    }
		}
		
		public void datastoreOpened(CreoleEvent e) {
		}

		public void datastoreCreated(CreoleEvent e) {
		}

		public void datastoreClosed(CreoleEvent e) {
		}

		public void resourceRenamed(Resource resource, String oldName,
					    String newName) {
		}
	    });
	}


    protected void loadAnnotationSet() {
			  
	annotationTypeComboBox.removeAllItems();
			  
	String setName = (String)annotationSetComboBox.getSelectedItem();
	AnnotationSet annotations = null;
	if(setName == "<Default Set>") {
	    annotations = textView.getDocument().getAnnotations();
	}
	else {
	    annotations = textView.getDocument().getAnnotations(setName);					
	}
		
	// // TODO: listen for changes in the annotation set 
	// // the following version would keep adding the listener to annotation sets and never remove it
	// // to remove the listener we need to keep track of the currentSet
	// annotations.addAnnotationSetListener(new AnnotationSetListener() {
	//
	//	@Override
	//	public void annotationAdded(AnnotationSetEvent arg0) {
	//		System.out.print("Addition");
	//		loadAnnotations();
	//	}
	//
	//	@Override
	//	public void annotationRemoved(AnnotationSetEvent arg0) {
	//		loadAnnotations();
	//	}
	//			
	// });
				
	String[] AnnotationTypes = annotations.getAllTypes().toArray(new String[0]);
	Arrays.sort(AnnotationTypes, StringComparator);
	for(String AnnotationType: AnnotationTypes) {
	    annotationTypeComboBox.addItem(AnnotationType);
	}
	
	initializeTable();
	loadAnnotations();
	
    }
	

	
    protected void initializeTable() {

	String annotType = (String)annotationTypeComboBox.getSelectedItem();
		
	// Setting the TableFormat and Editor
	if (schemasByType.containsKey(annotType)) {
			
	    // table format that depends on an annotation schema
	    AnnotationSchema schema = schemasByType.get(annotType);
	    tableModel.setTableFormat(new AnnotationSchemaTableFormat(schema));
	    
	    // enumerations get specific cell editors (combobox with autocompletion)
	    // and a specific rendered (renders all values not in the list red)
	    FeatureSchema[] schemas = schema.getFeatureSchemaSet().toArray(new FeatureSchema[0]);
	    int column_offset = tableModel.getTableFormat().getColumnCount() - schemas.length;
	    for(int i = 0; i < schemas.length; i++) {
		FeatureSchema fs = schemas[i];
		if (fs.isEnumeration()) {
		    
		    String[] values = Arrays.copyOf(fs.getPermittedValues().toArray(), fs.getPermittedValues().toArray().length, String[].class);		    	    	
		    Arrays.sort(values, StringComparator);

		    BasicEventList<String> list = new BasicEventList<String>();
		    list.addAll(Arrays.asList(values));
		    AutoCompleteCellEditor<String> columnEditor = AutoCompleteSupport.createTableCellEditor(list);
		    AutoCompleteSupport<String> ac = columnEditor.getAutoCompleteSupport();
		    ac.setStrict(true);
		    ac.setFilterMode(TextMatcherEditor.CONTAINS);
		    ac.setSelectsTextOnFocusGain(true);
		    ac.setHidesPopupOnFocusLost(false);
		    if (fs.isOptional()) {
			ac.setFirstItem("");		    	    		
		    }
		    TableColumn cm = table.getColumnModel().getColumn(i+column_offset);
		    cm.setCellEditor(columnEditor);		    	    	
					
		    // adding empty String, if feature is optional
		    if (fs.isOptional()) {
			String[] combined = new String[values.length + 1];
			combined[0] = "";
			System.arraycopy(values, 0, combined, 1, values.length);
			values = combined;
		    }
					
		    // // set the renderer
		    ColorCheckTableCellRenderer checkableCellRenderer = new ColorCheckTableCellRenderer();
		    	    	
		    checkableCellRenderer.setValidValues(values);
		    cm.setCellRenderer(checkableCellRenderer);
		}
	    }
	    table.setTabSkipUneditableCell(true);
	}
	else {
	    // generic table format for annotations
	    tableModel.setTableFormat(new AnnotationTableFormat());

	    table.setTabSkipUneditableCell(false);
	}

    }

    protected void loadAnnotations() {
		
	String setName = (String)annotationSetComboBox.getSelectedItem();
	String annotType = (String)annotationTypeComboBox.getSelectedItem();
				    
	// Setting the Data
		    
	AnnotationSet annotations = null;
	if(setName == "<Default Set>") {
	    annotations = textView.getDocument().getAnnotations();
	}
	else {
	    annotations = textView.getDocument().getAnnotations(setName);					
	}

	annotations = annotations.get(annotType);
		    
	annotationList.clear();
	annotationList.addAll(annotations);
			
	updateSelection();
		
    }
		
    /**
     *  Selects one of the visible annotations that cover the caret.
     *  If no annotation matches these criteria, the selection is not changed.
     */
    protected void updateSelection() {
	
	// if we don't know a textview or no annotation is visible
	// -> just return
	if (textView == null) { return; }
	if (visibleAnnotations.isEmpty()) { return; }
			    
	String setName = (String)annotationSetComboBox.getSelectedItem();
	String annotType = (String)annotationTypeComboBox.getSelectedItem();
				
	Long caretPosition = new Long(textView.getTextView().getCaretPosition());
				
	AnnotationSet annotations = null;
	if(setName == "<Default Set>") {
	    annotations = textView.getDocument().getAnnotations();
	}
	else {
	    annotations = textView.getDocument().getAnnotations(setName);					
	}
	annotations = annotations.getCovering(annotType, caretPosition, caretPosition);
				
				
	Annotation selectedAnnotation = null;
	int aktAuswahl = table.getSelectedRow();
	if (aktAuswahl >= 0) {
	    selectedAnnotation = visibleAnnotations.get(aktAuswahl);
	}
		
	// if no annotation covers the current caretPosition
	// -> return
	if (annotations.isEmpty()) {
	    return;
	}
				
	// if the selected annotation already covers the caretPosition
	// -> return
	if (selectedAnnotation != null && annotations.contains(selectedAnnotation)) {
	    return;
	}
				
	// otherwise: select the first annotation covering the caretPosition
	int index = visibleAnnotations.indexOf(annotations.iterator().next());
	if (index >= 0) {
	    table.setRowSelectionInterval(index, index);
	    table.scrollRectToVisible(table.getCellRect(index, 0, true));
	}

    }

    /**
     * populate the GUI
     */
    public void initGUI() { 
		

	initGateContext();
	initListeners();
			
	JPanel settingsPanel = new JPanel();
	settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
			
	choicePanel = new JPanel();
		
	// create the List of AnnotationSets
	String[] values = textView.getDocument().getAnnotationSetNames().toArray(new String[0]);
	Arrays.sort(values, StringComparator);
		    
	String[] combined = new String[values.length + 1];
	combined[0] = "<Default Set>";
	System.arraycopy(values, 0, combined, 1, values.length);
	values = combined;

	annotationSetComboBox = new JComboBox<String>(values);
	choicePanel.add(annotationSetComboBox);

	annotationSetComboBox.addActionListener (new ActionListener () {
		public void actionPerformed(ActionEvent e) {
		    loadAnnotationSet();
		}
	    });
			
	// Create the AnnotationType Chooser
	annotationTypeComboBox = new JComboBox<String>();
	choicePanel.add(annotationTypeComboBox);
			
	annotationTypeComboBox.addActionListener (new ActionListener () {
		public void actionPerformed(ActionEvent e) {
		    initializeTable();
		    loadAnnotations();
		}
	    });
			
	// create the filter
	AnnotationFilterPanel filterPanel = new AnnotationFilterPanel();

	settingsPanel.add(choicePanel);
	settingsPanel.add(filterPanel);
     
	annotationList = new BasicEventList<Annotation>();
	sortedAnnotations = new SortedList<Annotation>(annotationList, new AnnotationOffsetComparator());
	textMatcherEditor = new TextMatcherEditor<Annotation>(new TextFilterator<Annotation>(){
		public void getFilterStrings(List<String> baseList, Annotation annotation) {
		    if (annotation != null) {
			for(Map.Entry<Object, Object> feature: annotation.getFeatures().entrySet()) {
			    String filterString = "";
			    if (feature.getKey() != null) {
				filterString += feature.getKey().toString();
			    }
			    filterString += "/";
			    if (feature.getValue() != null) {
				filterString += feature.getValue().toString();
			    }
			    baseList.add(filterString);
			}
		    }
		    
		}
	    });
	textMatcherEditor.setMode(TextMatcherEditor.REGULAR_EXPRESSION);
	visibleAnnotations = new FilterList<Annotation>(sortedAnnotations, textMatcherEditor);
	tableModel = new DefaultEventTableModel<Annotation>(
							    GlazedListsSwing.swingThreadProxyList(visibleAnnotations), 
							    new AnnotationTableFormat());
			    	
	filterPanel.addChangeListener(new ChangeListener() {

		@Override
		public void stateChanged(ChangeEvent arg0) {
		    if (((AnnotationFilterPanel)arg0.getSource()).isActivated()) {
			
			ArrayList<String> filterStrings = new ArrayList<String>();
			for(Map.Entry<Object, Object> feature: ((AnnotationFilterPanel)arg0.getSource()).getFeatureValueFilter().entrySet()) {
			    String filterString = "";
			    if (feature.getKey() != null) {
				filterString += java.util.regex.Pattern.quote(feature.getKey().toString());
			    }
			    filterString += "[^/]*/";
			    if (feature.getValue() != null) {
				filterString += java.util.regex.Pattern.quote(feature.getValue().toString());
			    }
			    filterString += ".*";
			    filterStrings.add(filterString);
			}
			textMatcherEditor.setFilterText(filterStrings.toArray(new String[filterStrings.size()]));
		    }
		    else {
			textMatcherEditor.setFilterText(new String[0]);
		    }
		}
		
	    });
				
		
		
	// setup the table
	
	table = new XJTable(tableModel);
	table.setIntercellSpacing(new Dimension(2, 0));
	table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	// setup XJTable
	table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
	table.setEnableHidingColumns(true);
	table.setTabSkipUneditableCell(false);
	table.setEditCellAsSoonAsFocus(true);
	// TODO: 
	// show which column is used for sorting
	table.setSortable(true);
	table.setSortedColumn(0);

	// update the caret position to follow the selection
	table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
		    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		    if (!lsm.isSelectionEmpty()) {
					
			if (table.getSelectedRow() < visibleAnnotations.size()) {
			    Annotation annot = visibleAnnotations.get(table.getSelectedRow());
			    int caretPosition = textView.getTextView().getCaretPosition();
			    
			    if (caretPosition < annot.getStartNode().getOffset().intValue() ||
				caretPosition > annot.getEndNode().getOffset().intValue()) {
				textView.getTextView().setCaretPosition(
									annot.getEndNode().getOffset().intValue());	
			    }
			}
		    }
		}
	    });
		
		
	// setup the main Panel
			    
	scroller = new JScrollPane(table);
			    

			
	mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				   new JScrollPane(settingsPanel), scroller);
	mainPanel.setOneTouchExpandable(true);
	mainPanel.setDividerLocation(100);
			  
	loadAnnotationSet();
		     
    } 
 
    /**
     *  Returns the type of this view
     */ 
    public int getType() { 
	return gate.gui.docview.DocumentView.CENTRAL;
    } 
 
    /** 
     * Returns the actual UI component this view represents.
     */ 
    public Component getGUI() { 
	return mainPanel;	
    } 
 
    /**
     *  This method is called whenever view becomes active.
     *  It does nothing.
     */ 
    public void registerHooks() { 
	// do nothing      			
    } 
 
    /**
     *  This method called whenever view becomes inactive.
     *  It does nothing. 
     */ 
    public void unregisterHooks() { 
	// do nothing 
    } 

	
    /// Helpful classes
    Comparator<String> StringComparator = new Comparator<String> () {
	public int compare(String strA, String strB) {
	    return strA.compareTo(strB);
	}
    };	

    
    // some code for this class was taken from gate.gui.docview.AnnotationListView
    class AnnotationTableFormat implements TableFormat<Annotation> {
        	  
	private static final int START_COL = 0;
	private static final int END_COL = 1;
	private static final int ID_COL = 2;
	private static final int FEATURES_COL = 3;
		
	public int getColumnCount(){
	    return 4;
	}

	public String getColumnName(int column){
	    switch(column){
	    case START_COL: return "Start";
	    case END_COL: return "End";
	    case ID_COL: return "Id";
	    case FEATURES_COL: return "Features";
	    default: return "?";
	    }
	}

	@Override
	public Object getColumnValue(Annotation aData, int column) {
	    switch(column){
	    case START_COL: return aData.getStartNode().getOffset();
	    case END_COL: return aData.getEndNode().getOffset();
	    case ID_COL: return aData.getId();
	    case FEATURES_COL:
		//sort the features by name
		FeatureMap features = aData.getFeatures();
		List<String> keyList = new ArrayList<String>(features.keySet().size());
		Iterator<Object> it = features.keySet().iterator(); 
		while(it.hasNext()) {
		    keyList.add(it.next().toString());
		}
		Collections.sort(keyList);
		StringBuffer strBuf = new StringBuffer("{");
		Iterator<String> keyIter = keyList.iterator();
		boolean first = true;
		while(keyIter.hasNext()){
		    Object key = keyIter.next();
		    Object value = features.get(key);
		    if(first){
			first = false;
		    }
		    else {
			strBuf.append(", ");
		    }
		    strBuf.append(key.toString());
		    strBuf.append("=");
		    strBuf.append(value == null ? "[null]" : value.toString());
		}
		strBuf.append("}");
		return strBuf.toString();
	    default: return "?";
	    }
	}
    } 
          
          
    class AnnotationSchemaTableFormat implements WritableTableFormat<Annotation>, AdvancedTableFormat<Annotation> {
        	  
	private FeatureSchema[] schema;
	
	public AnnotationSchemaTableFormat (AnnotationSchema schema) {
	    super();
	    this.schema = schema.getFeatureSchemaSet().toArray(new FeatureSchema[0]);
	}
 	     
	public int getColumnCount(){
	    return schema.length+2;
	}
		
	public String getColumnName(int column){
	    if (column == 0) {
		return "Offset";
	    }
	    else if (column == 1) {
		return "Text";
	    }
	    else {
		return schema[column-2].getFeatureName();
	    }
	}
		
	@Override
	public Object getColumnValue(Annotation aData, int column) {
	    if(column >= getColumnCount()) return null;
	    if (column == 0) {
		return aData.getStartNode().getOffset();
	    }
	    else if (column == 1) {
		return gate.Utils.stringFor(textView.getDocument(), aData);
	    }
	    else {
		return aData.getFeatures().get(getColumnName(column));	
	    }
	}
 				
	public boolean isEditable(Annotation annot, int columnIndex){
	    // only first two column are not editable
	    return columnIndex > 1;
	}
   	         
	public Annotation setColumnValue(Annotation baseObject, Object editedValue, int column) {
	    FeatureMap map = baseObject.getFeatures();
	    map.put(getColumnName(column), editedValue);
	    baseObject.setFeatures(map);
	    return baseObject;
	}

	@Override
	public Class<?> getColumnClass(int column) {
	    if (column == 0) {
		return Long.class;
	    }
	    else if (column == 1) {
		return String.class;
	    }
	    else {
		FeatureSchema fs = schema[column-2];
		return fs.getFeatureValueClass();
	    }
	}

	@Override
	public Comparator<?> getColumnComparator(int column) {
	    return null;
	}
    }
          
    @SuppressWarnings("serial")
    class ColorCheckTableCellRenderer extends DefaultTableCellRenderer {
	
	TreeSet<String> valid_values = new TreeSet<String> (new Comparator<String> () {
		public int compare(String strA, String strB) {
		    return strA.compareTo(strB);
		}
	    });
  		
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
	    if (value == null || valid_values.contains(value)) {
		if (isSelected) {
		    setBackground( javax.swing.UIManager.getColor("Table.selectionBackground") );
		}
		else if (hasFocus) {
		    setBackground( javax.swing.UIManager.getColor("Table.focusCellBackground") );
		}
		else {
		    setBackground( javax.swing.UIManager.getColor("Table.dropCellBackground") );
		}
	    }
	    else {
		setBackground( Color.RED );
	    }
	    return this;
	}
             
	public void setValidValues (String[] validValues) {
	    valid_values.clear();
	    valid_values.addAll(Arrays.asList(validValues));                      	   
	}
    }
          
          
    class AnnotationOffsetComparator implements Comparator<Annotation> {
	public int compare(Annotation annotationA, Annotation annotationB) {
	    if (annotationA == null) {
		return 1;
	    }
	    if (annotationB == null) {
		return -1;
	    }
	    
        	    	
	    int AValue = annotationA.getStartNode().getOffset().intValue();
	    int BValue = annotationB.getStartNode().getOffset().intValue();

	    return AValue - BValue;
	}
    }


    /**
     * Stores the Annotation schema objects available in the system. The
     * annotation types are used as keys for the map.
     */
    protected Map<String, AnnotationSchema> schemasByType;

}
